package github.poscard8.peritia.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import github.poscard8.peritia.skill.recipe.SkillRecipe;
import github.poscard8.peritia.util.serialization.PeritiaResourceHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 */
@ParametersAreNonnullByDefault
@SuppressWarnings("unused")
public class SkillRecipeHandler extends SimpleJsonResourceReloadListener implements PeritiaResourceHandler
{
    protected static final String KEY = "peritia/skill_recipe";

    public List<SkillRecipe> recipes = new ArrayList<>();

    public SkillRecipeHandler() { super(GSON, KEY); }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller filler)
    {
        recipes = new ArrayList<>();
        int index = 1;

        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet())
        {
            ResourceLocation key = entry.getKey();

            if (!entry.getValue().isJsonObject())
            {
                MOD_LOGGER.error("Parsing error loading skill recipe {}", key);
                continue;
            }

            JsonObject jsonObject = entry.getValue().getAsJsonObject();

            try
            {
                SkillRecipe recipe = SkillRecipe.tryLoad(key, jsonObject);

                if (recipe.isInvalid())
                {
                    MOD_LOGGER.error("Skill recipe {} is invalid, therefore not loaded", key);
                    continue;
                }

                if (!recipe.isEmpty()) recipes.add(recipe);
            }
            catch (IllegalArgumentException | JsonParseException jsonParseException)
            {
                MOD_LOGGER.error("Parsing error loading skill recipe {}", key, jsonParseException);
            }
        }

        recipes = recipes.stream().sorted(SkillRecipe::compareTo).collect(Collectors.toList());
        MOD_LOGGER.info("Loaded {} skill recipes", values().size());
    }

    @Override
    public void revalidate()
    {
        MOD_LOGGER.info("Revalidating skill recipes");

        int initialSize = recipes.size();
        Iterator<SkillRecipe> iterator = recipes.iterator();

        while (iterator.hasNext())
        {
            SkillRecipe recipe = iterator.next();

            if (recipe.doesNotMeetConditions())
            {
                MOD_LOGGER.info("Removing skill recipe {} as it does not meet conditions", recipe.key());
                iterator.remove();
            }
        }
        int newSize = recipes.size();
        int removed = initialSize - newSize;

        if (removed > 0) MOD_LOGGER.info("{} skill recipes removed, {} skill recipes remain", removed, newSize);
    }

    @Nullable
    public SkillRecipe byKey(ResourceLocation key)
    {
        for (SkillRecipe recipe : values())
        {
            if (recipe.key().equals(key)) return recipe;
        }
        return null;
    }

    public List<ResourceLocation> keys()
    {
        List<ResourceLocation> keys = new ArrayList<>();

        for (SkillRecipe recipe : values()) keys.add(recipe.key());
        return keys;
    }

    public List<SkillRecipe> values() { return recipes; }

    public int recipeCount() { return values().size(); }

}
