package github.poscard8.peritia.util.gui.widget;

import github.poscard8.peritia.client.screen.XpSourceScreen;
import github.poscard8.peritia.xpsource.type.EntityXpSource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class EntityXpSourceWidget extends XpSourceWidget<EntityXpSource>
{
    protected final List<Item> items;
    protected final List<ItemDisplay> displays;
    protected boolean singular = false;

    public EntityXpSourceWidget(EntityXpSource xpSource, int x, int y)
    {
        super(xpSource, x, y);
        this.items = ItemDisplay.getItemsEntity(xpSource);
        this.displays = new ArrayList<>();

        switch (items.size())
        {
            case 0 -> {}
            case 1 ->
            {
                ItemDisplay display = ItemDisplay.entity(xpSource, ItemDisplay.FULL_RANGE, x + 37, y + 25);
                displays.add(display);
                singular = true;
            }
            case 2 ->
            {
                ItemDisplay left = ItemDisplay.entity(xpSource, ItemDisplay.FIRST_HALF, x + 25, y + 25);
                ItemDisplay right = ItemDisplay.entity(xpSource, ItemDisplay.SECOND_HALF, x + 49, y + 25);

                displays.add(left);
                displays.add(right);
            }
            default ->
            {
                ItemDisplay left = ItemDisplay.entity(xpSource, ItemDisplay.FIRST_THIRD, x + 13, y + 25);
                ItemDisplay middle = ItemDisplay.entity(xpSource, ItemDisplay.SECOND_THIRD, x + 37, y + 25);
                ItemDisplay right = ItemDisplay.entity(xpSource, ItemDisplay.LAST_THIRD, x + 61, y + 25);

                displays.add(left);
                displays.add(middle);
                displays.add(right);
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

        Component text = Component.translatable(translationKey());
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

    public List<Item> items() { return items; }

    public List<ItemDisplay> displays() { return displays; }

    public String translationKey() { return singular() ? "xp_source.peritia.entity.singular" : "xp_source.peritia.entity.plural"; }

    public boolean singular() { return singular; }
}
