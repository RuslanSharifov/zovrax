package az.zovrax.smartreturn;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.RaycastContext;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Client-only exploration recorder. Routes are separated by world and dimension.
 * Different cave entrances stay in different networks until the player actually
 * walks through a shared passage, at which point two networks are joined.
 */
public final class RouteRecorder {
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_CAVES=8;
    private final List<RouteGraph> caves=new ArrayList<>();
    private final ArrayDeque<Vec3d> approach=new ArrayDeque<>();
    private Object worldIdentity;
    private String worldId;
    private Path file;
    private RouteGraph active;
    private RouteGraph selected;
    private RouteGraph.Path directions;
    private boolean guidanceOn;
    private boolean reminderSent;
    private int ticks,deepTicks,daylightTicks,startedAt,lastSavedAt;
    private Vec3d lastInside;

    public boolean guiding(){return guidanceOn && directions!=null;}
    public List<Vec3d> guidance(){return directions==null?List.of():directions.points();}
    public double distance(){return directions==null?0:directions.distance();}
    public Vec3d target(){return directions==null?null:directions.exit();}
    public int caveCount(){return caves.size();}
    public int entranceCount(){return caves.stream().mapToInt(g->g.entrances().size()).sum();}

    private static Vec3d position(MinecraftClient mc){
        return new Vec3d(mc.player.getX(),mc.player.getY(),mc.player.getZ());
    }
    private static void say(MinecraftClient mc,String key,Object... args){
        if(mc.player!=null)mc.player.sendMessage(Text.translatable(key,args),true);
    }
    private static String locationId(MinecraftClient mc){
        String game=mc.getCurrentServerEntry()!=null?
            "server:"+mc.getCurrentServerEntry().address :
            mc.getServer()!=null ?
                "singleplayer:"+mc.getServer().getSaveProperties().getLevelName():
                "unknown-local-world";
        return game+"/"+mc.world.getRegistryKey().getValue();
    }
    private static Path dataFile(String id)throws Exception{
        byte[] hash=MessageDigest.getInstance("SHA-256").digest(id.getBytes(StandardCharsets.UTF_8));
        StringBuilder name=new StringBuilder();
        for(byte b:hash)name.append(String.format("%02x",b&0xff));
        return FabricLoader.getInstance().getConfigDir().resolve("zovrax-smart-return")
            .resolve("routes").resolve(name+".json");
    }
    private void changeWorld(MinecraftClient mc){
        save();
        worldIdentity=mc.world;
        worldId=null;file=null;
        caves.clear();active=null;selected=null;directions=null;
        guidanceOn=false;reminderSent=false;
        ticks=deepTicks=daylightTicks=startedAt=lastSavedAt=0;
        lastInside=null;approach.clear();
        if(mc.world==null)return;
        try {
            worldId=locationId(mc);
            file=dataFile(worldId);
            if(Files.isRegularFile(file)){
                JsonObject root=JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                if(!worldId.equals(root.get("world").getAsString()))return;
                if(root.has("caves")){
                    for(JsonElement entry:root.getAsJsonArray("caves")){
                        if(caves.size()>=MAX_CAVES)break;
                        caves.add(RouteGraph.fromJson(entry.getAsJsonObject()));
                    }
                }else if(root.has("route")){
                    // Automatic import from the 0.1.0 single-cave save format.
                    caves.add(RouteGraph.fromJson(root.getAsJsonObject("route")));
                }
            }
        }catch(Exception e){System.err.println("[SmartReturn] Could not read saved routes: "+e);}
    }

    private boolean walkable(Vec3d a,Vec3d b){
        MinecraftClient mc=MinecraftClient.getInstance();
        if(mc.world==null||mc.player==null)return false;
        if(a.distanceTo(b)<.05)return true;
        if(a.distanceTo(b)>5.5)return false;
        Vec3d from=a.add(0,.8,0),to=b.add(0,.8,0);
        return mc.world.raycast(new RaycastContext(from,to,
            RaycastContext.ShapeType.COLLIDER,RaycastContext.FluidHandling.NONE,
            mc.player)).getType()==HitResult.Type.MISS;
    }

    private void addApproach(Vec3d p){
        if(approach.isEmpty()||approach.getLast().distanceTo(p)>0.9)
            approach.addLast(p);
        while(approach.size()>200)approach.removeFirst();
    }
    private void beginCave(Vec3d pos){
        Vec3d first=approach.isEmpty()?pos:approach.getFirst();
        // An existing path is resumed only if we are physically standing
        // beside it. A close but wall-separated cave is a different network.
        RouteGraph existing=null;
        for(RouteGraph g:caves){
            int at=g.nearest(first,1.15);
            if(at>=0 && walkable(first,g.node(at))){
                existing=g;break;
            }
        }
        if(existing==null){
            if(caves.size()>=MAX_CAVES) caves.remove(0); // bounded archive
            existing=new RouteGraph(first);
            caves.add(existing);
        }else existing.reattach(first);
        active=existing;
        for(Vec3d step:approach)active.sample(step,this::walkable);
        active.sample(pos,this::walkable);
        approach.clear();
        startedAt=ticks-deepTicks;
        reminderSent=false;
        save();
    }
    private void mergeIfMet(Vec3d at){
        if(active==null)return;
        for(int i=caves.size()-1;i>=0;i--){
            RouteGraph other=caves.get(i);
            if(other==active)continue;
            int near=other.nearest(at,.85);
            if(near<0 || !walkable(at,other.node(near)))continue;
            if(active.mergeAt(other,at,this::walkable)){
                if(selected==other){selected=active;directions=null;}
                caves.remove(i);
            }
        }
    }

    public void tick(MinecraftClient mc,SmartReturnConfig config){
        if(worldIdentity!=mc.world)changeWorld(mc);
        if(mc.world==null||mc.player==null||!config.enabled)return;
        ticks++;
        if(ticks%3!=0)return; // ~6 samples/s, low-overhead
        Vec3d p=position(mc);
        BlockPos block=mc.player.getBlockPos();
        boolean daylight=mc.world.isSkyVisible(block.up());
        int roof=mc.world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
            block.getX(),block.getZ())-block.getY();
        boolean deep=!daylight && roof>=8;
        if(deep)deepTicks+=3;
        else deepTicks=0;

        if(active==null){
            if(daylight){approach.clear();addApproach(p);}
            else addApproach(p);
            if(deepTicks>=100) beginCave(p); // 5 seconds of definite cave
        }else{
            if(daylight) {
                daylightTicks+=3;
                // Record the last few steps to the real opening, not across
                // open fields. A newly found exit is only added on transition.
                if(daylightTicks<=12)active.sample(p,this::walkable);
                if(daylightTicks==30){
                    active.addExitNear(lastInside==null?p:lastInside);
                    active=null;
                    reminderSent=false;
                    say(mc,"smartreturn.message.cave_saved");
                    save();
                    approach.clear();addApproach(p);
                }
            }else{
                daylightTicks=0;
                active.sample(p,this::walkable);
                lastInside=p;
                mergeIfMet(p);
                if(config.reminders&&!reminderSent &&
                        ticks-startedAt>=config.reminderMinutes*1200){
                    reminderSent=true;
                    say(mc,"smartreturn.message.reminder");
                }
            }
        }
        if(active==null && daylight) { // remember current outdoor approach
            approach.clear();addApproach(p);
        }
        if(guidanceOn){
            if(selected==null){guidanceOn=false;directions=null;}
            else{
                directions=selected.homeward(p);
                if(directions==null){
                    guidanceOn=false;
                    say(mc,"smartreturn.message.no_connected_path");
                }else if(p.distanceTo(directions.exit())<2.6){
                    guidanceOn=false;directions=null;
                    say(mc,"smartreturn.message.arrived");
                }
            }
        }
        if(ticks-lastSavedAt>=600)save();
    }

    public void toggle(MinecraftClient mc){
        if(mc.player==null)return;
        if(guidanceOn){
            guidanceOn=false;directions=null;
            say(mc,"smartreturn.message.off");
            return;
        }
        Vec3d p=position(mc);
        RouteGraph bestGraph=null;
        RouteGraph.Path bestRoute=null;
        for(RouteGraph g:caves){
            RouteGraph.Path path=g.homeward(p);
            if(path!=null && (bestRoute==null || path.distance()<bestRoute.distance())){
                bestGraph=g;bestRoute=path;
            }
        }
        if(bestRoute==null){
            say(mc,caves.isEmpty()?"smartreturn.message.no_route":
                "smartreturn.message.no_connected_path");
            return;
        }
        selected=bestGraph;directions=bestRoute;guidanceOn=true;
        say(mc,"smartreturn.message.on");
    }
    public void forget(MinecraftClient mc){
        caves.clear();active=null;selected=null;directions=null;
        guidanceOn=false;reminderSent=false;deepTicks=daylightTicks=0;
        approach.clear();lastInside=null;
        if(file!=null)try{Files.deleteIfExists(file);}
        catch(Exception e){System.err.println("[SmartReturn] Failed to forget saved routes: "+e);}
        say(mc,"smartreturn.message.cleared");
    }
    public void save(){
        if(file==null||worldId==null||caves.isEmpty())return;
        try{
            Files.createDirectories(file.getParent());
            JsonObject root=new JsonObject();
            root.addProperty("world",worldId);
            root.addProperty("format",2);
            JsonArray list=new JsonArray();
            for(RouteGraph cave:caves)list.add(cave.toJson());
            root.add("caves",list);
            Path temp=file.resolveSibling(file.getFileName().toString()+".tmp");
            Files.writeString(temp,GSON.toJson(root),StandardCharsets.UTF_8);
            Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING);
            lastSavedAt=ticks;
        }catch(Exception e){System.err.println("[SmartReturn] Could not save routes: "+e);}
    }
}
