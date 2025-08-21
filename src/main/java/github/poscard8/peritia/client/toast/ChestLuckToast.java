package github.poscard8.peritia.client.toast;

import github.poscard8.peritia.util.gui.PeritiaToast;
import github.poscard8.peritia.util.text.PeritiaTexts;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChestLuckToast implements Toast, PeritiaToast
{
    public static final long DISPLAY_TIME_MILLISECONDS = 2000;

    protected final Item icon;
    protected final int rolls;

    public ChestLuckToast(Item icon, int rolls)
    {
        this.icon = icon;
        this.rolls = rolls;
    }

    @Override
    public Visibility render(GuiGraphics guiGraphics, ToastComponent toasts, long milliseconds)
    {
        renderBg(guiGraphics);
        guiGraphics.renderItem(icon.getDefaultInstance(), 8, 8);

        guiGraphics.drawString(font(), PeritiaTexts.chestLuckPopup(), 30, 7, 0xFFFFFF);
        guiGraphics.drawString(font(), PeritiaTexts.nLoot(rolls), 30, 18, 0xFFFFFF);

        return milliseconds >= DISPLAY_TIME_MILLISECONDS ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public int getTextureYStart() { return 64; }

}
