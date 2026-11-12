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
 * Authoritative player-owned field registry. One small, round-robin scan budget per
 * server tick prevents huge 128x128 farms from blocking every game tick. The
 * progress bar only receives completed, consistent full-field measurements.
 */
public final class PulseServer implements ModInitializer {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_FIELDS_PER_OWNER = 8;
    private static final int MAX_TRACKED_FIELDS = 64;
    private static final int SNAPSHOT_TICKS = 20;
    private static final int MAX_COLUMNS_PER_TICK = 192;
    private static final int MAX_COLUMNS_PER_FIELD_PASS = 48;
    private static final int SELECTION_TIMEOUT = 20 * 120;
    private final Map<UUID, Selection> pending = new HashMap<>();
    private final List<Field> fields = new ArrayList<>();
    private MinecraftServer activeServer;
    private Path dataFile;
    private long ticks;
    private int nextField;

    private record Selection(BlockPos pos, String dimension, long tick) { }

    private static final class Field {
        String id;
        UUID owner;
        String dimension;
        int minX, maxX, minZ, maxZ, minY, maxY;
        transient int mature, total, cursor, scanningMature, scanningTotal;
        transient boolean validSample;
        transient long lastScanTick;
        int columns() { return (maxX - minX + 1) * (maxZ - minZ + 1); }
    }
    private static final class Persisted { Field[] fields; }

    @Override public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(PulsePackets.Action.ID, PulsePackets.Action.CODEC);
        PayloadTypeRegistry.playS2C().register(PulsePackets.Snapshot.ID, PulsePackets.Snapshot.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PulsePackets.Action.ID,
            (payload, context) -> receive(context.player(),payload.json()));
        ServerLifecycleEvents.SERVER_STARTED.register(this::load);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if(server == activeServer) { save(); pending.clear(); fields.clear(); activeServer = null; }
        });
        ServerTickEvents.END_SERVER_TICK.register(this::tick);
    }

    private void load(MinecraftServer server) {
        activeServer = server;
        ticks = 0;
        nextField = 0;
        pending.clear();
        fields.clear();
        dataFile = server.getSavePath(WorldSavePath.ROOT).resolve("data/zovrax-harvest-pulse.json");
        try {
            if(Files.isRegularFile(dataFile)) {
                Persisted data=JSON.fromJson(Files.readString(dataFile,StandardCharsets.UTF_8),Persisted.class);
                if(data!=null && data.fields!=null) for(Field f:data.fields) {
                    if(fields.size()>=MAX_TRACKED_FIELDS) break;
                    if(f!=null && valid(f)) fields.add(f);
                }
            }
        } catch(Exception e) { System.err.println("[HarvestPulse] Field data read failed: "+e); }
    }
    private boolean valid(Field f) {
        return f.owner!=null && f.id!=null && f.id.length()<=48 && f.dimension!=null
            && f.dimension.length()<128 && FieldMath.validArea(f.minX,f.minY,f.minZ,f.maxX,f.maxY,f.maxZ);
    }
    private void save() {
        if(dataFile==null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            Path temp=dataFile.resolveSibling("zovrax-harvest-pulse.json.tmp");
            Persisted data=new Persisted(); data.fields=fields.toArray(Field[]::new);
            Files.writeString(temp,JSON.toJson(data),StandardCharsets.UTF_8);
            try { Files.move(temp,dataFile,java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE); }
            catch(java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(temp,dataFile,java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch(Exception e) { System.err.println("[HarvestPulse] Field data write failed: "+e); }
    }

    private void receive(ServerPlayerEntity player,String json) {
        if(activeServer==null||json==null||json.length()>2048) return;
        JsonObject data;
        try { data=JsonParser.parseString(json).getAsJsonObject(); }
        catch(Exception e) { return; }
        String action;
        try { action=data.get("action").getAsString(); }
        catch(Exception e) { return; }
        UUID owner=player.getUuid();
        if("cancel".equals(action)) { pending.remove(owner); return; }
        if("remove".equals(action)) {
            try {
                String id=data.get("id").getAsString();
                if(id.length()>48) return;
                if(fields.removeIf(f->f.owner.equals(owner)&&f.id.equals(id))) save();
            } catch(Exception ignored) { }
            return;
        }
        if(!"corner".equals(action)) return;
        try {
            BlockPos pos=new BlockPos(data.get("x").getAsInt(),data.get("y").getAsInt(),data.get("z").getAsInt());
            if(player.squaredDistanceTo(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)>64) return;
            String dimension=player.getEntityWorld().getRegistryKey().getValue().toString();
            String id=data.has("id")?data.get("id").getAsString():"";
            if(id.isEmpty()) { pending.put(owner,new Selection(pos,dimension,ticks)); return; }
            Selection first=pending.remove(owner);
            int limit=data.has("limit")?data.get("limit").getAsInt():16;
            if(!FieldMath.validLimit(limit)||!id.matches("[a-f0-9-]{36}")||first==null||
                ticks-first.tick()>SELECTION_TIMEOUT||!first.dimension().equals(dimension)) return;
            BlockPos from=first.pos();
            if(!FieldMath.validArea(from.getX(),from.getY(),from.getZ(),pos.getX(),pos.getY(),pos.getZ(),limit)) return;
            if(fields.size()>=MAX_TRACKED_FIELDS||fields.stream().filter(f->f.owner.equals(owner)).count()>=MAX_FIELDS_PER_OWNER) return;
            if(fields.stream().anyMatch(f->f.id.equals(id))) return;
            int minX=Math.min(from.getX(),pos.getX()),maxX=Math.max(from.getX(),pos.getX());
            int minZ=Math.min(from.getZ(),pos.getZ()),maxZ=Math.max(from.getZ(),pos.getZ());
            int minY=Math.min(from.getY(),pos.getY()),maxY=Math.max(from.getY(),pos.getY());
            // Prevent duplicate ownership of the same plot; adjacent borders are allowed.
            for(Field f:fields) if(f.owner.equals(owner)&&f.dimension.equals(dimension)
                && FieldMath.overlaps(minX,minZ,maxX,maxZ,f.minX,f.minZ,f.maxX,f.maxZ)
                && maxY>=f.minY-2 && minY<=f.maxY+2) return;
            Field field=new Field();
            field.id=id;field.owner=owner;field.dimension=dimension;
            field.minX=minX;field.maxX=maxX;field.minZ=minZ;field.maxZ=maxZ;
            field.minY=minY;field.maxY=maxY;
            if(!valid(field)) return;
            fields.add(field);
            save();
        } catch(Exception ignored) { }
    }

    private static boolean isCrop(BlockState s) {
        Block b=s.getBlock();
        return b instanceof CropBlock || b instanceof NetherWartBlock || b instanceof SweetBerryBushBlock || b instanceof CocoaBlock;
    }
    private static boolean isMature(BlockState s) {
        Block b=s.getBlock();
        if(b instanceof CropBlock crop) return crop.isMature(s);
        for(Property<?> p:s.getProperties()) if(p instanceof IntProperty age && "age".equals(p.getName())) {
            int value=s.get(age);
            if(b instanceof SweetBerryBushBlock) return value>=2;
            return value>=age.getValues().stream().mapToInt(Integer::intValue).max().orElse(1000);
        }
        return false;
    }
    private static boolean nearby(MinecraftServer server,ServerWorld world,int x,int z) {
        for(ServerPlayerEntity player:server.getPlayerManager().getPlayerList()) {
            if(player.getEntityWorld()!=world) continue;
            double dx=player.getX()-(x+.5),dz=player.getZ()-(z+.5);
            if(dx*dx+dz*dz<128*128) return true;
        }
        return false;
    }
    private static RegistryKey<World> dimensionKey(Field f) {
        Identifier id=Identifier.tryParse(f.dimension);
        return id==null?null:RegistryKey.of(RegistryKeys.WORLD,id);
    }
    private void tick(MinecraftServer server) {
        if(server!=activeServer) return;
        ticks++;
        if(!fields.isEmpty()) {
            int remaining=MAX_COLUMNS_PER_TICK;
            // Fair round-robin across tracked fields; never scans 16k blocks in one tick.
            for(int visited=0;visited<fields.size() && remaining>0;visited++) {
                Field f=fields.get(nextField++ % fields.size());
                if(server.getPlayerManager().getPlayer(f.owner)==null) continue;
                RegistryKey<World> key=dimensionKey(f);
                ServerWorld world=key==null?null:server.getWorld(key);
                if(world==null) continue;
                int portion=Math.min(MAX_COLUMNS_PER_FIELD_PASS,remaining);
                for(int n=0;n<portion;n++) {
                    scanColumn(server,world,f);
                    remaining--;
                }
            }
        }
        if(ticks%SNAPSHOT_TICKS!=0) return;
        for(ServerPlayerEntity player:server.getPlayerManager().getPlayerList()) {
            JsonArray list=new JsonArray();
            for(Field f:fields) if(f.owner.equals(player.getUuid()) && f.validSample) {
                JsonObject o=new JsonObject();
                o.addProperty("id",f.id);
                o.addProperty("total",f.total);
                o.addProperty("mature",f.mature);
                o.addProperty("scanning",f.cursor!=0);
                list.add(o);
            }
            if(list.size()>0 && ServerPlayNetworking.canSend(player,PulsePackets.Snapshot.ID))
                ServerPlayNetworking.send(player,new PulsePackets.Snapshot(JSON.toJson(list)));
        }
    }
    private void scanColumn(MinecraftServer server,ServerWorld world,Field f) {
        int width=f.maxX-f.minX+1;
        int x=f.minX+(f.cursor%width);
        int z=f.minZ+(f.cursor/width);
        // Chunk reads are intentionally batched. No permanent force-load tickets.
        BlockPos.Mutable pos=new BlockPos.Mutable();
        BlockPos cropPos=null;
        BlockState state=null;
        boolean farmland=false;
        for(int y=f.minY;y<=f.maxY;y++) {
            pos.set(x,y,z);
            BlockState current=world.getBlockState(pos);
            if(isCrop(current)) { cropPos=pos.toImmutable(); state=current; break; }
            if(current.getBlock() instanceof FarmlandBlock || current.isOf(Blocks.SOUL_SAND)) farmland=true;
            BlockState below=world.getBlockState(pos.down());
            if(below.getBlock() instanceof FarmlandBlock || below.isOf(Blocks.SOUL_SAND)) farmland=true;
            // Clicking farmland selects the soil, while the plant lives above it.
            if(current.getBlock() instanceof FarmlandBlock || current.isOf(Blocks.SOUL_SAND)) {
                BlockState above=world.getBlockState(pos.up());
                if(isCrop(above)) { cropPos=pos.up().toImmutable(); state=above; break; }
            }
        }
        if(state!=null||farmland) {
            f.scanningTotal++;
            if(state!=null) {
                if(!isMature(state) && !nearby(server,world,x,z)) {
                    int speed=Math.clamp(world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED),0,4096);
                    int elapsed=(int)Math.clamp(ticks-f.lastScanTick,1L,24000L);
                    // At most a bounded number of missed ticks, using actual crop
                    // randomTick() and respecting randomTickSpeed=0 and growth rules.
                    double expected=Math.min(4.0,(double)speed*elapsed/4096d);
                    int attempts=(int)expected;
                    if(world.random.nextDouble()<expected-attempts) attempts++;
                    for(int i=0;i<attempts && !isMature(state);i++) {
                        state.randomTick(world,cropPos,world.random);
                        state=world.getBlockState(cropPos);
                    }
                }
                if(isCrop(state)&&isMature(state)) f.scanningMature++;
            }
        }
        f.cursor++;
        if(f.cursor>=f.columns()) {
            f.total=f.scanningTotal;
            f.mature=f.scanningMature;
            f.validSample=true;
            f.lastScanTick=ticks;
            f.cursor=0;
            f.scanningTotal=0;
            f.scanningMature=0;
        }
    }
}
