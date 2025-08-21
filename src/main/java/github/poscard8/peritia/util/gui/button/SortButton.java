package github.poscard8.peritia.util.gui.button;

import github.poscard8.peritia.client.screen.XpSourceScreen;
import github.poscard8.peritia.util.xpsource.XpSourceSortFunction;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;


@OnlyIn(Dist.CLIENT)
public class SortButton extends CompactButton
{
    public static final int WIDTH = 88;
    public static final int HEIGHT = 13;

    public XpSourceSortFunction sortFunction = XpSourceSortFunction.empty();

    public SortButton(int x, int y, OnPress press) { super(x, y, WIDTH, HEIGHT, 196, 0, XpSourceScreen.textureLocation(), 512, 256, press); }

    public XpSourceSortFunction nextSortFunction()
    {
        switch (sortFunction)
        {
            case XP_ASCENDING -> { return XpSourceSortFunction.XP_DESCENDING; }
            case XP_DESCENDING -> { return XpSourceSortFunction.DEFAULT_ORDER; }
            default -> { return XpSourceSortFunction.XP_ASCENDING; }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta)
    {
        super.render(guiGraphics, mouseX, mouseY, delta);

        float ratio = 0.75F;
        float inverse = 1 / ratio;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(ratio, ratio, ratio);

        Component text = Component.translatable("generic.peritia.xp_source_sort").append(sortFunction.getText());

        guiGraphics.drawString(font(), text, Math.round((getX() + 4) * inverse), Math.round((getY() + 4) * inverse) + 1, textColor(), false);

        guiGraphics.pose().scale(1, 1, 1);
        guiGraphics.pose().popPose();
    }

    public int textColor() { return isHovered() ? textureStyle().hoveredTextColor() : textureStyle().tertiaryTextColor(); }

}
