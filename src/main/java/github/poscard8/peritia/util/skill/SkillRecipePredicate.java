package github.poscard8.peritia.util.skill;

import github.poscard8.peritia.skill.recipe.SkillRecipe;
import github.poscard8.peritia.util.serialization.StringSerializable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class SkillRecipePredicate implements Predicate<SkillRecipe>, StringSerializable<SkillRecipePredicate>
{
    public static final String ANY_NAME = "any";

    protected @Nullable ResourceLocation recipeKey = null;
    protected boolean acceptAny = true;

    public SkillRecipePredicate() {}

    public SkillRecipePredicate(@Nullable ResourceLocation recipeKey)
    {
        this.recipeKey = recipeKey;
        this.acceptAny = recipeKey == null;
    }

    public static SkillRecipePredicate empty() { return new SkillRecipePredicate(); }

    public static SkillRecipePredicate tryLoad(String data) { return empty().loadWithFallback(data); }

    @Nullable
    public ResourceLocation recipeKey() { return recipeKey; }

    @Nullable
    public SkillRecipe recipe() { return recipeKey == null ? null : SkillRecipe.byKey(recipeKey()); }

    public boolean acceptsAny() { return acceptAny; }

    @Override
    public boolean test(SkillRecipe recipe) { return acceptsAny() || recipe.equals(recipe()); }

    @Override
    public SkillRecipePredicate fallback() { return empty(); }

    @Override
    public SkillRecipePredicate load(String data)
    {
        ResourceLocation recipeKey = data.equals(ANY_NAME) ? null : ResourceLocation.tryParse(data);
        return new SkillRecipePredicate(recipeKey);
    }

    @Override
    public String save() { return acceptAny || recipeKey == null ? ANY_NAME : recipeKey.toString(); }


}
