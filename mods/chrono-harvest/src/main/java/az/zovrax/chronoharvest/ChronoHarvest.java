package az.zovrax.chronoharvest;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
public final class ChronoHarvest implements ModInitializer {
 public static final String ID="zovrax_chronoharvest";
 public static final ChronoHarvestConfig CONFIG=ChronoHarvestConfig.load();
 @Override public void onInitialize(){
  ServerLifecycleEvents.SERVER_STARTED.register(GrowthLedger::load);
  ServerLifecycleEvents.SERVER_STOPPING.register(GrowthLedger::save);
  ServerChunkEvents.CHUNK_LOAD.register(GrowthLedger::onChunkLoad);
  ServerTickEvents.END_SERVER_TICK.register(server->GrowthLedger.periodicSave());
  PlayerBlockBreakEvents.BEFORE.register((world,player,pos,state,blockEntity)->{
   if(world.isClient()||!(world instanceof ServerWorld sw)||!(player instanceof ServerPlayerEntity sp)||!GrowthLedger.supported(state))return true;
   var age=GrowthLedger.ageProperty(state); if(age==null)return true;
   int current=state.get(age), max=age.getValues().stream().mapToInt(Integer::intValue).max().orElse(current);
   boolean mature=current>=max;
   if(!mature&&CONFIG.requireShiftForImmatureHarvest&&!sp.isSneaking()){
    sp.sendMessage(Text.literal("ChronoHarvest: not ripe yet. Hold Shift to harvest early; Shift uproots the plant."),true);return false;
   }
   // Sneak-harvesting always performs a full vanilla break, removing the plant.
   if(sp.isSneaking()){ GrowthLedger.forget(sw,pos); return true; }
   if(CONFIG.regrowAfterHarvest){
    net.minecraft.block.Block.dropStacks(state,sw,pos,blockEntity,sp,sp.getMainHandStack());
    sw.setBlockState(pos,state.with(age,0),3); GrowthLedger.markPlanted(sw,pos); return false;
   }
   GrowthLedger.forget(sw,pos); return true;
  });
 }
}
