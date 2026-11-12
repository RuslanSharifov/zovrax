package az.zovrax.harvestpulse;

import com.google.gson.JsonParser;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
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
    private static int firstLimit = 16;
    private KeyBinding key;
    public static HarvestPulseConfig config() { return CONFIG; }
    public static boolean selecting() { return selecting; }
    public static BlockPos firstCorner() { return firstCorner; }
    public static int selectionLimit() { return firstLimit; }
    public static BlockPos hoverCorner(MinecraftClient mc) {
        if (!(mc.crosshairTarget instanceof BlockHitResult hit) || mc.world == null) return null;
        BlockPos p=hit.getBlockPos();
        return mc.world.getBlockState(p).getBlock() instanceof FarmlandBlock ? p.up() : p;
    }
    public static boolean overlapsExisting(MinecraftClient mc, BlockPos first, BlockPos second) {
        String world=FieldTracker.worldKey(mc), dimension=FieldTracker.dimension(mc);
        for (HarvestPulseConfig.Field field:CONFIG.fields) {
            if (!field.worldKey.equals(world) || !field.dimension.equals(dimension)) continue;
            int minY=Math.min(first.getY(),second.getY()), maxY=Math.max(first.getY(),second.getY());
            if (minY>Math.max(field.y1,field.y2)+2 || maxY+2<Math.min(field.y1,field.y2)) continue;
            if (FieldMath.overlaps(first.getX(),first.getZ(),second.getX(),second.getZ(),
                field.x1,field.z1,field.x2,field.z2)) return true;
        }
        return false;
    }
    public static boolean validSecond(MinecraftClient mc, BlockPos second) {
        return firstCorner!=null && second!=null && scope!=null
            && scope.equals(FieldTracker.worldKey(mc)+"|"+FieldTracker.dimension(mc))
            && FieldMath.validArea(firstCorner.getX(),firstCorner.getY(),firstCorner.getZ(),
                second.getX(),second.getY(),second.getZ(),firstLimit)
            && !overlapsExisting(mc,firstCorner,second);
    }
    public static void cancelSelection() {
        if (selecting) transmit("{\"action\":\"cancel\"}");
        selecting=false;firstCorner=null;scope=null;
    }
    public static void drawSelectionBanner(DrawContext ctx,MinecraftClient mc) {
        if (!selecting || mc.currentScreen!=null || mc.player==null) return;
        BlockPos hovered=hoverCorner(mc);
        boolean second=firstCorner!=null;
        String status = !second ? "Choose FIRST corner" : hovered==null ? "Aim at SECOND corner"
            : validSecond(mc,hovered) ? "GREEN - valid / right click" : "RED - too large, slope or overlap";
        String line="HarvestPulse  ["+firstLimit+"x"+firstLimit+"]  "+status+"  |  H: manage / cancel";
        int x=(ctx.getScaledWindowWidth()-mc.textRenderer.getWidth(line))/2;
        x=Math.max(6,x);
        int w=mc.textRenderer.getWidth(line)+12;
        ctx.fill(x-6,4,x+w,23,0xB5102018);
        ctx.drawTextWithShadow(mc.textRenderer,line,x,10, !second?0xFFEBD99A:validSecond(mc,hovered)?0xFF89EBA7:0xFFFF7979);
    }

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
        j.addProperty("limit",firstLimit);
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
                if (mc.player != null && mc.currentScreen == null) mc.setScreen(new HarvestPulseScreen(null));
            }
        });
        net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents.AFTER_ENTITIES.register(PulseRegionRender::render);
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
                firstCorner=pos.toImmutable();scope=current;firstLimit=CONFIG.selectionLimit;
                select(pos,"");
                player.sendMessage(Text.literal("HarvestPulse: corner 1. Right-click opposite corner."),true);
            } else {
                if (!validSecond(mc,pos)) {
                    player.sendMessage(Text.literal("HarvestPulse: RED selection (limit, 32-block height, or existing area). Choose another corner or press H to cancel."),true);
                    return ActionResult.FAIL;
                }
                if(CONFIG.fields.size()>=FieldMath.MAX_FIELDS) {
                    player.sendMessage(Text.literal("HarvestPulse: maximum 8 fields per player."),true);
                    cancelSelection();return ActionResult.FAIL;
                }
                HarvestPulseConfig.Field f=new HarvestPulseConfig.Field();
                f.name="Field "+(CONFIG.fields.size()+1);
                f.worldKey=FieldTracker.worldKey(mc);
                f.dimension=FieldTracker.dimension(mc);
                f.x1=firstCorner.getX();f.y1=firstCorner.getY();f.z1=firstCorner.getZ();
                f.x2=pos.getX();f.y2=pos.getY();f.z2=pos.getZ();
                select(pos,f.id);
                CONFIG.fields.add(f);CONFIG.save();
                selecting=false;firstCorner=null;scope=null;
                player.sendMessage(Text.literal("HarvestPulse: field submitted. Server will confirm the region."),true);
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
                if (j.has("error")) {
                    String reason=j.get("error").getAsString();
                    CONFIG.fields.remove(f); CONFIG.save();
                    mc.player.sendMessage(Text.literal("HarvestPulse: field rejected - "+reason),false);
                    continue;
                }
                boolean wasReady=f.hasReadyState && f.previouslyReady;
                f.mature=Math.max(0,j.get("mature").getAsInt());
                f.total=Math.max(f.mature,j.get("total").getAsInt());
                f.loaded=true;f.sampled=true;f.lastSampleMillis=System.currentTimeMillis();
                boolean ready=FieldMath.ready(f.mature,f.total);
                if (ready && !wasReady) {
                    f.animationStartMillis=System.currentTimeMillis();
                    if (CONFIG.notifyToast) SystemToast.add(mc.getToastManager(),SystemToast.Type.PERIODIC_NOTIFICATION,
                        Text.literal("HarvestPulse - Harvest ready!"),Text.literal(f.name+" · "+f.mature+"/"+f.total));
                    if (CONFIG.notifySound) mc.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(),0.7f,1.25f);
                }
                f.previouslyReady=ready;f.hasReadyState=true;
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
        selecting=true;firstCorner=null;scope=null;firstLimit=CONFIG.selectionLimit;
        mc.setScreen(null);
        mc.player.sendMessage(Text.literal("HarvestPulse: right-click two corners. H opens manager with Cancel button."),false);
    }
}
