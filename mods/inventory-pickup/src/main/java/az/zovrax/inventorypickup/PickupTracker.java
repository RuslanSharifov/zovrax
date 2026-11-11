package az.zovrax.inventorypickup;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Tracks net item gains without packet injection, mixins or inventory mutation.
 *
 * It aggregates equivalent item+component stacks across ALL inventory slots before
 * comparing snapshots. Shuffling stacks, moving between hotbar/inventory, and
 * shifting between slots should therefore not create false pickup notifications.
 * Works with both ground pickups and chest/other container transfers once synced.
 */
public final class PickupTracker {
    private final List<Entry> previous = new ArrayList<>();
    private boolean initialized = false;
    private Object lastPlayer;
    private Object lastWorld;

    public void reset() {
        previous.clear();
        initialized = false;
        lastPlayer = null;
        lastWorld = null;
    }

    public void tick(MinecraftClient client, BiConsumer<ItemGain, Integer> gained) {
        if (client.player == null || client.world == null) {
            reset();
            return;
        }
        if (client.player != lastPlayer || client.world != lastWorld) {
            previous.clear();
            initialized = false;
            lastPlayer = client.player;
            lastWorld = client.world;
        }

        PlayerInventory inventory = client.player.getInventory();
        List<Entry> current = collect(inventory);

        // Login, respawn or world change should not generate giant "pickup" bursts.
        if (initialized) {
            for (Entry latest : current) {
                int before = count(previous, latest.stack);
                int difference = latest.count - before;
                if (difference > 0) {
                    gained.accept(new ItemGain(latest.stack.copyWithCount(1), difference), latest.count);
                }
            }
        }

        initialized = true;
        previous.clear();
        previous.addAll(current);
    }

    static List<Entry> collect(PlayerInventory inventory) {
        List<Entry> result = new ArrayList<>();
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack.isEmpty()) continue;
            Entry existing = find(result, stack);
            if (existing == null) {
                result.add(new Entry(stack.copyWithCount(1), stack.getCount()));
            } else {
                existing.count += stack.getCount();
            }
        }
        return result;
    }

    private static Entry find(List<Entry> list, ItemStack target) {
        for (Entry entry : list) {
            if (ItemStack.areItemsAndComponentsEqual(entry.stack, target)) return entry;
        }
        return null;
    }

    private static int count(List<Entry> list, ItemStack target) {
        Entry entry = find(list, target);
        return entry == null ? 0 : entry.count;
    }

    static final class Entry {
        final ItemStack stack;
        int count;

        Entry(ItemStack stack, int count) {
            this.stack = stack;
            this.count = count;
        }
    }

    public record ItemGain(ItemStack stack, int amount) {
    }
}
