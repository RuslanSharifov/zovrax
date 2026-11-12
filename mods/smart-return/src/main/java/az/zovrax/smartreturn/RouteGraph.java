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

/**
 * Walked-path graph. Edges are added only between successive, physically
 * sampled positions. Re-visiting a nearby node creates a loop-closing edge
 * instead of a second copy of the same corridor. Dijkstra can then choose
 * the shortest KNOWN way home, never a straight line through rock.
 */
public final class RouteGraph {
    public static final int LIMIT = 10000;
    private static final double STEP = 1.8;
    private static final double SNAP = 1.35;
    private final List<Vec3d> nodes = new ArrayList<>();
    private final List<List<Edge>> links = new ArrayList<>();
    private int lastNode = -1;
    private Vec3d lastSample;
    private record Edge(int target, double distance) { }
    private record Visit(int node, double distance) { }

    public RouteGraph(Vec3d entrance) {
        addNode(entrance);
        lastNode = 0;
        lastSample = entrance;
    }
    private RouteGraph() { }

    public int count() { return nodes.size(); }
    public Vec3d entrance() { return nodes.get(0); }
    public Vec3d node(int index) { return nodes.get(index); }
    private int addNode(Vec3d pos) {
        int index = nodes.size();
        nodes.add(pos);
        links.add(new ArrayList<>());
        return index;
    }
    public int nearest(Vec3d position, double max) {
        int best = -1;
        double bestDist = max * max;
        for (int i = 0; i < nodes.size(); i++) {
            Vec3d pos = nodes.get(i);
            if (Math.abs(pos.y - position.y) > 1.6) continue;
            double d = pos.squaredDistanceTo(position);
            if (d < bestDist) {
                best = i;
                bestDist = d;
            }
        }
        return best;
    }
    private void connect(int a, int b, double distance) {
        if (a == b) return;
        if (links.get(a).stream().noneMatch(edge -> edge.target == b)) {
            links.get(a).add(new Edge(b, distance));
            links.get(b).add(new Edge(a, distance));
        }
    }
    public void sample(Vec3d position) {
        if (lastSample != null) {
            double delta = position.distanceTo(lastSample);
            if (delta < STEP) return;
            if (delta > 8.0) {
                // Teleports and discontinuous movement do not create phantom corridors.
                lastNode = nearest(position, 2.5);
                lastSample = position;
                return;
            }
        }
        int matched = nearest(position, SNAP);
        int next = matched >= 0 ? matched : (nodes.size() < LIMIT ? addNode(position) : -1);
        if (next < 0) return;
        if (lastNode >= 0) connect(lastNode, next, nodes.get(lastNode).distanceTo(position));
        lastNode = next;
        lastSample = position;
    }

    /** Returns nearest -> ... -> entrance, or an empty list when disconnected. */
    public List<Vec3d> homeward(Vec3d from) {
        int start = nearest(from, 6.0);
        if (start < 0) return List.of();
        double[] dist = new double[nodes.size()];
        int[] prev = new int[nodes.size()];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(prev, -1);
        PriorityQueue<Visit> queue = new PriorityQueue<>(Comparator.comparingDouble(Visit::distance));
        queue.add(new Visit(start, 0));
        dist[start] = 0;
        while (!queue.isEmpty()) {
            Visit current = queue.poll();
            if (current.distance > dist[current.node]) continue;
            if (current.node == 0) break;
            for (Edge edge : links.get(current.node)) {
                double d = current.distance + edge.distance;
                if (d < dist[edge.target]) {
                    dist[edge.target] = d;
                    prev[edge.target] = current.node;
                    queue.add(new Visit(edge.target, d));
                }
            }
        }
        if (!Double.isFinite(dist[0])) return List.of();
        List<Vec3d> path = new ArrayList<>();
        for (int index = 0; index >= 0; index = prev[index]) {
            path.add(nodes.get(index));
            if (index == start) break;
        }
        // Prev was computed from start outward. From entrance backwards to start,
        // then reverse to receive current -> entrance.
        Collections.reverse(path);
        return path;
    }

    public JsonObject toJson() {
        JsonObject result = new JsonObject();
        JsonArray pts = new JsonArray();
        for (Vec3d p : nodes) {
            JsonArray xyz = new JsonArray();
            xyz.add(p.x); xyz.add(p.y); xyz.add(p.z);
            pts.add(xyz);
        }
        JsonArray edges = new JsonArray();
        for (int a = 0; a < links.size(); a++) {
            for (Edge edge : links.get(a)) {
                if (edge.target <= a) continue;
                JsonArray item = new JsonArray();
                item.add(a); item.add(edge.target); item.add(edge.distance);
                edges.add(item);
            }
        }
        result.addProperty("format", 1);
        result.add("nodes", pts);
        result.add("edges", edges);
        return result;
    }
    public static RouteGraph fromJson(JsonObject json) {
        if (json.get("format").getAsInt() != 1) throw new IllegalArgumentException("Unknown route format");
        RouteGraph graph = new RouteGraph();
        for (JsonElement el : json.getAsJsonArray("nodes")) {
            JsonArray p = el.getAsJsonArray();
            if (graph.nodes.size() >= LIMIT) throw new IllegalArgumentException("Route too large");
            graph.addNode(new Vec3d(p.get(0).getAsDouble(),p.get(1).getAsDouble(),p.get(2).getAsDouble()));
        }
        if (graph.nodes.isEmpty()) throw new IllegalArgumentException("Empty route");
        for (JsonElement el : json.getAsJsonArray("edges")) {
            JsonArray p = el.getAsJsonArray();
            int a = p.get(0).getAsInt(), b = p.get(1).getAsInt();
            double d = p.get(2).getAsDouble();
            if (a < 0 || b < 0 || a >= graph.count() || b >= graph.count() || !Double.isFinite(d) || d <= 0)
                throw new IllegalArgumentException("Invalid route edge");
            graph.connect(a,b,d);
        }
        graph.lastNode = -1; // reconnect to a nearby walked node after reload
        graph.lastSample = null;
        return graph;
    }
}
