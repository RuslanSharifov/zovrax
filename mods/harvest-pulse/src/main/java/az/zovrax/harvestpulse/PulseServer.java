package az.zovrax.harvestpulse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server-owned farm registry and incremental crop sampling.
 * Uses a global, bounded work budget; never visits an entire 128x128 field in one tick.
 * Random crop growth is an approximation of vanilla random-tick selections, and
 * is deliberately stopped when nearby players can make vanilla random ticks occur.
 */
public final class PulseServer implements ModInitializer {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_EDGE = FieldMath.MAX_EDGE;
    private static final int MAX_FIELDS_PER_OWNER = FieldMath.MAX_FIELDS;
    private static final int MAX_TRACKED_FIELDS = 64;
    private static final int MAX_TOTAL_PLOTS = 131072;
    private static final int MAX_SAMPLES_PER_TICK = 384;
    private static final int SNAPSHOT_INTERVAL_TICKS = 20;
    private static final int SELECTION_TIMEOUT_TICKS = 20 * 120;
    private final Map<UUID, Selection> pending = new HashMap<>();
    private final List<Field> fields = new ArrayList<>();
    private MinecraftServer activeServer;
    private Path dataFile;
    private long ticks;
    private int nextField;

    private record Selection(BlockPos pos, String dimension, int limit, long tick) { }
    private static final class Field {
        String id;
        UUID owner;
        String dimension;
        int minX, maxX, minZ, maxZ, minY, maxY;
        transient int mature, total;
        transient boolean validSample;
        transient int cursor, pendingMature, pendingTotal;
        transient long[] lastVisit;
        transient long lastScanTick;
        int area() { return (maxX-minX+1)*(maxZ-minZ+1); }
    }
    private static final class Persisted { Field[] fields; }

    @Override public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(PulsePackets.Action.ID, PulsePackets.Action.CODEC);
        PayloadTypeRegistry.playS2C().register(PulsePackets.Snapshot.ID, PulsePackets.Snapshot.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PulsePackets.Action.ID,
            (payload, context) -> receive(context.player(), payload.json()));
        ServerLifecycleEvents.SERVER_STARTED.register(this::load);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (server == activeServer) { save(); pending.clear(); fields.clear(); activeServer = null; }
        });
        ServerTickEvents.END_SERVER_TICK.register(this::tick);
    }

    private void load(MinecraftServer server) {
        activeServer = server;
        ticks = 0;
        nextField = 0;
        pending.clear(); fields.clear();
        dataFile = server.getSavePath(WorldSavePath.ROOT).resolve("data/zovrax-harvest-pulse.json");
        int area = 0;
        try {
            if (Files.isRegularFile(dataFile)) {
                Persisted data = GSON.fromJson(Files.readString(dataFile, StandardCharsets.UTF_8), Persisted.class);
                if (data != null && data.fields != null) {
                    for (Field f : data.fields) {
                        if (fields.size() >= MAX_TRACKED_FIELDS) break;
                        if (f == null || !valid(f) || area + f.area() > MAX_TOTAL_PLOTS) continue;
                        // Ensure no duplicate IDs or overlapping areas from a corrupt/edited save.
                        if (fields.stream().anyMatch(a -> a.id.equals(f.id) || samePlot(a,f))) continue;
                        fields.add(f); area += f.area();
                    }
                }
            }
        } catch (Exception e) { System.err.println("[HarvestPulse] Unable to load field data: " + e); }
    }

    private static boolean valid(Field f) {
        return f.owner != null && f.id != null && f.id.matches("[a-f0-9-]{36}")
            && f.dimension != null && f.dimension.length() < 128
            && f.maxX >= f.minX && f.maxZ >= f.minZ
            && f.maxX - (long)f.minX < MAX_EDGE && f.maxZ - (long)f.minZ < MAX_EDGE
            && f.maxY >= f.minY && f.maxY - f.minY <= FieldMath.MAX_VERTICAL_SPAN;
    }
    private static boolean samePlot(Field a, Field b) {
        return a.owner.equals(b.owner) && a.dimension.equals(b.dimension)
            && a.minY <= b.maxY + 2 && a.maxY + 2 >= b.minY
            && FieldMath.overlaps(a.minX,a.minZ,a.maxX,a.maxZ,b.minX,b.minZ,b.maxX,b.maxZ);
    }
    private void save() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            Path temp = dataFile.resolveSibling("zovrax-harvest-pulse.json.tmp");
            Persisted data = new Persisted(); data.fields = fields.toArray(Field[]::new);
            Files.writeString(temp, GSON.toJson(data), StandardCharsets.UTF_8);
            try { Files.move(temp, dataFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.ATOMIC_MOVE); }
            catch (java.nio.file.AtomicMoveNotSupportedException ex) {
                Files.move(temp, dataFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) { System.err.println("[HarvestPulse] Unable to save field data: " + e); }
    }
    private void reject(ServerPlayerEntity player, String id, String reason) {
        if (id == null || !id.matches("[a-f0-9-]{36}")) return;
        JsonObject entry = new JsonObject();
        entry.addProperty("id", id); entry.addProperty("error", reason);
        JsonArray array = new JsonArray();array.add(entry);
        if (ServerPlayNetworking.canSend(player, PulsePackets.Snapshot.ID))
            ServerPlayNetworking.send(player,new PulsePackets.Snapshot(GSON.toJson(array)));
    }
    private void receive(ServerPlayerEntity player, String json) {
        if (activeServer == null || json.length() > 2048) return;
        JsonObject data;
        try { data = JsonParser.parseString(json).getAsJsonObject(); }
        catch (Exception e) { return; }
        String action;
        try { action = data.get("action").getAsString(); }
        catch (Exception e) { return; }
        UUID owner = player.getUuid();
        if ("cancel".equals(action)) { pending.remove(owner); return; }
        if ("remove".equals(action)) {
            try {
                String id=data.get("id").getAsString();
                if (id.length()>48) return;
                if (fields.removeIf(f -> f.owner.equals(owner) && f.id.equals(id))) save();
            } catch (Exception ignored) { }
            return;
        }
        if (!"corner".equals(action)) return;
        String id="";
        try {
            id = data.get("id").getAsString();
            BlockPos pos=new BlockPos(data.get("x").getAsInt(),data.get("y").getAsInt(),data.get("z").getAsInt());
            if (player.squaredDistanceTo(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)>64.0) {
                reject(player,id,"Corner must be within 8 blocks"); return;
            }
            String dimension=player.getEntityWorld().getRegistryKey().getValue().toString();
            if (id.isEmpty()) {
                int limit=data.has("limit")?data.get("limit").getAsInt():16;
                if (limit!=16 && limit!=32 && limit!=64 && limit!=128) return;
                pending.put(owner,new Selection(pos,dimension,limit,ticks));
                return;
            }
            Selection first=pending.remove(owner);
            if (!id.matches("[a-f0-9-]{36}")) return;
            if (first==null || ticks-first.tick()>SELECTION_TIMEOUT_TICKS || !dimension.equals(first.dimension())) {
                reject(player,id,"First corner expired or in another dimension"); return;
            }
            BlockPos from=first.pos();
            if (!FieldMath.validArea(from.getX(),from.getY(),from.getZ(),pos.getX(),pos.getY(),pos.getZ(),first.limit())) {
                reject(player,id,"Field exceeds selected size or 32-block slope limit"); return;
            }
            if (fields.size()>=MAX_TRACKED_FIELDS || fields.stream().filter(f->f.owner.equals(owner)).count()>=MAX_FIELDS_PER_OWNER) {
                reject(player,id,"Server/player field count limit reached"); return;
            }
            Field f = new Field(); f.id=id; f.owner=owner; f.dimension=dimension;
            f.minX=Math.min(from.getX(),pos.getX()); f.maxX=Math.max(from.getX(),pos.getX());
            f.minZ=Math.min(from.getZ(),pos.getZ()); f.maxZ=Math.max(from.getZ(),pos.getZ());
            f.minY=Math.min(from.getY(),pos.getY()); f.maxY=Math.max(from.getY(),pos.getY());
            if (!valid(f) || fields.stream().anyMatch(x->x.id.equals(f.id) || samePlot(x,f))) {
                reject(player,id,"Field overlaps an existing field"); return;
            }
            int area=fields.stream().mapToInt(Field::area).sum();
            if (area+f.area()>MAX_TOTAL_PLOTS) {
                reject(player,id,"Server's total tracked-plot safety budget exceeded"); return;
            }
            fields.add(f); save();
        } catch (Exception ex) { reject(player,id,"Invalid field data"); }
    }

    private static boolean crop(BlockState state) {
        Block b=state.getBlock();
        return b instanceof CropBlock || b instanceof NetherWartBlock || b instanceof SweetBerryBushBlock || b instanceof CocoaBlock;
    }
    private static boolean mature(BlockState state) {
        Block b=state.getBlock();
        if (b instanceof CropBlock c) return c.isMature(state);
        for (Property<?> p:state.getProperties()) if (p instanceof IntProperty age && "age".equals(p.getName())) {
            int value=state.get(age);
            if (b instanceof SweetBerryBushBlock) return value>=2;
            return value>=age.getValues().stream().mapToInt(Integer::intValue).max().orElse(1000);
        }
        return false;
    }
    private static boolean nearVanillaTickRange(MinecraftServer server,Field f,ServerWorld world) {
        for (ServerPlayerEntity p:server.getPlayerManager().getPlayerList()) {
            if (p.getEntityWorld()!=world) continue;
            double dx=Math.max(0,Math.max(f.minX-p.getX(),p.getX()-f.maxX));
            double dz=Math.max(0,Math.max(f.minZ-p.getZ(),p.getZ()-f.maxZ));
            if (dx*dx+dz*dz<144*144) return true;
        }
        return false;
    }

    private void processColumn(Field f, ServerWorld world,boolean remote,int randomSpeed) {
        int area=f.area();
        if (area<1) return;
        if (f.lastVisit==null || f.lastVisit.length!=area) f.lastVisit=new long[area];
        int depth=f.maxZ-f.minZ+1;
        int index=f.cursor;
        int x=f.minX+index/depth,z=f.minZ+index%depth;
        BlockPos.Mutable pos=new BlockPos.Mutable();
        BlockPos foundPos=null; BlockState found=null; boolean soil=false;
        // An uneven field can have up to 32 blocks of elevation difference.
        // Sample nearby elevations around the selected corner band, but never the whole world column.
        int fromY=Math.max(world.getBottomY(),f.minY-2);
        int toY=Math.min(world.getTopYInclusive(),f.maxY+2);
        for (int y=toY;y>=fromY;y--) {
            pos.set(x,y,z);
            BlockState state=world.getBlockState(pos);
            if (crop(state)) { foundPos=pos.toImmutable();found=state;break; }
            if (state.getBlock() instanceof FarmlandBlock || state.isOf(Blocks.SOUL_SAND)) {
                soil=true;
                BlockPos above=pos.up();
                BlockState top=world.getBlockState(above);
                if (crop(top)) {foundPos=above;found=top;break;}
            }
        }
        if (found != null || soil) f.pendingTotal++;
        if (found!=null) {
            // The interval is per plot, not per field. Don't "catch up" for time
            // when the owner was offline or after a server restart.
            long previous=f.lastVisit[index];
            int elapsed=(int)Math.clamp(ticks-previous,0,1200);
            if (remote && previous!=0 && randomSpeed>0 && !mature(found)
                && world.random.nextDouble()<FieldMath.randomTickChance(randomSpeed,elapsed)) {
                found.randomTick(world,foundPos,world.random);
                found=world.getBlockState(foundPos);
            }
            if (crop(found) && mature(found)) f.pendingMature++;
        }
        f.lastVisit[index]=ticks;
        f.cursor++;
        if (f.cursor>=area) {
            f.cursor=0;
            f.total=f.pendingTotal;
            f.mature=f.pendingMature;
            f.pendingTotal=0;f.pendingMature=0;
            f.validSample=true;
            f.lastScanTick=ticks;
        }
    }

    private void tick(MinecraftServer server) {
        if (server!=activeServer) return;
        ticks++;
        if (!fields.isEmpty()) {
            // Even when every tracked field is 128x128, a single server tick
            // never scans more than MAX_SAMPLES_PER_TICK crop positions globally.
            int visited=0, idle=0;
            while (visited<MAX_SAMPLES_PER_TICK && idle<fields.size()) {
                if (nextField>=fields.size()) nextField=0;
                Field f=fields.get(nextField++);
                ServerPlayerEntity owner=server.getPlayerManager().getPlayer(f.owner);
                if (owner==null) {idle++;continue;}
                Identifier dimension=Identifier.tryParse(f.dimension);
                if (dimension==null) {idle++;continue;}
                RegistryKey<World> key=RegistryKey.of(RegistryKeys.WORLD,dimension);
                ServerWorld world=server.getWorld(key);
                if (world==null) {idle++;continue;}
                idle=0; visited++;
                int speed=Math.clamp(world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED),0,4096);
                processColumn(f,world,!nearVanillaTickRange(server,f,world),speed);
            }
        }
        if (ticks%SNAPSHOT_INTERVAL_TICKS!=0) return;
        for (ServerPlayerEntity player:server.getPlayerManager().getPlayerList()) {
            JsonArray array=new JsonArray();
            for (Field f:fields) if (f.owner.equals(player.getUuid()) && f.validSample) {
                JsonObject data=new JsonObject();
                data.addProperty("id",f.id);
                data.addProperty("mature",f.mature);data.addProperty("total",f.total);
                array.add(data);
            }
            if (array.size()>0 && ServerPlayNetworking.canSend(player,PulsePackets.Snapshot.ID))
                ServerPlayNetworking.send(player,new PulsePackets.Snapshot(GSON.toJson(array)));
        }
    }
}
