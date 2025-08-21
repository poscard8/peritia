package github.poscard8.peritia.mixin;

import github.poscard8.peritia.util.text.ColorGradient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
@SuppressWarnings("ALL")
public abstract class ItemStackMixin
{
    @Shadow
    @Nullable
    abstract CompoundTag getTag();

    @Inject(method = "getRarity", at = @At("TAIL"), cancellable = true)
    private void peritia$getRarity(CallbackInfoReturnable<Rarity> ci)
    {
        @Nullable ColorGradient gradient = ColorGradient.ofNbt(getTag());
        if (gradient != null) ci.setReturnValue(gradient.asRarity());
    }

}
