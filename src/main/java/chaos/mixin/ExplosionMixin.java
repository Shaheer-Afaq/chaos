package chaos.mixin;

import net.minecraft.world.explosion.ExplosionImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExplosionImpl.class)
public abstract class ExplosionMixin {
    @Inject(method = "shouldDestroyBlocks", at = @At("HEAD"), cancellable = true)
    private void preventBlockDestruction(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
