package az.zovrax.inventorypickup;

import net.minecraft.item.ItemStack;

/** One visible pickup toast; repeat pickups refresh its lifetime. */
public final class PickupToast {
    public final ItemStack stack;
    public final boolean experience;
    public int gain;
    public int total;
    public long createdAtMillis;

    public PickupToast(ItemStack stack, int gain, int total, long now) {
        this(stack, gain, total, now, false);
    }

    public PickupToast(ItemStack stack, int gain, int total, long now, boolean experience) {
        this.stack = stack.copyWithCount(1);
        this.experience = experience;
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
