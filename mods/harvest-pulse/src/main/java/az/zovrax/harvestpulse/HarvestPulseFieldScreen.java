package az.zovrax.harvestpulse;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import java.util.List;

/** Name, mode, number format, order and deletion for each registered field. */
public final class HarvestPulseFieldScreen extends Screen {
    private static final String[] MODES={"Always show", "Show only when 100% ready", "Hide"};
    private static final String[] VALUES={"Count and percent", "Percent only", "Count only"};
    private final Screen parent;
    private final String id;
    private final HarvestPulseConfig cfg=HarvestPulseClient.config();
    private TextFieldWidget nameInput;
    public HarvestPulseFieldScreen(Screen parent,String id) {
        super(Text.literal("HarvestPulse - edit field"));this.parent=parent;this.id=id;
    }
    @Override protected void init() {
        HarvestPulseConfig.Field f=cfg.byId(id);
        if(f==null){close();return;}
        int w=Math.min(290,Math.max(150,width-24)),x=(width-w)/2;
        int top=Math.max(39,(height-216)/2);
        nameInput=new TextFieldWidget(textRenderer,x,top,w,20,Text.literal("Field name"));
        nameInput.setMaxLength(32);nameInput.setText(f.name);addDrawableChild(nameInput);
        addDrawableChild(ButtonWidget.builder(Text.literal("Visibility: "+MODES[f.showMode]),b->{
            f.showMode=(f.showMode+1)%MODES.length;cfg.save();b.setMessage(Text.literal("Visibility: "+MODES[f.showMode]));
        }).dimensions(x,top+28,w,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Numbers: "+VALUES[f.valueMode]),b->{
            f.valueMode=(f.valueMode+1)%VALUES.length;cfg.save();b.setMessage(Text.literal("Numbers: "+VALUES[f.valueMode]));
        }).dimensions(x,top+53,w,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Move up"),b->{shift(-1);})
            .dimensions(x,top+83,(w-4)/2,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Move down"),b->{shift(1);})
            .dimensions(x+(w+4)/2,top+83,(w-4)/2,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Delete field"),b->{
            HarvestPulseClient.removeRemote(id);
            cfg.fields.removeIf(field->field.id.equals(id));cfg.save();
            if(client!=null)client.setScreen(new HarvestPulseScreen(null));
        }).dimensions(x,top+116,w,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Save and back"),b->close()).dimensions(x,top+144,w,20).build());
    }
    private void shift(int step) {
        List<HarvestPulseConfig.Field> fields=cfg.fields;
        for(int i=0;i<fields.size();i++)if(fields.get(i).id.equals(id)){
            int other=i+step;
            if(other>=0&&other<fields.size()){
                java.util.Collections.swap(fields,i,other);cfg.save();
            }
            break;
        }
    }
    @Override public void render(DrawContext ctx,int mouseX,int mouseY,float delta) {
        renderBackground(ctx,mouseX,mouseY,delta);
        ctx.drawCenteredTextWithShadow(textRenderer,title,width/2,10,0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,Text.literal("A bell appears when the entire field is ready"),width/2,23,0xFFB3DBAF);
        super.render(ctx,mouseX,mouseY,delta);
    }
    @Override public void close() {
        HarvestPulseConfig.Field f=cfg.byId(id);
        if(f!=null&&nameInput!=null){
            String text=nameInput.getText().trim();
            if(!text.isEmpty())f.name=text;
        }
        cfg.save();if(client!=null)client.setScreen(parent);
    }
}
