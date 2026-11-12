package az.zovrax.harvestpulse;

import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

/** Client handles selection/input/HUD; only server creates and grows fields. */
public final class HarvestPulseClient implements ClientModInitializer {
    public static final String ID = "zovrax_harvestpulse";
    private static final HarvestPulseConfig CONFIG = HarvestPulseConfig.load();
    private static boolean selecting;
    private static BlockPos firstCorner;
    private static String scope;
    private KeyBinding key;
    public static HarvestPulseConfig config() { return CONFIG; }

    public static boolean online(MinecraftClient mc) {
        return mc.getNetworkHandler() != null && ClientPlayNetworking.canSend(PulsePackets.Action.ID);
    }
    private static void transmit(String json) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (online(mc)) ClientPlayNetworking.send(new PulsePackets.Action(json));
    }
    private static void select(BlockPos pos, String id) {
        JsonObject j = new JsonObject();
        j.addProperty("action","corner");j.addProperty("id",id);
        j.addProperty("x",pos.getX());j.addProperty("y",pos.getY());j.addProperty("z",pos.getZ());
        transmit(j.toString());
    }
    public static void removeRemote(String id) {
        JsonObject j = new JsonObject();j.addProperty("action","remove");j.addProperty("id",id);
        transmit(j.toString());
    }

    @Override public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(PulsePackets.Snapshot.ID, (packet, context) ->
            context.client().execute(() -> acceptSnapshot(packet.json())));
        KeyBinding.Category group=KeyBinding.Category.create(Identifier.of(ID,"main"));
        key=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.zovrax_harvestpulse.manage",
            InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H,group));
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            FieldTracker.tick(mc);
            while (key.wasPressed()) {
                selecting=false;firstCorner=null;
                if (mc.player != null && mc.currentScreen == null) mc.setScreen(new HarvestPulseScreen(null));
            }
        });
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
            Identifier.of(ID,"overlay"),HarvestPulseHud::render);
        UseBlockCallback.EVENT.register((player,world,hand,hit)->{
            if (!world.isClient() || !selecting || hand != Hand.MAIN_HAND) return ActionResult.PASS;
            MinecraftClient mc=MinecraftClient.getInstance();
            if (mc.player != player || mc.currentScreen != null) return ActionResult.PASS;
            BlockPos clicked=hit.getBlockPos();
            BlockPos pos=world.getBlockState(clicked).getBlock() instanceof FarmlandBlock ? clicked.up() : clicked;
            String current=FieldTracker.worldKey(mc)+"|"+FieldTracker.dimension(mc);
            if (firstCorner==null) {
                firstCorner=pos.toImmutable();scope=current;
                select(pos,"");
                player.sendMessage(Text.literal("HarvestPulse: corner 1. Right-click opposite corner."),true);
            } else {
                if(!current.equals(scope)||!FieldMath.validArea(firstCorner.getX(),firstCorner.getY(),firstCorner.getZ(),
                    pos.getX(),pos.getY(),pos.getZ())) {
                    player.sendMessage(Text.literal("HarvestPulse: 16x16 maximum, max height difference 2. Retry corner 2."),true);
                    return ActionResult.FAIL;
                }
                if(CONFIG.fields.size()>=FieldMath.MAX_FIELDS) {
                    player.sendMessage(Text.literal("HarvestPulse: maximum 8 fields per player."),true);
                    selecting=false;firstCorner=null;return ActionResult.FAIL;
                }
                HarvestPulseConfig.Field f=new HarvestPulseConfig.Field();
                f.name="Field "+(CONFIG.fields.size()+1);
                f.worldKey=FieldTracker.worldKey(mc);
                f.dimension=FieldTracker.dimension(mc);
                f.x1=firstCorner.getX();f.y1=firstCorner.getY();f.z1=firstCorner.getZ();
                f.x2=pos.getX();f.y2=pos.getY();f.z2=pos.getZ();
                select(pos,f.id);
                CONFIG.fields.add(f);CONFIG.save();
                selecting=false;firstCorner=null;
                player.sendMessage(Text.literal("HarvestPulse: field submitted. Waiting for server crop data."),true);
                mc.execute(() -> mc.setScreen(new HarvestPulseFieldScreen(new HarvestPulseScreen(null),f.id)));
            }
            // Cancel vanilla use, e.g. planting, while choosing corners.
            return ActionResult.FAIL;
        });
    }

    private static void acceptSnapshot(String json) {
        MinecraftClient mc=MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;
        try {
            JsonArray array=JsonParser.parseString(json).getAsJsonArray();
            if (array.size()>FieldMath.MAX_FIELDS) return;
            for (var entry:array) {
                JsonObject j=entry.getAsJsonObject();
                HarvestPulseConfig.Field f=CONFIG.byId(j.get("id").getAsString());
                if (f==null || !f.worldKey.equals(FieldTracker.worldKey(mc))) continue;
                f.mature=Math.max(0,j.get("mature").getAsInt());
                f.total=Math.max(f.mature,j.get("total").getAsInt());
                f.loaded=true;f.sampled=true;f.lastSampleMillis=System.currentTimeMillis();
            }
        } catch (Exception ignored) { }
    }
    public static void beginSelection(MinecraftClient mc) {
        if(mc.player==null||mc.world==null)return;
        if(!online(mc)) {
            mc.player.sendMessage(Text.literal("HarvestPulse requires the mod on the server too (singleplayer included)."),false);
            return;
        }
        if(CONFIG.fields.size()>=FieldMath.MAX_FIELDS) {
            mc.player.sendMessage(Text.literal("HarvestPulse: 8-field limit reached."),false);return;
        }
        selecting=true;firstCorner=null;scope=null;
        mc.setScreen(null);
        mc.player.sendMessage(Text.literal("HarvestPulse: right-click two field corners. H cancels."),false);
    }
}
