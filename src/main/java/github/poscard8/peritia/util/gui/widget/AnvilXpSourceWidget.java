package github.poscard8.peritia.util.gui.widget;

import github.poscard8.peritia.client.screen.XpSourceScreen;
import github.poscard8.peritia.xpsource.type.AnvilXpSource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class AnvilXpSourceWidget extends XpSourceWidget<AnvilXpSource>
{
    protected final ItemDisplay leftDisplay;
    protected final ItemDisplay rightDisplay;

    public AnvilXpSourceWidget(AnvilXpSource xpSource, int x, int y)
    {
        super(xpSource, x, y);

        this.leftDisplay = ItemDisplay.anvilLeft(xpSource, x + 17, y + 25);
        this.rightDisplay = ItemDisplay.anvilRight(xpSource, x + 58, y + 25);
    }

    @Override
    protected void renderTexts(GuiGraphics guiGraphics)
    {
        super.renderTexts(guiGraphics);

        float ratio = 0.75F;
        float inverse = 1 / ratio;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(ratio, ratio, ratio);

        Component text = Component.translatable("xp_source.peritia.anvil");
        guiGraphics.drawString(font(), text, Math.round((getX() + 4) * inverse), Math.round((getY() + 17) * inverse), primaryTextColor(), false);

        guiGraphics.pose().scale(1, 1, 1);
        guiGraphics.pose().popPose();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics)
    {
        super.renderBg(guiGraphics);

        int plusTextureX = 180;
        int plusTextureY = isHovered() ? 219 : 210;
        guiGraphics.blit(XpSourceScreen.textureLocation(), getX() + 41, getY() + 29, plusTextureX, plusTextureY, 9, 9, 512, 256);
    }

    @Override
    public void addSelf(XpSourceScreen screen)
    {
        super.addSelf(screen);
        screen.$addWidget(leftDisplay());
        screen.$addWidget(rightDisplay());
    }

    @Override
    public void removeSelf(XpSourceScreen screen)
    {
        super.removeSelf(screen);
        screen.$removeWidget(leftDisplay());
        screen.$removeWidget(rightDisplay());
    }

    @Override
    public void setPos(int x, int y)
    {
        int deltaX = x - getX();
        int deltaY = y - getY();

        super.setPos(x, y);
        leftDisplay().changePos(deltaX, deltaY);
        rightDisplay().changePos(deltaX, deltaY);
    }

    public ItemDisplay leftDisplay() { return leftDisplay; }

    public ItemDisplay rightDisplay() { return rightDisplay; }

}
