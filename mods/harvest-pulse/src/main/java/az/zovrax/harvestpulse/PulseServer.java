package az.zovrax.harvestpulse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Server-authoritative persistence, far-field growth and progress reporting. */
public final class PulseServer implements ModInitializer {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_EDGE = 16;
    private static final int MAX_FIELDS_PER_OWNER = 8;
    private static final int MAX_TRACKED_FIELDS = 64;
    private static final int SNAPSHOT_INTERVAL_TICKS = 20;
    private static final int SELECTION_TIMEOUT_TICKS = 20 * 120;
    private final Map<UUID, Selection> pending = new HashMap<>();
    private final List<Field> fields = new ArrayList<>();
    private MinecraftServer activeServer;
    private Path dataFile;
    private long ticks;

    private record Selection(BlockPos pos, String dimension, long tick) { }

    private static final class Field {
        String id;
        UUID owner;
        String dimension;
        int minX, maxX, minZ, maxZ, minY, maxY;
        transient int mature, total;
        transient boolean validSample;
    }

    private static final class Persisted {
        Field[] fields;
    }

    @Override public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(PulsePackets.Action.ID, PulsePackets.Action.CODEC);
        PayloadTypeRegistry.playS2C().register(PulsePackets.Snapshot.ID, PulsePackets.Snapshot.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PulsePackets.Action.ID,
            (payload, context) -> receive(context.player(), payload.json()));
        ServerLifecycleEvents.SERVER_STARTED.register(this::load);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (server == activeServer) { save(); pending.clear(); activeServer = null; }
        });
        ServerTickEvents.END_SERVER_TICK.register(this::tick);
    }

    private void load(MinecraftServer server) {
        activeServer = server;
        ticks = 0;
        pending.clear();
        fields.clear();
        dataFile = server.getSavePath(WorldSavePath.ROOT).resolve("data/zovrax-harvest-pulse.json");
        try {
            if (Files.isRegularFile(dataFile)) {
                Persisted data = GSON.fromJson(Files.readString(dataFile, StandardCharsets.UTF_8), Persisted.class);
                if (data != null && data.fields != null) {
                    for (Field f : data.fields) {
                        if (fields.size() >= MAX_TRACKED_FIELDS) break;
                        if (f != null && valid(f)) fields.add(f);
                    }
                }
            }
        } catch (Exception e) { System.err.println("[HarvestPulse] Unable to load field data: " + e); }
    }

    private boolean valid(Field f) {
        return f.owner != null && f.id != null && f.id.length() <= 48 && f.dimension != null
            && f.dimension.length() < 128 && f.maxX >= f.minX && f.maxZ >= f.minZ
            && f.maxX - (long)f.minX < MAX_EDGE && f.maxZ - (long)f.minZ < MAX_EDGE
            && f.maxY - f.minY >= 0 && f.maxY - f.minY <= 2;
    }

    private void save() {
        if (dataFile == null) return;
        try {
            Files.createDirectories(dataFile.getParent());
            Path tmp = dataFile.resolveSibling("zovrax-harvest-pulse.json.tmp");
            Persisted data = new Persisted();
            data.fields = fields.toArray(Field[]::new);
            Files.writeString(tmp, GSON.toJson(data), StandardCharsets.UTF_8);
            try { Files.move(tmp, dataFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.ATOMIC_MOVE); }
            catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(tmp, dataFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) { System.err.println("[HarvestPulse] Unable to save field data: " + e); }
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
        if ("remove".equals(action)) {
            try {
                String id = data.get("id").getAsString();
                if (id.length() > 48) return;
                if (fields.removeIf(f -> f.owner.equals(owner) && f.id.equals(id))) save();
            } catch (Exception ignored) { }
            return;
        }
        if (!"corner".equals(action)) return;
        try {
            BlockPos pos = new BlockPos(data.get("x").getAsInt(),data.get("y").getAsInt(),data.get("z").getAsInt());
            if (player.squaredDistanceTo(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5) > 64.0) return;
            String dimension = player.getEntityWorld().getRegistryKey().getValue().toString();
            String id = data.has("id") ? data.get("id").getAsString() : "";
            Selection first = pending.get(owner);
            if (id.isEmpty()) {
                pending.put(owner, new Selection(pos, dimension, ticks));
                return;
            }
            pending.remove(owner);
            if (id.length() > 48 || !id.matches("[a-f0-9-]{36}")) return;
            if (first == null || ticks - first.tick() > SELECTION_TIMEOUT_TICKS ||
                !first.dimension().equals(dimension)) return;
            if (fields.size() >= MAX_TRACKED_FIELDS || fields.stream().filter(f -> f.owner.equals(owner)).count() >= MAX_FIELDS_PER_OWNER) return;
            BlockPos from = first.pos();
            if (!FieldMath.validArea(from.getX(),from.getY(),from.getZ(),pos.getX(),pos.getY(),pos.getZ())) return;
            if (fields.stream().anyMatch(f -> f.id.equals(id))) return;
            Field field = new Field();
            field.id = id;
            field.owner = owner;
            field.dimension = dimension;
            field.minX = Math.min(from.getX(),pos.getX()); field.maxX = Math.max(from.getX(),pos.getX());
            field.minZ = Math.min(from.getZ(),pos.getZ()); field.maxZ = Math.max(from.getZ(),pos.getZ());
            field.minY = Math.min(from.getY(),pos.getY()); field.maxY = Math.max(from.getY(),pos.getY());
            if (!valid(field)) return;
            fields.add(field);
            save();
        } catch (Exception ignored) { }
    }

    private static boolean crop(BlockState state) {
        Block b = state.getBlock();
        return b instanceof CropBlock || b instanceof NetherWartBlock || b instanceof SweetBerryBushBlock || b instanceof CocoaBlock;
    }

    private static boolean mature(BlockState state) {
        Block b = state.getBlock();
        if (b instanceof CropBlock c) return c.isMature(state);
        for (Property<?> p : state.getProperties()) {
            if (p instanceof IntProperty age && "age".equals(p.getName())) {
                int a = state.get(age);
                if (b instanceof SweetBerryBushBlock) return a >= 2;
                return a >= age.getValues().stream().mapToInt(Integer::intValue).max().orElse(1000);
            }
        }
        return false;
    }

    private static boolean normalTickNearby(MinecraftServer server, Field f, ServerWorld world) {
        double x = (f.minX + (double)f.maxX)/2.0, z = (f.minZ + (double)f.maxZ)/2.0;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.getEntityWorld() == world && player.squaredDistanceTo(x,player.getY(),z) < 144*144) return true;
        }
        return false;
    }

    private void tick(MinecraftServer server) {
        if (server != activeServer || (++ticks % SNAPSHOT_INTERVAL_TICKS) != 0) return;
        for (Field f : fields) {
            ServerPlayerEntity owner = server.getPlayerManager().getPlayer(f.owner);
            if (owner == null) continue;  // No background work for offline users.
            Identifier dim = Identifier.tryParse(f.dimension);
            if (dim == null) continue;
            RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, dim);
            ServerWorld world = server.getWorld(key);
            if (world == null) continue;
            int speed = Math.clamp(world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED), 0, 4096);
            double chance = FieldMath.randomTickChance(speed, SNAPSHOT_INTERVAL_TICKS);
            boolean remote = !normalTickNearby(server, f, world);
            int total = 0, mature = 0;
            BlockPos.Mutable pos = new BlockPos.Mutable();
            // Bounded to <=256 crop plots per field, one server-thread pass per second.
            for (int x=f.minX; x<=f.maxX; x++) for (int z=f.minZ; z<=f.maxZ; z++) {
                BlockPos cropPos = null;
                BlockState state = null;
                boolean soil = false;
                for (int y=f.minY; y<=f.maxY; y++) {
                    pos.set(x,y,z);
                    BlockState b = world.getBlockState(pos);
                    if (crop(b)) { cropPos = pos.toImmutable(); state = b; break; }
                    // When a planted crop has been harvested, the farmland is
                    // still one block beneath the selected crop layer.
                    boolean plantingSoil = b.getBlock() instanceof FarmlandBlock || b.isOf(Blocks.SOUL_SAND)
                        || world.getBlockState(pos.down()).getBlock() instanceof FarmlandBlock
                        || world.getBlockState(pos.down()).isOf(Blocks.SOUL_SAND);
                    if (plantingSoil) {
                        soil = true;
                        BlockPos next = b.getBlock() instanceof FarmlandBlock || b.isOf(Blocks.SOUL_SAND)
                            ? pos.up() : pos.toImmutable();
                        BlockState above = world.getBlockState(next);
                        if (crop(above)) { cropPos=next; state=above; break; }
                    }
                }
                if (state == null && !soil) continue;
                total++;
                if (state != null) {
                    // Vanilla random-tick eligibility is server-authoritative. This
                    // applies real world changes, not an optimistic growth estimate.
                    if (remote && speed > 0 && !mature(state) && world.random.nextDouble() < chance) {
                        state.randomTick(world, cropPos, world.random);
                        state = world.getBlockState(cropPos);
                    }
                    if (crop(state) && mature(state)) mature++;
                }
            }
            f.total = total; f.mature = mature; f.validSample = true;
        }
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            com.google.gson.JsonArray list = new com.google.gson.JsonArray();
            for (Field f : fields) if (f.owner.equals(player.getUuid()) && f.validSample) {
                JsonObject o = new JsonObject();
                o.addProperty("id",f.id);o.addProperty("total",f.total);o.addProperty("mature",f.mature);
                list.add(o);
            }
            if (list.size() > 0 && ServerPlayNetworking.canSend(player, PulsePackets.Snapshot.ID))
                ServerPlayNetworking.send(player,new PulsePackets.Snapshot(GSON.toJson(list)));
        }
    }
}
