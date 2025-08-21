package github.poscard8.peritia.util.gui.widget;

import github.poscard8.peritia.client.screen.XpSourceScreen;
import github.poscard8.peritia.util.xpsource.ItemInputs;
import github.poscard8.peritia.util.xpsource.ResourceInputs;
import github.poscard8.peritia.xpsource.type.TradeXpSource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class TradeXpSourceWidget extends XpSourceWidget<TradeXpSource>
{
    protected final List<ItemDisplay> displays;
    protected boolean singular;

    public TradeXpSourceWidget(TradeXpSource xpSource, int x, int y)
    {
        super(xpSource, x, y);
        this.displays = new ArrayList<>();

        ItemInputs itemInputs = xpSource.itemInputs();
        ResourceInputs professionInputs = xpSource.professionInputs();

        this.singular = ItemDisplay.getItemsGeneral(itemInputs).size() == 1;

        ItemDisplay middle = ItemDisplay.item(itemInputs, ItemDisplay.FULL_RANGE, x + 37, y + 25);
        displays.add(middle);

        if (!professionInputs.acceptsAll())
        {
            ItemDisplay right = ItemDisplay.profession(xpSource, x + 70, y + 25);
            displays.add(right);
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
        displays.forEach(screen::$addWidget);
    }

    @Override
    public void removeSelf(XpSourceScreen screen)
    {
        super.removeSelf(screen);
        displays.forEach(screen::$removeWidget);
    }

    @Override
    public void setPos(int x, int y)
    {
        int deltaX = x - getX();
        int deltaY = y - getY();

        super.setPos(x, y);
        displays().forEach(display -> display.changePos(deltaX, deltaY));
    }

    public List<ItemDisplay> displays() { return displays; }

    public boolean singular() { return singular; }

    public String getTranslationKey() { return singular() ? "xp_source.peritia.trade.singular" : "xp_source.peritia.trade.plural"; }

}
