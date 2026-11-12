package az.zovrax.smartreturn;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import java.util.List;

/** Tiny, optional direction strip; displayed ONLY when the player requests help. */
public final class SmartReturnHud {
    private SmartReturnHud() { }

    public static void render(DrawContext ctx, RenderTickCounter counter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        SmartReturnConfig settings = SmartReturnClient.config();
        RouteRecorder recorder = SmartReturnClient.routes();
        if (!settings.enabled || !settings.compass() || !recorder.guiding()
                || mc.player == null || mc.options.hudHidden || mc.currentScreen != null) return;
        List<Vec3d> route = recorder.guidance();
        if (route.isEmpty()) return;

        Vec3d location = new Vec3d(mc.player.getX(),mc.player.getY(),mc.player.getZ());
        Vec3d waypoint = route.get(0);
        for (Vec3d p : route) {
            if (p.distanceTo(location) > 2.25) { waypoint = p; break; }
        }
        double dx = waypoint.x - location.x;
        double dz = waypoint.z - location.z;
        double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
        float delta = MathHelper.wrapDegrees((float)(targetYaw - mc.player.getYaw()));
        String arrow;
        if (delta >= -22.5 && delta < 22.5) arrow = "↑";
        else if (delta >= 22.5 && delta < 67.5) arrow = "↗";
        else if (delta >= 67.5 && delta < 112.5) arrow = "→";
        else if (delta >= 112.5 && delta < 157.5) arrow = "↘";
        else if (delta <= -22.5 && delta > -67.5) arrow = "↖";
        else if (delta <= -67.5 && delta > -112.5) arrow = "←";
        else if (delta <= -112.5 && delta > -157.5) arrow = "↙";
        else arrow = "↓";

        double heightChange = waypoint.y - location.y;
        String vertical = heightChange > 1.1 ? " / UP" : heightChange < -1.1 ? " / DOWN" : "";
        String distance = "";
        if (settings.showDistance) {
            double total = 0;
            Vec3d cursor = location;
            for (Vec3d node : route) {
                total += cursor.distanceTo(node);
                cursor = node;
            }
            distance = "  ~" + Math.round(total) + "m";
        }
        String label = "RETURN  " + arrow + vertical + distance;
        int w = mc.textRenderer.getWidth(label)+18;
        int x = (ctx.getScaledWindowWidth()-w)/2;
        int y = 18;
        ctx.fill(x,y,x+w,y+24,0xB30D2427);
        ctx.fill(x,y,x+3,y+24,0xFF54E5C2);
        ctx.drawTextWithShadow(mc.textRenderer,label,x+10,y+8,0xFFDCFDF4);
    }
}
