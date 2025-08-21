package github.poscard8.peritia.util.skill;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.config.PeritiaServerConfig;
import github.poscard8.peritia.skill.HighScoreMap;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.SkillMap;
import github.poscard8.peritia.skill.data.SkillData;
import github.poscard8.peritia.util.serialization.StringSerializable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public class SkillRequisite implements StringSerializable<SkillRequisite>
{
    public static final String ANY_NAME = "any";
    public static final String ALL_NAME = "all";

    protected @Nullable ResourceLocation skillKey = null;
    protected Type type = Type.ANY;
    protected int level = -1;

    public SkillRequisite() {}

    public SkillRequisite(@Nullable ResourceLocation skillKey, Type type, int level)
    {
        this.skillKey = skillKey;
        this.type = type;
        this.level = level;
    }

    public static SkillRequisite empty() { return new SkillRequisite(); }

    public static SkillRequisite tryLoad(String data) { return empty().loadWithFallback(data); }

    @Nullable
    public ResourceLocation skillKey() { return skillKey; }

    @Nullable
    public Skill skill() { return skillKey == null ? null : Skill.byKey(skillKey()); }

    @NotNull
    public Type type() { return type; }

    public int level() { return level; }

    public boolean isPossible() { return !isImpossible(); }

    public boolean isImpossible()
    {
        if (Peritia.skills().isEmpty()) return true;

        switch (type())
        {
            case ANY ->
            {
                for (Skill skill : Peritia.skills())
                {
                    if (skill.maxLevel() > level()) return false;
                }
                return true;
            }
            case ALL ->
            {
                for (Skill skill : Peritia.skills())
                {
                    if (level() > skill.maxLevel()) return true;
                }
                return false;
            }
            default ->
            {
                Skill skill = skill();
                return skill == null || level() > skill.maxLevel();
            }
        }
    }

    public boolean isValid()
    {
        if (type() == Type.SINGLE)
        {
            return skill() != null && level() >= 0;
        }
        else return level() >= 0;
    }

    public boolean isInvalid() { return !isValid(); }

    public boolean testForRecipe(SkillData skillData)
    {
        boolean keep = PeritiaServerConfig.KEEP_SKILL_RECIPES.get();
        return test(skillData, keep);
    }

    public boolean testForLockedItem(SkillData skillData)
    {
        boolean keep = PeritiaServerConfig.KEEP_UNLOCKED_ITEMS.get();
        return test(skillData, keep);
    }

    public boolean test(SkillData skillData, boolean keep)
    {
        return isImpossible() || (keep ? testHighScoreMap(skillData.highScoreMap()) : testSkillMap(skillData.skillMap()));
    }

    public boolean testSkillMap(SkillMap skillMap)
    {
        switch (type())
        {
            case ANY ->
            {
                for (Skill skill : Peritia.skills())
                {
                    if (skillMap.getLevel(skill) >= level()) return true;
                }
                return false;
            }
            case ALL ->
            {
                for (Skill skill : Peritia.skills())
                {
                    if (skillMap.getLevel(skill) < level()) return false;
                }
                return true;
            }
            default ->
            {
                return skillMap.getLevel(skill()) >= level();
            }
        }
    }

    public boolean testHighScoreMap(HighScoreMap highScoreMap)
    {
        switch (type())
        {
            case ANY ->
            {
                for (Skill skill : Peritia.skills())
                {
                    if (highScoreMap.getHighScore(skill) >= level()) return true;
                }
                return false;
            }
            case ALL ->
            {
                for (Skill skill : Peritia.skills())
                {
                    if (highScoreMap.getHighScore(skill) < level()) return false;
                }
                return true;
            }
            default ->
            {
                Skill newSkill = Peritia.skillHandler().byKey(Objects.requireNonNull(skill()).key());
                return highScoreMap.getHighScore(newSkill) >= level();
            }
        }
    }

    public boolean makesOtherRedundant(SkillRequisite other)
    {
        int caseId = type().ordinal() * 3 + other.type().ordinal();
        switch (caseId)
        {
            case 0, 3, 4, 5, 6 -> { return level() >= other.level(); } // ANY -> ANY or ALL -> ANY or ALL -> ALL or ALL -> SINGLE or SINGLE -> ANY
            case 1, 2, 7 -> { return false; }                          // ANY -> ALL or ANY -> SINGLE or SINGLE -> ALL
            case 8 ->                                                  // SINGLE -> SINGLE
            {
                boolean sameSkill = skillKey() == other.skillKey();
                return sameSkill && (level() >= other.level());
            }
        }
        return false;
    }

    @Override
    public SkillRequisite fallback() { return empty(); }

    @Override
    public SkillRequisite load(String data)
    {
        String[] split = data.split(",");
        if (split.length != 2) throw new RuntimeException("Invalid string for skill requisite");

        String type = split[0];
        String level = split[1];

        if (type.equals(ANY_NAME))
        {
            this.type = Type.ANY;
        }
        else if (type.equals(ALL_NAME))
        {
            this.type = Type.ALL;
        }
        else
        {
            this.type = Type.SINGLE;
            this.skillKey = ResourceLocation.tryParse(type);
        }

        this.level = Integer.parseInt(level);
        return this;
    }

    @Override
    public String save()
    {
        return isInvalid() ? "" : type().getNameFor(skillKey()) + "," + level;
    }

    public enum Type
    {
        ANY(key -> ANY_NAME),
        ALL(key -> ALL_NAME),
        SINGLE(key -> key == null ? "" : key.toString());

        private final Function<ResourceLocation, String> nameGetter;

        Type(Function<ResourceLocation, String> nameGetter) { this.nameGetter = nameGetter; }

        public String getNameFor(ResourceLocation skillKey) { return nameGetter.apply(skillKey); }

    }

}
