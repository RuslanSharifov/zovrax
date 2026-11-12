package az.zovrax.smartreturn;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Vanilla-style controls: no optional config-screen mod needed. */
public final class SmartReturnSettings extends Screen {
    private final Screen parent;
    private final SmartReturnConfig config = SmartReturnClient.config();
    public SmartReturnSettings(Screen parent) {
        super(Text.literal("SmartReturn Settings"));
        this.parent = parent;
    }
    @Override protected void init() {
        int w = Math.min(290, width - 36);
        int x = (width - w)/2;
        int top = Math.max(35, (height - 226)/2);
        addDrawableChild(ButtonWidget.builder(Text.empty(), b -> {
            config.enabled = !config.enabled;
            b.setMessage(Text.literal("Quiet recording: " + (config.enabled ? "ON":"OFF")));
            config.save();
        }).dimensions(x,top,w,20).build()).setMessage(Text.literal(
            "Quiet recording: " + (config.enabled ? "ON":"OFF")));
        addDrawableChild(ButtonWidget.builder(Text.empty(), b -> {
            config.displayMode = (config.displayMode + 1)%3;
            b.setMessage(Text.literal(displayLabel()));
            config.save();
        }).dimensions(x,top+27,w,20).build()).setMessage(Text.literal(displayLabel()));
        addDrawableChild(ButtonWidget.builder(Text.empty(), b -> {
            config.reminders = !config.reminders;
            b.setMessage(Text.literal("One quiet reminder: " + (config.reminders ? "ON":"OFF")));
            config.save();
        }).dimensions(x,top+54,w,20).build()).setMessage(Text.literal(
            "One quiet reminder: " + (config.reminders ? "ON":"OFF")));
        addDrawableChild(ButtonWidget.builder(Text.empty(), b -> {
            config.reminderMinutes = config.reminderMinutes >= 10 ? 1 : config.reminderMinutes + 1;
            b.setMessage(Text.literal("Reminder after: " + config.reminderMinutes + " min"));
            config.save();
        }).dimensions(x,top+81,w,20).build()).setMessage(Text.literal(
            "Reminder after: " + config.reminderMinutes + " min"));
        addDrawableChild(ButtonWidget.builder(Text.empty(), b -> {
            config.showDistance = !config.showDistance;
            b.setMessage(Text.literal("Show remaining distance: " + (config.showDistance ? "ON":"OFF")));
            config.save();
        }).dimensions(x,top+108,w,20).build()).setMessage(Text.literal(
            "Show remaining distance: " + (config.showDistance ? "ON":"OFF")));
        addDrawableChild(ButtonWidget.builder(Text.literal("Forget saved cave route"), b -> {
            if (client != null) SmartReturnClient.routes().forget(client);
        }).dimensions(x,top+141,w,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"),b -> close())
            .dimensions(x,top+177,w,20).build());
    }
    private String displayLabel() {
        return "Directions: " + switch(config.displayMode) {
            case 1 -> "Mini compass";
            case 2 -> "3D arrows + mini compass";
            default -> "3D arrows";
        };
    }
    @Override public void render(DrawContext ctx,int mouseX,int mouseY,float delta) {
        renderBackground(ctx,mouseX,mouseY,delta);
        ctx.drawCenteredTextWithShadow(textRenderer,title,width/2,Math.max(12,(height-226)/2-20),0xffffffff);
        super.render(ctx,mouseX,mouseY,delta);
    }
    @Override public void close() {
        config.save();
        if (client != null) client.setScreen(parent);
    }
}
