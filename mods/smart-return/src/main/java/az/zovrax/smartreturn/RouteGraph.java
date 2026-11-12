package az.zovrax.smartreturn;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.BiPredicate;

/**
 * A physically walked cave network with potentially multiple reachable exits.
 * Nodes are connected ONLY by successive positions actually traversed by a
 * player. Revisited corridors form loops, then Dijkstra finds the shortest
 * recorded return, not a shortcut through unrecorded blocks.
 */
public final class RouteGraph {
    public static final int LIMIT = 12000;
    private static final double STEP = 1.05;
    private static final double SNAP = 0.78;
    private final List<Vec3d> nodes = new ArrayList<>();
    private final List<List<Edge>> links = new ArrayList<>();
    private final List<Integer> exits = new ArrayList<>();
    private int lastNode = -1;
    private Vec3d lastSample;
    private record Edge(int target, double distance) { }
    private record Visit(int index, double distance) { }
    public record Path(List<Vec3d> points, Vec3d exit, double distance) { }

    public RouteGraph(Vec3d entrance) {
        int first = addNode(entrance);
        exits.add(first);
        lastNode = first;
        lastSample = entrance;
    }
    private RouteGraph() { }

    public int count() { return nodes.size(); }
    public List<Vec3d> entrances() {
        return exits.stream().map(nodes::get).toList();
    }
    private int addNode(Vec3d p) {
        int index = nodes.size();
        nodes.add(p);
        links.add(new ArrayList<>());
        return index;
    }
    public Vec3d node(int index) { return nodes.get(index); }

    public int nearest(Vec3d p, double radius) {
        double limit = radius * radius;
        int match = -1;
        for (int i = 0; i < nodes.size(); i++) {
            Vec3d q = nodes.get(i);
            if (Math.abs(q.y - p.y) > 1.4) continue;
            double d = q.squaredDistanceTo(p);
            if (d < limit) { limit = d; match = i; }
        }
        return match;
    }
    private void connect(int a,int b,double length) {
        if (a == b || a < 0 || b < 0 || a >= count() || b >= count()) return;
        if (!Double.isFinite(length) || length <= 0 || length > 8) return;
        if (links.get(a).stream().noneMatch(e -> e.target == b)) {
            links.get(a).add(new Edge(b, length));
            links.get(b).add(new Edge(a, length));
        }
    }
    public void reattach(Vec3d at) {
        lastNode = nearest(at, 1.15);
        lastSample = lastNode < 0 ? null : nodes.get(lastNode);
    }
    public void sample(Vec3d point, BiPredicate<Vec3d,Vec3d> traversable) {
        if (lastSample != null) {
            double moved = lastSample.distanceTo(point);
            if (moved < STEP) return;
            if (moved > 5.5) { // teleport or discontinuous movement
                reattach(point);
                return;
            }
        }
        int match = nearest(point, SNAP);
        // When an apparently close node is behind a wall, it is NOT the same
        // walked corridor. Do not merge routes across collision geometry.
        if (match >= 0 && !traversable.test(nodes.get(match), point)) match = -1;
        int next = match >= 0 ? match : (count() < LIMIT ? addNode(point) : -1);
        if (next < 0) return;
        if (lastNode >= 0) {
            Vec3d source = nodes.get(lastNode);
            if (source.distanceTo(point) <= 5.5 && traversable.test(source,point))
                connect(lastNode,next,source.distanceTo(nodes.get(next)));
        }
        lastNode = next;
        lastSample = point;
    }
    /** Call only when the player has physically reached daylight from this cave. */
    public void addExitNear(Vec3d lastInside) {
        int n = nearest(lastInside, 2.5);
        if (n < 0 || exits.contains(n)) return;
        exits.add(n);
    }

    /** Connect two previously separate recordings after physically meeting. */
    public boolean mergeAt(RouteGraph other, Vec3d meeting, BiPredicate<Vec3d,Vec3d> walkable) {
        if (other == this || count() + other.count() > LIMIT) return false;
        int local = nearest(meeting, 1.45);
        int remote = other.nearest(meeting, 1.45);
        if (local < 0 || remote < 0 ||
                !walkable.test(nodes.get(local),other.nodes.get(remote))) return false;
        int offset = count();
        for (Vec3d p:other.nodes) addNode(p);
        for (int a = 0; a < other.links.size(); a++) {
            for (Edge edge:other.links.get(a)) {
                if (edge.target > a) connect(offset+a,offset+edge.target,edge.distance);
            }
        }
        for (int exit:other.exits) if (!exits.contains(offset+exit)) exits.add(offset+exit);
        connect(local,offset+remote,Math.max(.05,nodes.get(local).distanceTo(nodes.get(offset+remote))));
        // Prevent consecutive samples after merge from jumping back to the
        // other recorder's previous player position.
        return true;
    }

    /** Shortest recorded walking route from player to ANY known exit. */
    public Path homeward(Vec3d player) {
        int start = nearest(player, 7.0);
        if (start < 0 || exits.isEmpty()) return null;
        double[] distance = new double[count()];
        int[] parent = new int[count()];
        Arrays.fill(distance,Double.POSITIVE_INFINITY);
        Arrays.fill(parent,-1);
        distance[start] = 0;
        PriorityQueue<Visit> queue = new PriorityQueue<>(Comparator.comparingDouble(Visit::distance));
        queue.add(new Visit(start,0));
        int exit = -1;
        while (!queue.isEmpty()) {
            Visit next = queue.poll();
            if (next.distance > distance[next.index]) continue;
            if (exits.contains(next.index)) { exit = next.index; break; }
            for (Edge edge:links.get(next.index)) {
                double proposal = distance[next.index]+edge.distance;
                if (proposal < distance[edge.target]) {
                    distance[edge.target] = proposal;
                    parent[edge.target] = next.index;
                    queue.add(new Visit(edge.target,proposal));
                }
            }
        }
        if (exit == -1) return null;
        List<Vec3d> path = new ArrayList<>();
        for (int i=exit;i>=0;i=parent[i]) {
            path.add(nodes.get(i));
            if (i == start) break;
        }
        Collections.reverse(path);
        return new Path(List.copyOf(path),nodes.get(exit),distance[exit]+
                player.distanceTo(nodes.get(start)));
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("format",2);
        JsonArray positions = new JsonArray();
        for (Vec3d p:nodes) {
            JsonArray a=new JsonArray();
            a.add(p.x);a.add(p.y);a.add(p.z);positions.add(a);
        }
        JsonArray edges=new JsonArray();
        for (int i=0;i<links.size();i++) {
            for (Edge e:links.get(i)) if(e.target>i) {
                JsonArray row=new JsonArray();
                row.add(i);row.add(e.target);row.add(e.distance);edges.add(row);
            }
        }
        JsonArray entrances=new JsonArray();
        for (int exit:exits) entrances.add(exit);
        json.add("nodes",positions);
        json.add("edges",edges);
        json.add("exits",entrances);
        return json;
    }
    /** Supports the original 0.1.0 graph format without destroying old data. */
    public static RouteGraph fromJson(JsonObject json) {
        int format=json.get("format").getAsInt();
        if (format!=1 && format!=2) throw new IllegalArgumentException("Unknown route format");
        RouteGraph graph=new RouteGraph();
        for(JsonElement el:json.getAsJsonArray("nodes")) {
            JsonArray p=el.getAsJsonArray();
            if(graph.count()>=LIMIT)throw new IllegalArgumentException("Route too large");
            double x=p.get(0).getAsDouble(),y=p.get(1).getAsDouble(),z=p.get(2).getAsDouble();
            if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z))
                throw new IllegalArgumentException("Invalid coordinates");
            graph.addNode(new Vec3d(x,y,z));
        }
        if(graph.count()==0)throw new IllegalArgumentException("Empty cave route");
        for(JsonElement el:json.getAsJsonArray("edges")) {
            JsonArray p=el.getAsJsonArray();
            int a=p.get(0).getAsInt(),b=p.get(1).getAsInt();
            double length=p.get(2).getAsDouble();
            if(a<0||b<0||a>=graph.count()||b>=graph.count()||
                    !Double.isFinite(length)||length<=0||length>8)
                throw new IllegalArgumentException("Bad route edge");
            graph.connect(a,b,length);
        }
        if(format==2 && json.has("exits")) {
            for(JsonElement p:json.getAsJsonArray("exits")) {
                int i=p.getAsInt();
                if(i<0||i>=graph.count())throw new IllegalArgumentException("Bad exit");
                if(!graph.exits.contains(i))graph.exits.add(i);
            }
        } else graph.exits.add(0);
        if(graph.exits.isEmpty())graph.exits.add(0);
        return graph;
    }
}
