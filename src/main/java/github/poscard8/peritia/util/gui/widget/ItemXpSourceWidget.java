package github.poscard8.peritia.util.gui.widget;

import github.poscard8.peritia.client.screen.XpSourceScreen;
import github.poscard8.peritia.util.xpsource.ItemInputs;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.type.ConsumeXpSource;
import github.poscard8.peritia.xpsource.type.FishXpSource;
import github.poscard8.peritia.xpsource.type.GrindstoneXpSource;
import github.poscard8.peritia.xpsource.type.PotionXpSource;
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
public class ItemXpSourceWidget<T extends XpSource> extends XpSourceWidget<T>
{
    protected final List<Item> items;
    protected final List<ItemDisplay> displays;
    protected final String singularKey;
    protected final String pluralKey;
    protected boolean singular = false;

    public ItemXpSourceWidget(T xpSource, ItemInputs inputs, String singularKey, String pluralKey, int x, int y)
    {
        super(xpSource, x, y);
        this.items = ItemDisplay.getItemsGeneral(inputs);
        this.displays = new ArrayList<>();
        this.singularKey = singularKey;
        this.pluralKey = pluralKey;

        switch (items.size())
        {
            case 0 -> {}
            case 1 ->
            {
                ItemDisplay display = ItemDisplay.item(inputs, ItemDisplay.FULL_RANGE, x + 37, y + 25);
                displays.add(display);
                singular = true;
            }
            case 2 ->
            {
                ItemDisplay left = ItemDisplay.item(inputs, ItemDisplay.FIRST_HALF, x + 25, y + 25);
                ItemDisplay right = ItemDisplay.item(inputs, ItemDisplay.SECOND_HALF, x + 49, y + 25);

                displays.add(left);
                displays.add(right);
            }
            default ->
            {
                ItemDisplay left = ItemDisplay.item(inputs, ItemDisplay.FIRST_THIRD, x + 13, y + 25);
                ItemDisplay middle = ItemDisplay.item(inputs, ItemDisplay.SECOND_THIRD, x + 37, y + 25);
                ItemDisplay right = ItemDisplay.item(inputs, ItemDisplay.LAST_THIRD, x + 61, y + 25);

                displays.add(left);
                displays.add(middle);
                displays.add(right);
            }
        }
    }

    public static ItemXpSourceWidget<ConsumeXpSource> consume(ConsumeXpSource xpSource, int x, int y)
    {
        return new ItemXpSourceWidget<>(xpSource, xpSource.inputs(), "xp_source.peritia.consume.singular", "xp_source.peritia.consume.plural", x, y);
    }

    public static ItemXpSourceWidget<FishXpSource> fish(FishXpSource xpSource, int x, int y)
    {
        return new ItemXpSourceWidget<>(xpSource, xpSource.inputs(), "xp_source.peritia.fish.singular", "xp_source.peritia.fish.plural", x, y);
    }

    public static ItemXpSourceWidget<GrindstoneXpSource> grindstone(GrindstoneXpSource xpSource, int x, int y)
    {
        return new ItemXpSourceWidget<>(xpSource, xpSource.inputs(), "xp_source.peritia.grindstone.singular", "xp_source.peritia.grindstone.plural", x, y);
    }

    public static ItemXpSourceWidget<PotionXpSource> potion(PotionXpSource xpSource, int x, int y)
    {
        return new ItemXpSourceWidget<>(xpSource, xpSource.inputs(), "xp_source.peritia.potion.singular", "xp_source.peritia.potion.plural", x, y);
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
        displays.forEach(display -> display.changePos(deltaX, deltaY));
    }

    public List<Item> items() { return items; }

    public List<ItemDisplay> displays() { return displays; }

    public String translationKey() { return singular() ? singularKey : pluralKey; }

    public boolean singular() { return singular; }
}
