package az.zovrax.harvestpulse;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Native Minecraft slider with saved preference changes. */
public final class HarvestPulseSlider extends SliderWidget {
    private final String label;
    private final int min,max,step;
    private final IntConsumer change;
    public HarvestPulseSlider(int x,int y,int width,String label,int min,int max,int step,
                               IntSupplier value,IntConsumer change) {
        super(x,y,width,20,Text.empty(),(value.getAsInt()-min)/(double)(max-min));
        this.label=label;this.min=min;this.max=max;this.step=step;this.change=change;
        updateMessage();
    }
    private int current() {
        int raw=min+(int)Math.round(value*(max-min));
        return Math.clamp(min+Math.round((raw-min)/(float)step)*step,min,max);
    }
    @Override protected void updateMessage() {setMessage(Text.literal(label+": "+current()+"%"));}
    @Override protected void applyValue() {
        change.accept(current());HarvestPulseClient.config().save();updateMessage();
    }
}
