package az.zovrax.harvestpulse;

import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

/** Keyboard manager, terrain preview, HUD and client-only local preferences. */
public final class HarvestPulseClient implements ClientModInitializer {
    public static final String ID="zovrax_harvestpulse";
    private static final HarvestPulseConfig CONFIG=HarvestPulseConfig.load();
    private static boolean selecting;
    private static BlockPos firstCorner;
    private static String scope;
    private KeyBinding key;

    public static HarvestPulseConfig config() { return CONFIG; }
    public static boolean isSelecting() { return selecting; }
    public static BlockPos firstCorner() { return firstCorner; }
    public static boolean online(MinecraftClient mc) {
        return mc.getNetworkHandler()!=null && ClientPlayNetworking.canSend(PulsePackets.Action.ID);
    }
    private static void transmit(JsonObject j) {
        MinecraftClient mc=MinecraftClient.getInstance();
        if(online(mc)) ClientPlayNetworking.send(new PulsePackets.Action(j.toString()));
    }
    private static void sendCorner(BlockPos pos,String id) {
        JsonObject j=new JsonObject();
        j.addProperty("action","corner");j.addProperty("id",id);
        j.addProperty("x",pos.getX());j.addProperty("y",pos.getY());j.addProperty("z",pos.getZ());
        j.addProperty("limit",CONFIG.selectionLimit);
        transmit(j);
    }
    public static void removeRemote(String id) {
        JsonObject j=new JsonObject();j.addProperty("action","remove");j.addProperty("id",id);transmit(j);
    }
    public static void cancelSelection(MinecraftClient mc) {
        selecting=false;firstCorner=null;scope=null;
        JsonObject j=new JsonObject();j.addProperty("action","cancel");transmit(j);
        if(mc.player!=null)mc.player.sendMessage(Text.literal("HarvestPulse: selection cancelled."),true);
    }
    /** Farm soil clicks select the crop layer, so the server and preview agree. */
    public static BlockPos cropLevelPos(World world,BlockPos clicked) {
        return world.getBlockState(clicked).getBlock() instanceof FarmlandBlock ||
            world.getBlockState(clicked).isOf(Blocks.SOUL_SAND) ? clicked.up() : clicked;
    }
    public static boolean validPreview(BlockPos start,BlockPos end,String worldKey,String dimension) {
        if(!FieldMath.validArea(start.getX(),start.getY(),start.getZ(),
            end.getX(),end.getY(),end.getZ(),CONFIG.selectionLimit)) return false;
        int x1=Math.min(start.getX(),end.getX()),x2=Math.max(start.getX(),end.getX());
        int z1=Math.min(start.getZ(),end.getZ()),z2=Math.max(start.getZ(),end.getZ());
        int y1=Math.min(start.getY(),end.getY()),y2=Math.max(start.getY(),end.getY());
        for(HarvestPulseConfig.Field f:CONFIG.fields) {
            if(!worldKey.equals(f.worldKey)||!dimension.equals(f.dimension)) continue;
            if(FieldMath.overlaps(x1,z1,x2,z2,f.x1,f.z1,f.x2,f.z2) &&
                y2>=Math.min(f.y1,f.y2)-2 && y1<=Math.max(f.y1,f.y2)+2) return false;
        }
        return true;
    }
    public static int currentWorldCount(MinecraftClient mc) {
        String world=FieldTracker.worldKey(mc);
        return (int)CONFIG.fields.stream().filter(f->world.equals(f.worldKey)).count();
    }

    @Override public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(PulsePackets.Snapshot.ID,
            (packet,context)->context.client().execute(()->acceptSnapshot(packet.json())));
        KeyBinding.Category group=KeyBinding.Category.create(Identifier.of(ID,"main"));
        key=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.zovrax_harvestpulse.manage",
            InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_H,group));
        ClientTickEvents.END_CLIENT_TICK.register(mc->{
            FieldTracker.tick(mc);
            while(key.wasPressed()) {
                if(mc.player!=null && mc.currentScreen==null)mc.setScreen(new HarvestPulseScreen(null));
            }
        });
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
            Identifier.of(ID,"overlay"),HarvestPulseHud::render);
        WorldRenderEvents.AFTER_ENTITIES.register(FieldSelectionRenderer::render);
        UseBlockCallback.EVENT.register((player,world,hand,hit)->{
            if(!world.isClient() || !selecting || hand!=Hand.MAIN_HAND) return ActionResult.PASS;
            MinecraftClient mc=MinecraftClient.getInstance();
            if(mc.player!=player || mc.currentScreen!=null) return ActionResult.PASS;
            BlockPos pos=cropLevelPos(world,hit.getBlockPos());
            String nowScope=FieldTracker.worldKey(mc)+"|"+FieldTracker.dimension(mc);
            if(firstCorner==null) {
                firstCorner=pos.toImmutable();scope=nowScope;
                sendCorner(pos,"");
                player.sendMessage(Text.literal("HarvestPulse: first corner set. Green=valid, red=invalid. H=Cancel."),true);
            } else {
                if(!nowScope.equals(scope)||!validPreview(firstCorner,pos,FieldTracker.worldKey(mc),FieldTracker.dimension(mc))) {
                    player.sendMessage(Text.literal("HarvestPulse: red area cannot be saved. Choose within "+CONFIG.selectionLimit+"x"+CONFIG.selectionLimit+"."),true);
                    return ActionResult.FAIL;
                }
                if(currentWorldCount(mc)>=FieldMath.MAX_FIELDS) {
                    player.sendMessage(Text.literal("HarvestPulse: eight fields in this world is the maximum."),true);
                    cancelSelection(mc);return ActionResult.FAIL;
                }
                HarvestPulseConfig.Field f=new HarvestPulseConfig.Field();
                f.name="Field "+(currentWorldCount(mc)+1);
                f.worldKey=FieldTracker.worldKey(mc); f.dimension=FieldTracker.dimension(mc);
                f.x1=firstCorner.getX();f.y1=firstCorner.getY();f.z1=firstCorner.getZ();
                f.x2=pos.getX();f.y2=pos.getY();f.z2=pos.getZ();
                sendCorner(pos,f.id);
                CONFIG.fields.add(f);CONFIG.save();
                selecting=false;firstCorner=null;scope=null;
                player.sendMessage(Text.literal("HarvestPulse: field registered; waiting for full server scan."),true);
                mc.execute(()->mc.setScreen(new HarvestPulseFieldScreen(new HarvestPulseScreen(null),f.id)));
            }
            return ActionResult.FAIL; // Do not plant/harvest while choosing corners.
        });
    }

    private static void acceptSnapshot(String json) {
        MinecraftClient mc=MinecraftClient.getInstance();
        if(mc.world==null||mc.player==null) return;
        try {
            JsonArray array=JsonParser.parseString(json).getAsJsonArray();
            if(array.size()>FieldMath.MAX_FIELDS)return;
            for(var entry:array) {
                JsonObject j=entry.getAsJsonObject();
                HarvestPulseConfig.Field f=CONFIG.byId(j.get("id").getAsString());
                if(f==null||!f.worldKey.equals(FieldTracker.worldKey(mc)))continue;
                boolean was=f.sampled && FieldMath.ready(f.mature,f.total);
                f.mature=Math.max(0,j.get("mature").getAsInt());
                f.total=Math.max(f.mature,j.get("total").getAsInt());
                f.loaded=true;f.sampled=true;f.lastSampleMillis=System.currentTimeMillis();
                boolean is=FieldMath.ready(f.mature,f.total);
                // Notify only on a transition to fully mature, never on 99% or
                // a repeated server snapshot. Trigger once per ready-cycle.
                if(is && !f.notifiedReady) {
                    f.readySinceMillis=System.currentTimeMillis();
                    HarvestPulseAlerts.notifyReady(mc,f);
                    f.notifiedReady=true;
                } else if(!is) f.notifiedReady=false;
            }
        } catch(Exception e) { System.err.println("[HarvestPulse] Invalid snapshot: "+e); }
    }
    public static void beginSelection(MinecraftClient mc) {
        if(mc.player==null || mc.world==null)return;
        if(!online(mc)) {
            mc.player.sendMessage(Text.literal("HarvestPulse requires installation on the server as well."),false);return;
        }
        if(currentWorldCount(mc)>=FieldMath.MAX_FIELDS) {
            mc.player.sendMessage(Text.literal("HarvestPulse: eight fields already selected in this world."),false);return;
        }
        selecting=true;firstCorner=null;scope=null;
        mc.setScreen(null);
        mc.player.sendMessage(Text.literal("HarvestPulse: right-click two corners. H opens Cancel. Blue=existing field."),false);
    }
}
