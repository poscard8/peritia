package github.poscard8.peritia.util.gui.widget;

import github.poscard8.peritia.client.screen.XpSourceScreen;
import github.poscard8.peritia.util.xpsource.ItemInputs;
import github.poscard8.peritia.xpsource.type.RecipeXpSource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class RecipeXpSourceWidget extends XpSourceWidget<RecipeXpSource>
{
    protected final List<ItemDisplay> displays;
    protected final RecipeXpSource.Category category;
    protected boolean singular;

    public RecipeXpSourceWidget(RecipeXpSource xpSource, int x, int y)
    {
        super(xpSource, x, y);
        this.displays = new ArrayList<>();
        this.category = xpSource.category();

        ItemInputs ingredientInputs = xpSource.ingredientInputs();
        this.singular = ItemDisplay.getItemsRecipe(xpSource).size() == 1;

        ItemDisplay middle = ItemDisplay.recipeResult(xpSource, x + 37, y + 25);
        displays.add(middle);

        if (!ingredientInputs.acceptsAll())
        {
            ItemDisplay right = ItemDisplay.recipeIngredient(xpSource, x + 70, y + 25);
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

    public RecipeXpSource.Category category() { return category; }

    public boolean singular() { return singular; }

    public String getTranslationKey()
    {
        String suffix = singular() ? "singular" : "plural";
        return String.format("xp_source.peritia.%s.%s", category().name(), suffix);
    }

}
