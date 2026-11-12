package az.zovrax.harvestpulse;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** The field list keeps editing operations separate from Minecraft interactions. */
public final class HarvestPulseScreen extends Screen {
    private final Screen parent;
    private final HarvestPulseConfig cfg=HarvestPulseClient.config();
    private int page;
    public HarvestPulseScreen(Screen parent) { super(Text.literal("HarvestPulse - fields")); this.parent=parent; }

    @Override protected void init() {
        int w=Math.min(270, Math.max(145,width-24));
        int x=(width-w)/2;
        int top=Math.max(29,(height-235)/2);
        addDrawableChild(ButtonWidget.builder(Text.literal("Track new field (select 2 corners)"),b->
            HarvestPulseClient.beginSelection(client)).dimensions(x,top,w,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("HUD: "+(cfg.enabled?"ON":"OFF")),b->{
            cfg.enabled=!cfg.enabled;cfg.save();b.setMessage(Text.literal("HUD: "+(cfg.enabled?"ON":"OFF")));
        }).dimensions(x,top+23,w,20).build());
        for(int row=0;row<4;row++) {
            int i=page*4+row;
            if(i>=cfg.fields.size())break;
            HarvestPulseConfig.Field f=cfg.fields.get(i);
            addDrawableChild(ButtonWidget.builder(Text.literal((i+1)+". "+f.name+"  (Edit)"),b->{
                if(client!=null)client.setScreen(new HarvestPulseFieldScreen(this,f.id));
            }).dimensions(x,top+50+row*22,w,20).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("< Previous"),b->{page--;clearAndInit();})
            .dimensions(x,top+142,(w-4)/2,20).build()).active=page>0;
        addDrawableChild(ButtonWidget.builder(Text.literal("Next >"),b->{page++;clearAndInit();})
            .dimensions(x+(w+4)/2,top+142,(w-4)/2,20).build()).active=(page+1)*4<cfg.fields.size();
        addDrawableChild(ButtonWidget.builder(Text.literal("Move HUD"),b->{if(client!=null)client.setScreen(new HarvestPulsePositionScreen(this));})
            .dimensions(x,top+165,w,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Scale: "+cfg.scale+"%  (+25)"),b->{
            cfg.scale=cfg.scale==200?50:Math.min(200,cfg.scale+25);cfg.save();b.setMessage(Text.literal("Scale: "+cfg.scale+"%  (+25)"));
        }).dimensions(x,top+187,(w-4)/2,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("BG: "+cfg.backgroundOpacity+"%  (+25)"),b->{
            cfg.backgroundOpacity=(cfg.backgroundOpacity+25)%125;cfg.save();b.setMessage(Text.literal("BG: "+cfg.backgroundOpacity+"%  (+25)"));
        }).dimensions(x+(w+4)/2,top+187,(w-4)/2,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"),b->close()).dimensions(x,top+210,w,20).build());
    }

    @Override public void render(DrawContext ctx,int mouseX,int mouseY,float delta) {
        renderBackground(ctx,mouseX,mouseY,delta);
        ctx.drawCenteredTextWithShadow(textRenderer,title,width/2,10,0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,Text.literal("8 fields max / 16x16 each · server mod required"),width/2,21,0xFFAACBB4);
        super.render(ctx,mouseX,mouseY,delta);
    }
    @Override public void close() { cfg.save();if(client!=null)client.setScreen(parent); }
}
