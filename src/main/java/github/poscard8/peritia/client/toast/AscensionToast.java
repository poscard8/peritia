package github.poscard8.peritia.client.toast;

import github.poscard8.peritia.util.gui.PeritiaToast;
import github.poscard8.peritia.util.text.PeritiaTexts;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AscensionToast implements Toast, PeritiaToast
{
    public static final long DISPLAY_TIME_MILLISECONDS = 3000;

    public AscensionToast() {}

    @Override
    public Visibility render(GuiGraphics guiGraphics, ToastComponent toasts, long milliseconds)
    {
        renderBg(guiGraphics);
        guiGraphics.renderItem(ascensionSystem().icon().getDefaultInstance(), 8, 8);
        guiGraphics.drawString(font(), PeritiaTexts.ascensionSuccessToast(), 30, 12, 0xFFAA00);

        return milliseconds >= DISPLAY_TIME_MILLISECONDS ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public int getTextureYStart() { return 96; }

}
