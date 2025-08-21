package github.poscard8.peritia.util.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.recipe.SkillRecipe;
import github.poscard8.peritia.xpsource.XpSource;

import java.util.ArrayList;
import java.util.List;

public class PeritiaResources implements JsonSerializable<PeritiaResources>
{
    protected JsonArray skills;
    protected JsonArray skillRecipes;
    protected JsonArray xpSources;

    PeritiaResources() {}

    PeritiaResources(PeritiaResourceLoader loader)
    {
        this.skills = saveSkills(loader.skillHandler.skills);
        this.skillRecipes = saveSkillRecipes(loader.skillRecipeHandler.recipes);
        this.xpSources = saveXpSources(loader.xpSourceHandler.xpSources);
    }

    public static PeritiaResources empty() { return new PeritiaResources(); }

    public static PeritiaResources tryLoad(JsonObject data) { return empty().loadWithFallback(data); }

    private static JsonArray saveSkills(Skill[] skills)
    {
        JsonArray array = new JsonArray();

        for (int i = 0; i < Skill.MAX_SKILL_COUNT; i++)
        {
            Skill skill = skills[i];
            JsonElement element = skill == null ? JsonNull.INSTANCE : skill.save();
            array.add(element);
        }
        return array;
    }

    private static JsonArray saveSkillRecipes(List<SkillRecipe> skillRecipes)
    {
        JsonArray array = new JsonArray();
        for (SkillRecipe recipe : skillRecipes) array.add(recipe.save());
        return array;
    }

    private static JsonArray saveXpSources(List<XpSource> xpSources)
    {
        JsonArray array = new JsonArray();
        for (XpSource xpSource : xpSources) array.add(xpSource.save());
        return array;
    }

    public Skill[] loadSkills()
    {
        Skill[] skillArray = new Skill[Skill.MAX_SKILL_COUNT];

        for (int i = 0; i < Skill.MAX_SKILL_COUNT; i++)
        {
            JsonElement element = skills.get(i);
            if (element.isJsonObject())
            {
                Skill skill = Skill.tryLoad(element.getAsJsonObject());
                skillArray[i] = skill;
            }
        }
        return skillArray;
    }

    public List<SkillRecipe> loadSkillRecipes()
    {
        List<SkillRecipe> list = new ArrayList<>();

        for (JsonElement element : skillRecipes)
        {
            if (element.isJsonObject())
            {
                SkillRecipe recipe = SkillRecipe.tryLoad(element.getAsJsonObject());
                list.add(recipe);
            }
        }
        return list;
    }

    public List<XpSource> loadXpSources()
    {
        List<XpSource> list = new ArrayList<>();

        for (JsonElement element : xpSources)
        {
            if (element.isJsonObject())
            {
                XpSource xpSource = XpSource.tryLoad(element.getAsJsonObject());
                list.add(xpSource);
            }
        }
        return list;
    }

    @Override
    public PeritiaResources fallback() { return empty(); }

    @Override
    public PeritiaResources load(JsonObject data)
    {
        this.skills = JsonHelper.readArray(data, "skills");
        this.skillRecipes = JsonHelper.readArray(data, "skillRecipes");
        this.xpSources = JsonHelper.readArray(data, "xpSources");

        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();
        JsonHelper.write(data, "skills", skills);
        JsonHelper.write(data, "skillRecipes", skillRecipes);
        JsonHelper.write(data, "xpSources", xpSources);

        return data;
    }

}
