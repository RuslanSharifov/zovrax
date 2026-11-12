package az.zovrax.chromaoutline;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

final class OutlineSlider extends SliderWidget {
    private final String label;
    private final int min;
    private final int max;
    private final int step;
    private final IntConsumer setter;
    private final String unit;

    OutlineSlider(int x, int y, int width, String label, int min, int max, int step,
                  IntSupplier getter, IntConsumer setter, String unit) {
        super(x, y, width, 20, Text.empty(), (getter.getAsInt() - min) / (double) (max - min));
        this.label = label;
        this.min = min;
        this.max = max;
        this.step = step;
        this.setter = setter;
        this.unit = unit;
        updateMessage();
    }

    private int selected() {
        int raw = min + (int) Math.round(value * (max - min));
        return Math.clamp(min + Math.round((raw - min) / (float) step) * step, min, max);
    }

    @Override protected void updateMessage() {
        setMessage(Text.literal(label + ": " + selected() + unit));
    }

    @Override protected void applyValue() {
        setter.accept(selected());
        ChromaOutlineClient.config().save();
        updateMessage();
    }
}
