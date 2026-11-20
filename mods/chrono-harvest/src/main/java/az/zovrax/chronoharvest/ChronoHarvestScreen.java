package az.zovrax.chronoharvest;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
public final class ChronoHarvestScreen extends Screen {
 private final Screen parent;
 public ChronoHarvestScreen(Screen p){super(Text.literal("ChronoHarvest Settings"));parent=p;}
 @Override protected void init(){
  int w=Math.min(300,width-24),x=(width-w)/2,y=Math.max(36,(height-150)/2);
  addDrawableChild(ButtonWidget.builder(label("Mod enabled",ChronoHarvest.CONFIG.enabled),b->{ChronoHarvest.CONFIG.enabled=!ChronoHarvest.CONFIG.enabled;ChronoHarvest.CONFIG.save();b.setMessage(label("Mod enabled",ChronoHarvest.CONFIG.enabled));}).dimensions(x,y,w,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Growth time / stage: "+ChronoHarvest.CONFIG.secondsPerStage+" sec"),b->{int[] v={60,120,180,240,300,420,600,900,1200,1800,2400,3600};int n=60;for(int a:v)if(a>ChronoHarvest.CONFIG.secondsPerStage){n=a;break;}ChronoHarvest.CONFIG.secondsPerStage=n;ChronoHarvest.CONFIG.save();b.setMessage(Text.literal("Growth time / stage: "+n+" sec"));}).dimensions(x,y+25,w,20).build());
  addDrawableChild(ButtonWidget.builder(label("Require Shift for early harvest",ChronoHarvest.CONFIG.requireShiftForImmatureHarvest),b->{ChronoHarvest.CONFIG.requireShiftForImmatureHarvest=!ChronoHarvest.CONFIG.requireShiftForImmatureHarvest;ChronoHarvest.CONFIG.save();b.setMessage(label("Require Shift for early harvest",ChronoHarvest.CONFIG.requireShiftForImmatureHarvest));}).dimensions(x,y+50,w,20).build());
  addDrawableChild(ButtonWidget.builder(label("Replant after harvest",ChronoHarvest.CONFIG.regrowAfterHarvest),b->{ChronoHarvest.CONFIG.regrowAfterHarvest=!ChronoHarvest.CONFIG.regrowAfterHarvest;ChronoHarvest.CONFIG.save();b.setMessage(label("Replant after harvest",ChronoHarvest.CONFIG.regrowAfterHarvest));}).dimensions(x,y+75,w,20).build());
  addDrawableChild(ButtonWidget.builder(Text.literal("Done"),b->close()).dimensions(x,y+110,w,20).build());
 }
 private static Text label(String s,boolean v){return Text.literal(s+": "+(v?"ON":"OFF"));}
 @Override public void render(DrawContext c,int mx,int my,float d){renderBackground(c,mx,my,d);c.drawCenteredTextWithShadow(textRenderer,title,width/2,14,0xFFFFFFFF);c.drawCenteredTextWithShadow(textRenderer,Text.literal("Elapsed time is applied when crop chunks load."),width/2,26,0xFFB8D7B4);super.render(c,mx,my,d);}
 @Override public void close(){ChronoHarvest.CONFIG.save();if(client!=null)client.setScreen(parent);}
}
