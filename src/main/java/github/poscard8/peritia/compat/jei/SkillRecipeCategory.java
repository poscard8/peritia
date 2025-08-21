package github.poscard8.peritia.compat.jei;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.network.ClientHandler;
import github.poscard8.peritia.skill.recipe.SkillRecipe;
import github.poscard8.peritia.util.text.PeritiaTexts;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
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
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SkillRecipeCategory implements IRecipeCategory<SkillRecipe>
{
    static final ResourceLocation TEXTURE_LOCATION = Peritia.asResource("textures/gui/jei/skill_recipe.png");

    public final IDrawable icon;
    public final IDrawable background;

    public SkillRecipeCategory(IGuiHelper guiHelper)
    {
        this.icon = guiHelper.createDrawable(TEXTURE_LOCATION, 144, 0, 16, 16);
        this.background = guiHelper.createDrawable(TEXTURE_LOCATION, 0, 0, 144, 96);
    }

    @Override
    public RecipeType<SkillRecipe> getRecipeType() { return PeritiaJeiPlugin.SKILL_RECIPE_TYPE; }

    @Override
    public Component getTitle() { return PeritiaTexts.skillCrafting(); }

    @Override
    public IDrawable getBackground() { return background; }

    @Override
    @Nullable
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SkillRecipe recipe, IFocusGroup focusGroup)
    {
        if (recipe.hasTwoInputs())
        {
            ItemStack[] input1Stacks = recipe.input().getStacks();
            ItemStack[] input2Stacks = recipe.secondInput().getStacks();

            IRecipeSlotBuilder slot1Builder = builder.addSlot(RecipeIngredientRole.INPUT, 20, 40);
            IRecipeSlotBuilder slot2Builder = builder.addSlot(RecipeIngredientRole.INPUT, 46, 40);
            for (ItemStack stack : input1Stacks) slot1Builder.addItemStack(stack);
            for (ItemStack stack : input2Stacks) slot2Builder.addItemStack(stack);

            builder.addSlot(RecipeIngredientRole.OUTPUT, 104, 40).addItemStack(recipe.assemble());
        }
        else
        {
            ItemStack[] inputStacks = recipe.input().getStacks();

            IRecipeSlotBuilder slotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, 33, 40);
            for (ItemStack stack : inputStacks) slotBuilder.addItemStack(stack);

            builder.addSlot(RecipeIngredientRole.OUTPUT, 91, 40).addItemStack(recipe.assemble());
        }
    }


    @Override
    public void draw(SkillRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY)
    {
        if (recipe.hasTwoInputs())
        {
            guiGraphics.blit(TEXTURE_LOCATION, 19, 35, 0, 96, 106, 26);
        }
        else guiGraphics.blit(TEXTURE_LOCATION, 32, 35, 0, 122, 80, 26);

        Font font = Minecraft.getInstance().font;
        Component text = PeritiaTexts.turquoiseExclamationMark();
        int infoX = recipe.hasTwoInputs() ? 126 : 113;
        int infoY = 34;

        guiGraphics.drawString(font, text, infoX - font.width(text), infoY, 0x00AAAA);

        boolean xCheck = mouseX >= infoX - 6 && mouseX < infoX + 2;
        boolean yCheck = mouseY >= infoY - 2 && mouseY < infoY + 10;

        if (xCheck && yCheck)
        {
            guiGraphics.renderTooltip(font, PeritiaTexts.$requisitesForRecipeJei(recipe.requisites(), ClientHandler.getSkillData()), Optional.empty(), (int) mouseX, (int) mouseY);
        }
    }
}
