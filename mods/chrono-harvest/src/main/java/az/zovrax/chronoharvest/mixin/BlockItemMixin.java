package az.zovrax.chronoharvest.mixin;
import az.zovrax.chronoharvest.GrowthLedger;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
 @Inject(method="place",at=@At("RETURN"))
 private void chronoHarvest$recordPlanting(ItemPlacementContext context,CallbackInfoReturnable<ActionResult> cir){
  if(!cir.getReturnValue().isAccepted())return;World w=context.getWorld();if(!(w instanceof ServerWorld sw))return;
  BlockState s=w.getBlockState(context.getBlockPos());if(GrowthLedger.supported(s))GrowthLedger.markPlanted(sw,context.getBlockPos());
 }
}
