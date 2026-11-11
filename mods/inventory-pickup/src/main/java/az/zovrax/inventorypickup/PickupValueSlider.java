package az.zovrax.inventorypickup;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Vanilla Minecraft slider with discrete steps and immediate persistence. */
public final class PickupValueSlider extends SliderWidget {
    private final String label;
    private final String unit;
    private final int min;
    private final int max;
    private final int step;
    private final IntConsumer setter;

    public PickupValueSlider(int x, int y, int width, String label, String unit,
                             int min, int max, int step, IntSupplier getter,
                             IntConsumer setter) {
        super(x, y, width, 20, Text.empty(), (getter.getAsInt() - min) / (double) (max - min));
        this.label = label;
        this.unit = unit;
        this.min = min;
        this.max = max;
        this.step = step;
        this.setter = setter;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(Text.literal(label + ": " + current() + unit));
    }

    @Override
    protected void applyValue() {
        setter.accept(current());
        InventoryPickupClient.config().save();
        updateMessage();
    }

    private int current() {
        int raw = min + (int) Math.round(value * (max - min));
        return Math.max(min, Math.min(max, min + Math.round((raw - min) / (float) step) * step));
    }
}
