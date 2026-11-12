package az.zovrax.dayclock;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

/** Native slider with immediate save, no special config GUI dependency. */
public final class DayClockSlider extends SliderWidget {
    private final String name;
    private final int min, max, step;
    private final IntConsumer setter;

    public DayClockSlider(int x, int y, int w, String name, int min, int max, int step,
                          IntSupplier getter, IntConsumer setter) {
        super(x, y, w, 20, Text.empty(), (getter.getAsInt()-min)/(double)(max-min));
        this.name=name; this.min=min; this.max=max; this.step=step; this.setter=setter;
        updateMessage();
    }

    private int current() {
        int raw = min + (int)Math.round(value * (max-min));
        return Math.clamp(min + Math.round((raw-min)/(float)step)*step, min, max);
    }

    @Override protected void updateMessage() {
        setMessage(Text.literal(name+": "+current()+"%"));
    }

    @Override protected void applyValue() {
        setter.accept(current());
        DayClockClient.config().save();
        updateMessage();
    }
}
