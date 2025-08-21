package github.poscard8.peritia.mixin.client;

import github.poscard8.peritia.network.PeritiaClientHandler;
import github.poscard8.peritia.util.text.insertion.Insertion;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
@OnlyIn(Dist.CLIENT)
@SuppressWarnings("ALL")
public abstract class ScreenMixin
{
    @Inject(method = "handleComponentClicked", at = @At("HEAD"), cancellable = true)
    private void peritia$handleComponentClicked(@Nullable Style style, CallbackInfoReturnable<Boolean> ci)
    {
        if (style == null) return;
        Insertion insertion = Insertion.tryLoad(style.getInsertion());
        if (insertion == null) return;

        insertion.accept(PeritiaClientHandler.getInstance());
        ci.setReturnValue(true);
    }



}
