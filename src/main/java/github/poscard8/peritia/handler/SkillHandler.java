package github.poscard8.peritia.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.serialization.PeritiaResourceHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Skill loader. {@link #skills} is a skill array with a size of 27.
 * <p>Skills are registered based on position (row and column). If multiple skills
 * are registered on the same position, one of them will override the other.</p>
 */
@ParametersAreNonnullByDefault
@SuppressWarnings("unused")
public final class SkillHandler extends SimpleJsonResourceReloadListener implements PeritiaResourceHandler
{
    static final String KEY = "peritia/skill";

    public Skill[] skills = new Skill[Skill.MAX_SKILL_COUNT];

    public SkillHandler() { super(GSON, KEY); }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller filler)
    {
        skills = new Skill[Skill.MAX_SKILL_COUNT];

        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet())
        {
            ResourceLocation key = entry.getKey();

            if (!entry.getValue().isJsonObject())
            {
                MOD_LOGGER.error("Parsing error loading skill {}", key);
                continue;
            }

            JsonObject jsonObject = entry.getValue().getAsJsonObject();

            try
            {
                Skill skill = Skill.tryLoad(key, jsonObject);
                int index = skill.positionIndex();

                if (skill.isInvalid())
                {
                    MOD_LOGGER.error("Skill {} is invalid, therefore not loaded", key);
                    continue;
                }

                if (!skill.isEmpty())
                {
                    @Nullable Skill existing = skills[index];

                    if (existing == null)
                    {
                        skills[index] = skill;
                    }
                    else
                    {
                        if (skill.shouldReplace(existing))
                        {
                            skills[skill.positionIndex()] = skill;
                            MOD_LOGGER.info("Skill {} replaced skill {} while loading", skill.stringKey(), existing.stringKey());
                        }
                        else MOD_LOGGER.info("Skill {} is not loaded as it had lower priority than {}", skill.stringKey(), existing.stringKey());
                    }

                }
            }
            catch (IllegalArgumentException | JsonParseException jsonParseException)
            {
                MOD_LOGGER.error("Parsing error loading skill {}", key, jsonParseException);
            }
        }
        MOD_LOGGER.info("Loaded {} skills", values().size());
    }

    @Override
    public void revalidate()
    {
        MOD_LOGGER.info("Revalidating skills");

        int initialSize = values().size();

        for (int i = 0; i < Skill.MAX_SKILL_COUNT; i++)
        {
            @Nullable Skill skill = skills[i];
            if (skill == null) continue;

            if (skill.doesNotMeetConditions())
            {
                skills[i] = null;
                MOD_LOGGER.info("Removing skill {} as it does not meet conditions", skill.key());
                continue;
            }
            skill.onRevalidate();
        }
        int newSize = values().size();
        int removed = initialSize - newSize;

        if (removed > 0) MOD_LOGGER.info("{} skills removed, {} skills remain", removed, newSize);
    }

    @Nullable
    public Skill byKey(ResourceLocation key)
    {
        for (Skill skill : values())
        {
            if (skill.key().equals(key)) return skill;
        }
        return null;
    }

    @Nullable
    public Skill byPositionIndex(int index)
    {
        try
        {
            Skill[] skillArray = valueArray();
            return skillArray[index];
        }
        catch (Exception exception) { return null; }
    }

    public List<ResourceLocation> keys()
    {
        List<ResourceLocation> keys = new ArrayList<>();

        for (Skill skill : values()) keys.add(skill.key());
        return keys;
    }

    public List<Skill> values()
    {
        List<Skill> skills = new ArrayList<>();

        for (Skill skill : valueArray())
        {
            if (skill != null) skills.add(skill);
        }
        return skills;
    }

    public Skill getFirstSkill() { return values().isEmpty() ? Skill.empty() : values().get(0); }

    public Skill[] valueArray() { return skills; }

    public int maxSkillLevel() { return values().stream().map(Skill::maxLevel).max(Integer::compareTo).orElse(Skill.TRUE_MAX_LEVEL); }

}
