package az.zovrax.biometracker;

import java.util.Comparator;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class BiomeTrackerStatsScreen extends Screen {
    private final Screen parent;
    public BiomeTrackerStatsScreen(Screen parent){super(Text.literal("Biome playtime statistics"));this.parent=parent;}

    @Override public void render(DrawContext ctx,int mx,int my,float delta){
        renderBackground(ctx,mx,my,delta);
        ctx.drawCenteredTextWithShadow(textRenderer,title,width/2,14,0xFFFFFFFF);
        var entries=BiomeTrackerClient.stats().snapshot().entrySet().stream()
            .sorted(Map.Entry.<String,Long>comparingByValue(Comparator.reverseOrder())).toList();
        int y=38;
        int shown=0;
        for(var entry:entries){
            if(shown++>=Math.max(1,(height-85)/14))break;
            String[] parts=entry.getKey().split("\\|",2);
            String biome=parts.length==2?parts[1]:entry.getKey();
            String dimension=parts.length==2?parts[0]:"";
            String line=biome+"  ·  "+BiomeStats.formatTicks(entry.getValue())+"  ·  "+dimension;
            ctx.drawTextWithShadow(textRenderer,line,Math.max(12,(width-textRenderer.getWidth(line))/2),y,0xFFE9F0FA);
            y+=14;
        }
        if(entries.isEmpty())ctx.drawCenteredTextWithShadow(textRenderer,Text.literal("No biome time recorded yet."),width/2,height/2,0xFFAAAAAA);
        super.render(ctx,mx,my,delta);
    }
    @Override public boolean shouldPause(){return false;}
    @Override public void close(){BiomeTrackerClient.stats().save();if(client!=null)client.setScreen(parent);}
}
