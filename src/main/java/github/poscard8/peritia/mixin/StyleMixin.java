package github.poscard8.peritia.mixin;

import github.poscard8.peritia.util.text.insertion.ColorGradientInsertion;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Style.class)
@SuppressWarnings("ALL")
public abstract class StyleMixin
{
    @Shadow
    @Nullable
    abstract String getInsertion();

    @Inject(method = "getColor", at = @At("RETURN"), cancellable = true)
    private void peritia$getColor(CallbackInfoReturnable<TextColor> ci)
    {
        ColorGradientInsertion insertion = ColorGradientInsertion.tryLoad(getInsertion());
        if (insertion == null) return;

        @Nullable TextColor textColor = insertion.gradient().getTextColor();
        ci.setReturnValue(textColor);
    }

}
