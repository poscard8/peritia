package github.poscard8.peritia.compat.jei;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.network.ClientHandler;
import github.poscard8.peritia.reward.ItemReward;
import github.poscard8.peritia.skill.recipe.SkillRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@JeiPlugin
public class PeritiaJeiPlugin implements IModPlugin
{
    static final ResourceLocation ID = Peritia.asResource("jei_plugin");

    static final RecipeType<SkillRecipe> SKILL_RECIPE_TYPE = RecipeType.create(Peritia.ID, "skill_recipe", SkillRecipe.class);
    static final RecipeType<ItemReward> ASCENSION_TYPE =  RecipeType.create(Peritia.ID, "ascension", ItemReward.class);

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration)
    {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        registration.addRecipeCategories(new SkillRecipeCategory(guiHelper), new AscensionCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration)
    {
        registration.addRecipes(SKILL_RECIPE_TYPE, Peritia.skillRecipes());
        registration.addRecipes(ASCENSION_TYPE, ClientHandler.getAscensionSystem().itemRewards());
    }

    @Override
    public ResourceLocation getPluginUid() { return ID; }

}
