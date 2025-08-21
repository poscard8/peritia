package github.poscard8.peritia.skill;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import github.poscard8.peritia.enchantment.SymmetryEnchantment;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.clientbound.LevelUpPacket;
import github.poscard8.peritia.network.packet.clientbound.LevelUpReadyPacket;
import github.poscard8.peritia.reward.Reward;
import github.poscard8.peritia.skill.data.ServerSkillData;
import github.poscard8.peritia.util.PeritiaHelper;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import github.poscard8.peritia.util.skill.ComparableWeightHolder;
import github.poscard8.peritia.util.skill.XpGainContext;
import github.poscard8.peritia.util.text.PeritiaTexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

public class SkillInstance implements ComparableWeightHolder, JsonSerializable<SkillInstance>
{
    static final String UUID_PREFIX = "335da025-1f47-477e-b7a9-f443a78af1";

    protected Skill skill;
    protected int level;
    protected int xp;
    protected boolean[] claimedRewards;

    SkillInstance(Skill skill) { this(skill, skill.minLevel(), 0, skill.defaultRewardArray()); }

    SkillInstance(Skill skill, int level, int xp, boolean[] claimedRewards)
    {
        this.skill = skill;
        this.level = level;
        this.xp = xp;
        this.claimedRewards = claimedRewards;
    }

    public static SkillInstance empty() { return new SkillInstance(Skill.empty()); }

    public static SkillInstance tryLoad(JsonObject data) { return empty().loadWithFallback(data); }

    public static SkillInstance tryLoad(JsonObject data, Skill skill) { return empty().loadWithFallback(data, skill.newInstance()); }

    public static SkillInstance fromXpGainContext(XpGainContext.Component component) { return new SkillInstance(component.skill(), component.level(), component.xp(), component.skill().defaultRewardArray()); }

    public SkillInstance copy() { return new SkillInstance(skill(), level(), xp(), claimedRewards()); }

    public Skill skill() { return skill; }

    public ResourceLocation key() { return skill().key(); }

    public String stringKey() { return skill().stringKey(); }

    public int level() { return level; }

    public void setLevelValue(int newLevel) { this.level = Mth.clamp(newLevel, minLevel(), maxLevel()); }

    public void incrementLevelValue(int levels) { setLevelValue(level() + levels); }

    public int previousLevel() { return isMinLevel() ? level() : level() - 1; }

    public int nextLevel() { return isMaxLevel() ? level() : level() + 1; }

    public int levelOfInterest()
    {
        int levelOfInterest = milestoneStatus(level()).pending ? level() : nextLevel();
        return Mth.clamp(levelOfInterest, minLevel(), maxLevel());
    }

    public int minLevel() { return skill().minLevel(); }

    public int maxLevel() { return skill().maxLevel(); }

    public boolean isMinLevel() { return level() == minLevel(); }

    public boolean isMaxLevel() { return level() == maxLevel(); }

    public int xp() { return xp; }

    public void setXpValue(int newXp) { this.xp = Math.max(0, newXp); }

    public void incrementXpValue(int xp) { setXpValue(xp() + xp); }

    public void decrementXpValue(int xp) { setXpValue(xp() - xp); }

    public int totalXp() { return skill().getNeededTotalXp(level()) + xp(); }

    @Override
    public double weight() { return skill().weight() * 0.01F * totalXp(); }

    public int xpNeededForLevelUp() { return skill().getNeededXp(nextLevel()); }

    public boolean hasXpForLevelUp() { return !isMaxLevel() && xp() >= xpNeededForLevelUp(); }

    public boolean canAutomaticallyLevelUp() { return hasXpForLevelUp() && skill().canAutomaticallyLevelUp(nextLevel()); }

    public int xpForMilestone(int level) { return totalXp() - skill().getNeededTotalXp(level - 1); }

    public int xpNeededForMilestone(int level) { return skill().getNeededTotalXp(level) - skill().getNeededTotalXp(level - 1); }

    public boolean hasPendingLevels() { return !isMaxLevel() && xp() >= xpNeededForLevelUp(); }

    public boolean[] claimedRewards() { return claimedRewards; }

    public void resetRewards() { this.claimedRewards = skill().defaultRewardArray(); }

    public boolean hasUnclaimedRewards()
    {
        for (int lvl = minLevel(); lvl <= level(); lvl++)
        {
            if (canClaimReward(lvl)) return true;
        }
        return false;
    }

    public boolean isRewardClaimed(int level) { return claimedRewards()[level]; }

    public boolean canClaimReward(int level) { return !isRewardClaimed(level); }

    public boolean hasProgress() { return level() > minLevel() || xp() > 0; }

    public Status status()
    {
        if (hasPendingLevels()) return Status.PENDING_LEVELS;
        if (hasUnclaimedRewards()) return Status.PENDING_REWARDS;

        return Status.NORMAL;
    }

    public MilestoneStatus milestoneStatus(int level)
    {
        if (level() >= level) return MilestoneStatus.UNLOCKED;

        if (hasPendingLevels())
        {
            int totalXp = totalXp();
            int xpNeeded = skill().getNeededTotalXp(level);
            int previousXpNeeded = skill().getNeededTotalXp(level - 1);

            if (totalXp >= xpNeeded) return MilestoneStatus.PENDING;
            return totalXp >= previousXpNeeded ? MilestoneStatus.PENDING_UNLOCKING : MilestoneStatus.LOCKED;
        }
        else
        {
            return level == nextLevel() ? MilestoneStatus.UNLOCKING : MilestoneStatus.LOCKED;
        }
    }

    public Map<Attribute, Double> attributeMap()
    {
        Map<Attribute, Double> map = new HashMap<>();
        for (SkillAttributeInstance attributeInstance : skill().attributes())
        {
            Attribute attribute = attributeInstance.attribute();
            if (attribute == null) continue;

            double existingValue = map.getOrDefault(attribute, 0.0D);
            double addedValue = attributeInstance.value() * (attributeInstance.at().countInclusive(minLevel(), level()));
            double newValue = existingValue + addedValue;
            map.put(attribute, newValue);
        }
        return map;
    }

    public Multimap<Attribute, AttributeModifier> attributeModifierMap()
    {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = new ImmutableMultimap.Builder<>();

        for (Map.Entry<Attribute, Double> entry : attributeMap().entrySet())
        {
            String desc = "Peritia skill modifier: " + stringKey();
            String uuidString = UUID_PREFIX + String.format("%02x", skill().positionIndex());
            UUID uuid = UUID.fromString(uuidString);

            Attribute attribute = entry.getKey();
            AttributeModifier modifier = new AttributeModifier(uuid, () -> desc, entry.getValue(), AttributeModifier.Operation.ADDITION);
            builder.put(attribute, modifier);
        }
        return builder.build();
    }

    public void tryClaimRewards(ServerSkillData skillData, int oldLevel, int newLevel)
    {
        ServerPlayer player = skillData.player();

        for (int lvl = oldLevel + 1; lvl <= newLevel; lvl++)
        {
            if (canClaimReward(lvl))
            {
                claimedRewards[lvl] = true;
                for (Reward reward : skill().rewardsAt(lvl)) reward.award(player);
            }
        }
        updateSkillData(skillData);
    }

    public void tryPayRestrictions(ServerSkillData skillData, int level)
    {
        if (canPayRestrictions(level) && hasItemsForRestrictions(skillData.player(), level)) payRestrictions(skillData, level);
    }

    public boolean canPayRestrictions(int level) { return milestoneStatus(level) == MilestoneStatus.PENDING && milestoneStatus(level - 1) == MilestoneStatus.UNLOCKED; }

    public boolean hasItemsForRestrictions(ServerPlayer player, int level)
    {
        List<LevelRestriction> restrictions = skill().levelRestrictionsAt(level);

        for (LevelRestriction restriction : restrictions)
        {
            if (!restriction.checkInventory(player)) return false;
        }
        return true;
    }

    public void payRestrictions(ServerSkillData skillData, int level)
    {
        List<LevelRestriction> restrictions = skill().levelRestrictionsAt(level);
        for (LevelRestriction restriction : restrictions) restriction.takeItem(skillData.player());

        decrementXpValue(xpNeededForLevelUp());
        incrementLevelValue(1);
        addXp(skillData, 0, false, true, false);

        updateSkillData(skillData);
    }

    public boolean addXp(ServerSkillData skillData, int xp, boolean manually, boolean playSound, boolean ignoreRestrictions)
    {
        boolean hasPending = hasPendingLevels();
        incrementXpValue(xp);

        ServerPlayer player = skillData.player();
        int oldLevel = level();
        boolean levelingUp = xp() >= xpNeededForLevelUp() && !isMaxLevel();
        boolean levelUpReady = false;

        while (levelingUp)
        {
            if (canAutomaticallyLevelUp() || ignoreRestrictions)
            {
                decrementXpValue(xpNeededForLevelUp());
                incrementLevelValue(1);
                levelingUp = xp() >= xpNeededForLevelUp() && !isMaxLevel();
            }
            else
            {
                levelUpReady = !hasPending && hasPendingLevels();
                levelingUp = false;
            }
        }

        boolean levelUp = level() > oldLevel;
        if (levelUp) onLevelUp(player, oldLevel, level(), manually);
        if (levelUpReady) onLevelUpReady(player, playSound);

        int gameExperience = SymmetryEnchantment.getExperienceGain(player, xp);
        if (manually && gameExperience > 0) PeritiaHelper.giveExperienceToPlayer(player, gameExperience);

        updateSkillData(skillData);
        return levelUp;
    }

    public boolean addLevel(ServerSkillData skillData, int levels)
    {
        int oldLevel = level();
        setLevelValue(level() + levels);

        boolean levelUp = level() > oldLevel;
        if (levelUp) onLevelUp(skillData.player(), oldLevel, level(), false);

        updateSkillData(skillData);
        return levelUp;
    }

    public boolean setXp(ServerSkillData skillData, int xp, boolean playSound, boolean ignoreRestrictions)
    {
        boolean hasPending = hasPendingLevels();
        setXpValue(xp);

        ServerPlayer player = skillData.player();
        int oldLevel = level();
        boolean levelingUp = xp() >= xpNeededForLevelUp() && !isMaxLevel();
        boolean levelUpReady = false;

        while (levelingUp)
        {
            if (canAutomaticallyLevelUp() || ignoreRestrictions)
            {
                decrementXpValue(xpNeededForLevelUp());
                incrementLevelValue(1);
                levelingUp = xp() >= xpNeededForLevelUp() && !isMaxLevel();
            }
            else
            {
                levelUpReady = !hasPending && hasPendingLevels();
                levelingUp = false;
            }
        }

        boolean levelUp = level() > oldLevel;
        if (levelUp) onLevelUp(player, oldLevel, level(), false);
        if (levelUpReady) onLevelUpReady(player, playSound);

        updateSkillData(skillData);
        return levelUp;
    }

    public boolean setLevel(ServerSkillData skillData, int level)
    {
        int oldLevel = level();
        setLevelValue(level);

        boolean levelDown = level() < oldLevel;
        boolean levelUp = level() > oldLevel;

        if (levelDown) setXpValue(0);
        if (levelUp) onLevelUp(skillData.player(), oldLevel, level(), false);

        updateSkillData(skillData);
        return levelUp;
    }

    public void reset(ServerSkillData skillData)
    {
        setLevelValue(skill().minLevel());
        setXpValue(0);
        resetRewards();
        updateSkillData(skillData);
    }

    /**
     * Level ups are made from <i>n</i> to <i>m</i> instead of <i>n</i> to <i>n+1</i>. This is to prevent
     * the skill from displaying more messages than needed when large amounts of xp is gained instantly (commands).
     */
    public void onLevelUp(ServerPlayer player, int oldLevel, int newLevel, boolean manually)
    {
        if (newLevel == maxLevel() && manually)
        {
            Objects.requireNonNull(player.getServer()).getPlayerList().broadcastSystemMessage(PeritiaTexts.playerMaxedOutSkill(player, skill()), false);
        }
        PeritiaNetworkHandler.sendToClient(new LevelUpPacket(skill(), oldLevel, newLevel, manually), player);
    }

    public void onLevelUpReady(ServerPlayer player, boolean playSound)
    {
        PeritiaNetworkHandler.sendToClient(new LevelUpReadyPacket(skill(), playSound), player);
    }

    public void updateSkillData(ServerSkillData skillData) { skillData.putSkill(this); }

    @Override
    public SkillInstance fallback() { return empty(); }

    @Override
    public SkillInstance load(JsonObject data)
    {
        this.skill = Skill.byKey(JsonHelper.readResource(data, "skill"));
        assert skill != null;

        this.level = JsonHelper.readInt(data, "level", skill.minLevel());
        this.xp = JsonHelper.readInt(data, "newLevel", xp);
        this.claimedRewards = JsonHelper.readBooleanArray(data, "claimedRewards", skill.defaultRewardArray());

        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();
        JsonHelper.write(data, "skill", skill.key());
        JsonHelper.write(data, "level", level);
        JsonHelper.write(data, "newLevel", xp);
        JsonHelper.write(data, "claimedRewards", claimedRewards);

        return data;
    }


    public enum Status
    {
        NORMAL(null),
        PENDING_LEVELS(PeritiaTexts.redExclamationMark()),
        PENDING_REWARDS(PeritiaTexts.glowExclamationMark());

        @Nullable
        final Component text;

        Status(@Nullable Component text) { this.text = text; }

        @Nullable
        public Component text() { return text; }

    }

    public enum MilestoneStatus
    {
        LOCKED((instance, level) -> PeritiaTexts.locked(), false, false, 0),
        UNLOCKED((instance, level) -> PeritiaTexts.unlocked(), true, false, 44),
        UNLOCKING(PeritiaTexts::progressBarMilestone, false, false, 88),
        PENDING((instance, level) -> PeritiaTexts.pending(), false, true, 132),
        PENDING_UNLOCKING(PeritiaTexts::progressBarMilestone, false, true, 176);

        private final BiFunction<SkillInstance, Integer, Component> textFunction;
        private final boolean canClaim;
        private final boolean pending;
        private final int yTexOffset;

        MilestoneStatus(BiFunction<SkillInstance, Integer, Component> textFunction, boolean canClaim, boolean pending, int yTexOffset)
        {
            this.textFunction = textFunction;
            this.canClaim = canClaim;
            this.pending = pending;
            this.yTexOffset = yTexOffset;
        }

        public Component getText(SkillInstance instance, int level) { return textFunction.apply(instance, level); }

        public boolean canClaim() { return canClaim; }

        public boolean pending() { return pending; }

        public int yTexOffset() { return yTexOffset; }

    }

}
