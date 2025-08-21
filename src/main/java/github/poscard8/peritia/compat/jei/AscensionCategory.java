package github.poscard8.peritia.compat.jei;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.network.ClientHandler;
import github.poscard8.peritia.reward.ItemReward;
import github.poscard8.peritia.util.text.PeritiaTexts;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AscensionCategory implements IRecipeCategory<ItemReward>
{
    static final ResourceLocation TEXTURE_LOCATION = Peritia.asResource("textures/gui/jei/ascension.png");

    public final IDrawable icon;
    public final IDrawable background;

    public AscensionCategory(IGuiHelper guiHelper)
    {
        this.icon = guiHelper.createDrawableItemStack(ClientHandler.getAscensionSystem().icon().getDefaultInstance());
        this.background = guiHelper.createDrawable(TEXTURE_LOCATION, 0, 0, 144, 96);
    }

    @Override
    public RecipeType<ItemReward> getRecipeType() { return PeritiaJeiPlugin.ASCENSION_TYPE; }

    @Override
    public Component getTitle() { return PeritiaTexts.ascensionTitle(); }

    @Override
    public IDrawable getBackground() { return background; }

    @Override
    @Nullable
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ItemReward itemReward, IFocusGroup focusGroup)
    {
        builder.addSlot(RecipeIngredientRole.OUTPUT, 64, 40).addItemStack(itemReward.item());
    }

    @Override
    public void draw(ItemReward itemReward, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY)
    {
        Font font = Minecraft.getInstance().font;
        Component text = PeritiaTexts.turquoiseExclamationMark();
        int infoX = 86;
        int infoY = 34;

        guiGraphics.drawString(font, text, infoX - font.width(text), infoY, 0x00AAAA);

        boolean xCheck = mouseX >= infoX - 6 && mouseX < infoX + 2;
        boolean yCheck = mouseY >= infoY - 2 && mouseY < infoY + 10;

        if (xCheck && yCheck)
        {
            guiGraphics.renderTooltip(font, PeritiaTexts.ascensionRewardContext(itemReward), (int) mouseX, (int) mouseY);
        }
    }

}
