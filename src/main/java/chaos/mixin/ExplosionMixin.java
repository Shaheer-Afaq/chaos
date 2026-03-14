package chaos.mixin;

import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class ExplosionMixin {
    @Inject(method = "getBlastResistance", at = @At("HEAD"), cancellable = true)
    private void preventBlockDestruction(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(100000000.0f);
    }
}
