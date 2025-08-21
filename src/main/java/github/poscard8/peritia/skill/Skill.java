package github.poscard8.peritia.skill;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.registry.PeritiaParticleTypes;
import github.poscard8.peritia.reward.Reward;
import github.poscard8.peritia.reward.RewardLike;
import github.poscard8.peritia.skill.data.SkillData;
import github.poscard8.peritia.skill.recipe.SkillRecipe;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import github.poscard8.peritia.util.serialization.Loadable;
import github.poscard8.peritia.util.skill.LevelXpFunction;
import github.poscard8.peritia.util.skill.SkillFunction;
import github.poscard8.peritia.util.skill.WeightHolder;
import github.poscard8.peritia.util.text.Hints;
import github.poscard8.peritia.xpsource.XpSource;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Skill implements WeightHolder, Comparable<Skill>, JsonSerializable<Skill>, Loadable
{
    public static int MAX_SKILL_COUNT = 27;

    public static int DEFAULT_MIN_LEVEL = 1;
    public static int DEFAULT_MAX_LEVEL = 100;
    public static int TRUE_MAX_LEVEL = Short.MAX_VALUE;

    public static int DEFAULT_PARTICLE_COUNT = 16;

    protected ResourceLocation key;
    protected Priority priority = Priority.empty();
    protected SkillPosition position = SkillPosition.empty();
    protected int minLevel = DEFAULT_MIN_LEVEL;
    protected int maxLevel = DEFAULT_MAX_LEVEL;
    protected double weight = 1;
    protected LevelXpFunction levelXpFunction = LevelXpFunction.empty();
    protected String translationKey = "skill.peritia.empty";
    protected Hints description = Hints.empty();
    protected SkillTextures textures = SkillTextures.empty();
    protected SimpleParticleType particleType = PeritiaParticleTypes.LEVEL_UP.get();
    protected int particleCount = DEFAULT_PARTICLE_COUNT;
    protected LevelLayout levelLayout = LevelLayout.empty();
    protected SkillAttributes attributes = SkillAttributes.empty();
    protected SkillRewards rewards = SkillRewards.empty();
    protected ItemLocks itemLocks = ItemLocks.empty();
    protected SkillLevelRestrictions levelRestrictions = SkillLevelRestrictions.empty();
    protected JsonArray conditions = new JsonArray();

    public Skill(ResourceLocation key)
    {
        this.key = key;
    }

    public static Skill empty() { return empty(EMPTY_KEY); }

    public static Skill empty(ResourceLocation key) { return new Skill(key); }

    public static Skill tryLoad(JsonObject data)
    {
        Skill skill = empty().loadWithFallback(data, empty());
        skill.onLoad();

        return skill;
    }

    public static Skill tryLoad(ResourceLocation key, JsonObject data)
    {
        JsonHelper.write(data, "key", key);
        Skill skill = empty(key).loadWithFallback(data, empty(key));
        skill.onLoad();

        return skill;
    }

    @Nullable
    public static Skill byString(String stringKey)
    {
        if (stringKey.contains(":"))
        {
            ResourceLocation key = ResourceLocation.tryParse(stringKey);
            return key == null ? null : byKey(key);
        }
        else
        {
            for (String namespace : DEFAULT_NAMESPACES)
            {
                ResourceLocation key = ResourceLocation.tryBuild(namespace, stringKey);
                Skill skill = key == null ? null : byKey(key);
                if (skill != null) return skill;
            }
            return null;
        }
    }

    @Nullable
    public static Skill byKey(ResourceLocation key) { return Peritia.skillHandler().byKey(key); }

    public static Skill byIndex(int index) { return Peritia.skillHandler().byPositionIndex(index); }

    static String defaultTranslationKey(ResourceLocation key) { return String.format("skill.%s.%s", key.getNamespace(), key.getPath()); }

    public SkillInstance newInstance() { return new SkillInstance(this); }

    public SkillInstance maxInstance() { return new SkillInstance(this, maxLevel(), getXpForMaxOutCommand(), defaultRewardArray()); }

    @Override
    public ResourceLocation key() { return key; }

    public String translationKey() { return translationKey; }

    public Component name() { return Component.translatable(translationKey()).withStyle(ChatFormatting.DARK_AQUA); }

    public Component plainName() { return name().plainCopy(); }

    public Hints description() { return description; }

    public List<Component> descriptionTexts() { return description().getTexts(); }

    public Priority priority() { return priority; }

    public boolean shouldReplace(@Nullable Skill other) { return other == null || priority().shouldReplace(other.priority()); }

    public SkillPosition position() { return position; }

    public int row() { return position().row(); }

    public int column() { return position().column(); }

    public int positionIndex() { return position().index(); }

    public int minLevel() { return minLevel; }

    public int maxLevel() { return maxLevel; }

    @Override
    public double weight() { return weight; }

    public LevelXpFunction levelXpFunction() { return levelXpFunction; }

    public int getNeededXp(int level) { return levelXpFunction().getNeededXp(level); }

    public int getNeededXp(int oldLevel, int newLevel) { return levelXpFunction().getNeededXp(oldLevel, newLevel); }

    public int getNeededTotalXp() { return levelXpFunction().getNeededTotalXp(this); }

    public int getNeededTotalXp(int level) { return levelXpFunction().getNeededTotalXp(this, level); }

    public int getXpForMaxOutCommand() { return getNeededTotalXp() > 2_000_000 ? 0 : 2_000_000 - getNeededTotalXp(); }

    public SkillTextures textures() { return textures; }

    public SimpleParticleType particleType() { return particleType; }

    public int particleCount() { return particleCount; }

    public LevelLayout levelLayout() { return levelLayout; }

    public SkillAttributes attributes() { return attributes; }

    public List<SkillAttributeInstance> attributeList() { return attributes().attributes(); }

    public List<SkillAttributeInstance> attributesAt(int level) { return attributeList().stream().filter(instance -> instance.isAvailableFor(level)).toList(); }

    public List<SkillRecipe> recipesAt(int level)
    {
        return Peritia.skillRecipes().stream().filter(recipe -> recipe.isValidRewardLikeFor(this, level)).toList();
    }

    public SkillRewards rewards() { return rewards; }

    public List<Reward> rewardList() { return rewards().rewards(); }

    public List<Reward> rewardsAt(int level) { return rewardList().stream().filter(reward -> reward.isAvailableFor(level)).toList(); }

    public List<RewardLike<?>> rewardLikesAt(int level)
    {
        List<RewardLike<?>> rewardLikes = new ArrayList<>();
        rewardLikes.addAll(attributesAt(level));
        rewardLikes.addAll(itemLocksAt(level));
        rewardLikes.addAll(recipesAt(level));
        rewardLikes.addAll(rewardsAt(level));

        return rewardLikes.stream().sorted(Comparator.comparingInt(RewardLike::priority)).toList();
    }

    public boolean[] defaultRewardArray()
    {
        boolean[] array = new boolean[maxLevel() + 1];

        for (int i = minLevel(); i <= maxLevel(); i++)
        {
            array[i] = true;

            for (Reward reward : rewardList())
            {
                if (reward.isAvailableFor(i))
                {
                    array[i] = false;
                    break;
                }
            }
        }
        return array;
    }

    public ItemLocks itemLocks() { return itemLocks; }

    public List<ItemLock> itemLockList() { return itemLocks().locks(); }

    public List<ItemLock> itemLocksAt(int level) { return itemLockList().stream().filter(lock -> lock.isValidRewardLikeFor(this, level)).toList(); }

    public SkillLevelRestrictions levelRestrictions() { return levelRestrictions; }

    public List<LevelRestriction> levelRestrictionList() { return levelRestrictions().restrictions(); }

    public List<LevelRestriction> levelRestrictionsAt(int level) { return levelRestrictionList().stream().filter(restriction -> restriction.isAvailableFor(level)).toList(); }

    public boolean canAutomaticallyLevelUp(int newLevel) { return levelRestrictionsAt(newLevel).isEmpty(); }

    @Override
    public JsonArray conditions() { return conditions; }

    public List<XpSource> getXpSourcesForSelf(SkillData skillData)
    {
        return Peritia.xpSources().stream().filter(xpSource ->
        {
            SkillFunction skillFunction = xpSource.skillFunction();
            return skillFunction.getSkills(skillData).contains(this) || skillFunction == SkillFunction.Special.RANDOM;
        }).toList();
    }

    public void onLoad()
    {
        this.minLevel = Mth.clamp(minLevel, 0, 1);
        this.maxLevel = Math.max(maxLevel, minLevel + 1);

        itemLocks().assignSkill(this);
        levelRestrictions().assignSkill(this);
    }

    public void onRevalidate()
    {
        itemLocks().validateConditions();
    }

    @Override
    public Skill fallback() { return empty(); }

    @Override
    public Skill load(JsonObject data)
    {
        ParticleType<?> particleType2 = JsonHelper.readRegistrable(data, "particleType", ForgeRegistries.PARTICLE_TYPES, particleType);
        SimpleParticleType particleType3 = particleType2 instanceof SimpleParticleType simpleParticleType ? simpleParticleType : particleType;

        this.key = JsonHelper.readResource(data, "key", key);
        this.translationKey = JsonHelper.readString(data, "translationKey", defaultTranslationKey(key));
        this.description = JsonHelper.readArraySerializable(data, "description", Hints::tryLoad, description);
        this.priority = JsonHelper.readStringSerializable(data, "priority", Priority::tryLoad, priority);
        this.position = JsonHelper.readStringSerializable(data, "position", SkillPosition::tryLoad, position);
        this.minLevel = JsonHelper.readInt(data, "minLevel", minLevel);
        this.maxLevel = JsonHelper.readInt(data, "maxLevel", maxLevel);
        this.weight = JsonHelper.readDouble(data, "weight", weight);
        this.levelXpFunction = JsonHelper.readJsonSerializable(data, "levelXpFunction", LevelXpFunction::tryLoad, levelXpFunction);
        this.textures = JsonHelper.readJsonSerializable(data, "textures", SkillTextures::tryLoad, textures);
        this.particleType = particleType3;
        this.particleCount = JsonHelper.readInt(data, "particleCount", particleCount);
        this.levelLayout = JsonHelper.readStringSerializable(data, "levelLayout", LevelLayout::tryLoad, levelLayout);
        this.attributes = JsonHelper.readArraySerializable(data, "attributes", SkillAttributes::tryLoad, attributes);
        this.rewards = JsonHelper.readArraySerializable(data, "rewards", SkillRewards::tryLoad, rewards);
        this.itemLocks = JsonHelper.readArraySerializable(data, "itemLocks", ItemLocks::tryLoad, itemLocks);
        this.levelRestrictions = JsonHelper.readArraySerializable(data, "levelRestrictions", SkillLevelRestrictions::tryLoad, levelRestrictions);
        this.conditions = JsonHelper.readArray(data, "conditions", conditions);

        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();
        JsonHelper.write(data, "key", key);
        JsonHelper.write(data, "translationKey", translationKey);
        JsonHelper.write(data, "description", description);
        JsonHelper.write(data, "priority", priority);
        JsonHelper.write(data, "position", position);
        JsonHelper.write(data, "minLevel", minLevel);
        JsonHelper.write(data, "maxLevel", maxLevel);
        JsonHelper.write(data, "weight", weight);
        JsonHelper.write(data, "levelXpFunction", levelXpFunction);
        JsonHelper.write(data, "textures", textures);
        JsonHelper.write(data, "particleType", particleType, ForgeRegistries.PARTICLE_TYPES);
        JsonHelper.write(data, "particleCount", particleCount);
        JsonHelper.write(data, "levelLayout", levelLayout);
        JsonHelper.write(data, "attributes", attributes);
        JsonHelper.write(data, "rewards", rewards);
        JsonHelper.write(data, "itemLocks", itemLocks);
        JsonHelper.write(data, "levelRestrictions", levelRestrictions);
        JsonHelper.write(data, "conditions", conditions);

        return data;
    }

    @Override
    public int compareTo(@NotNull Skill other) { return positionIndex() - other.positionIndex(); }

    @Override
    public boolean equals(Object object) { return object instanceof Skill skill && key().equals(skill.key()); }

    @Override
    public String toString() { return String.format("Skill: %s", stringKey()); }

}
