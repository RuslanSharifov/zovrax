package az.zovrax.harvestpulse;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Interactive HUD editor slider; saves on drag, no need to open a second settings screen. */
public final class HarvestPulseSlider extends SliderWidget {
    private final String label;
    private final int min, max, step;
    private final IntConsumer setter;
    public HarvestPulseSlider(int x,int y,int width,String label,int min,int max,int step,
                              IntSupplier getter,IntConsumer setter) {
        super(x,y,width,20,Text.empty(),(getter.getAsInt()-min)/(double)(max-min));
        this.label=label; this.min=min;this.max=max;this.step=step;this.setter=setter;
        updateMessage();
    }
    private int current() {
        int raw=min+(int)Math.round(value*(max-min));
        return Math.clamp(min+Math.round((raw-min)/(float)step)*step,min,max);
    }
    @Override protected void updateMessage() {setMessage(Text.literal(label+": "+current()+"%"));}
    @Override protected void applyValue() {
        setter.accept(current());
        HarvestPulseClient.config().save();
        updateMessage();
    }
}
