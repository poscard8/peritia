package github.poscard8.peritia.util.gui.widget;

import github.poscard8.peritia.client.screen.XpSourceScreen;
import github.poscard8.peritia.util.xpsource.ResourceInputs;
import github.poscard8.peritia.xpsource.type.ChestXpSource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class ChestXpSourceWidget extends XpSourceWidget<ChestXpSource>
{
    protected final ItemDisplay display;
    protected boolean singular = true;

    public ChestXpSourceWidget(ChestXpSource xpSource, int x, int y)
    {
        super(xpSource, x, y);
        this.display = ItemDisplay.chest(xpSource, x + 37, y + 25);

        ResourceInputs inputs = xpSource.inputs();
        int count = 0;

        for (ResourceLocation chestKey : clientHandler().gameContext().lootTableContext().chestKeys(false))
        {
            if (inputs.test(chestKey)) count++;
            if (count > 1)
            {
                singular = false;
                break;
            }
        }
    }

    @Override
    protected void renderTexts(GuiGraphics guiGraphics)
    {
        super.renderTexts(guiGraphics);

        float ratio = 0.75F;
        float inverse = 1 / ratio;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(ratio, ratio, ratio);

        Component text = Component.translatable(getTranslationKey());
        guiGraphics.drawString(font(), text, Math.round((getX() + 4) * inverse), Math.round((getY() + 17) * inverse), primaryTextColor(), false);

        guiGraphics.pose().scale(1, 1, 1);
        guiGraphics.pose().popPose();
    }

    @Override
    public void addSelf(XpSourceScreen screen)
    {
        super.addSelf(screen);
        screen.$addWidget(display());
    }

    @Override
    public void removeSelf(XpSourceScreen screen)
    {
        super.removeSelf(screen);
        screen.$removeWidget(display());
    }

    @Override
    public void setPos(int x, int y)
    {
        int deltaX = x - getX();
        int deltaY = y - getY();

        super.setPos(x, y);
        display().changePos(deltaX, deltaY);
    }

    public ItemDisplay display() { return display; }

    public boolean singular() { return singular; }

    public String getTranslationKey() { return singular() ? "xp_source.peritia.chest.singular" : "xp_source.peritia.chest.plural"; }

}
