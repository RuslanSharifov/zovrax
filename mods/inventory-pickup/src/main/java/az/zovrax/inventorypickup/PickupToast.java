package az.zovrax.inventorypickup;

import net.minecraft.item.ItemStack;

/** The currently visible notification for a single item variant. */
public final class PickupToast {
    public final ItemStack stack;
    public int gain;
    public int total;
    public long createdAtMillis;

    public PickupToast(ItemStack stack, int gain, int total, long now) {
        this.stack = stack.copyWithCount(1);
        this.gain = gain;
        this.total = total;
        this.createdAtMillis = now;
    }

    public void accumulate(int amount, int newTotal, long now) {
        gain += amount;
        total = newTotal;
        createdAtMillis = now;
    }
}
