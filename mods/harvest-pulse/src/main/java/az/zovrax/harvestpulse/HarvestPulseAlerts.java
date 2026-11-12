package az.zovrax.harvestpulse;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

/** One-shot client-side sound and vanilla top-right toast per maturation event. */
public final class HarvestPulseAlerts {
    private HarvestPulseAlerts() { }
    public static void notifyReady(MinecraftClient mc, HarvestPulseConfig.Field field) {
        HarvestPulseConfig cfg=HarvestPulseClient.config();
        if(cfg.soundAlerts && mc.player!=null)
            mc.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(),0.65f,1.35f);
        if(cfg.toastAlerts)
            SystemToast.add(mc.getToastManager(),SystemToast.Type.NARRATOR_TOGGLE,
                Text.literal("HarvestPulse  •  Harvest ready"),
                Text.literal(field.name+"  •  "+field.mature+" / "+field.total));
    }
}
