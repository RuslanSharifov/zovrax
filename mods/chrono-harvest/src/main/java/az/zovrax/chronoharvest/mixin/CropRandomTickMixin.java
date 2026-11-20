package az.zovrax.chronoharvest.mixin;
import az.zovrax.chronoharvest.GrowthLedger;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin({CropBlock.class,NetherWartBlock.class,SweetBerryBushBlock.class,CocoaBlock.class})
public abstract class CropRandomTickMixin {
 @Inject(method="randomTick",at=@At("HEAD"),cancellable=true)
 private void chronoHarvest$elapsedGrowth(BlockState state,ServerWorld world,BlockPos pos,Random random,CallbackInfo ci){
  if(GrowthLedger.applyElapsedGrowth(world,pos))ci.cancel();
 }
}
