package github.poscard8.peritia.ascension;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.registry.PeritiaRewardTypes;
import github.poscard8.peritia.reward.ItemReward;
import github.poscard8.peritia.reward.Reward;
import github.poscard8.peritia.reward.RewardLike;
import github.poscard8.peritia.skill.SkillAttributeInstance;
import github.poscard8.peritia.skill.SkillAttributes;
import github.poscard8.peritia.skill.SkillRewards;
import github.poscard8.peritia.util.skill.LevelXpFunction;
import github.poscard8.peritia.util.skill.Polynomial;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public sealed class AscensionSystem permits ServerAscensionSystem, ClientAscensionSystem
{
    protected static Item DEFAULT_ICON = Items.NETHER_STAR;
    protected static SoundEvent DEFAULT_SOUND = SoundEvents.GENERIC_EXPLODE;
    protected static SimpleParticleType DEFAULT_PARTICLE_TYPE = null;
    protected static int DEFAULT_PARTICLE_COUNT = 24;
    protected static LevelXpFunction DEFAULT_XP_FUNCTION = LevelXpFunction.of(Polynomial.of(0.00012D, 0.027D, -55.5D, 12055.47288D), false);
    protected static SkillAttributes DEFAULT_ATTRIBUTES = SkillAttributes.empty();
    protected static SkillRewards DEFAULT_REWARDS = SkillRewards.empty();

    protected Item icon = getDefaultIcon();
    protected SoundEvent sound = getDefaultSound();
    protected SimpleParticleType particleType = getDefaultParticleType();
    protected int particleCount = getDefaultParticleCount();
    protected LevelXpFunction xpFunction = getDefaultXpFunction();
    protected SkillAttributes attributes = getDefaultAttributes();
    protected SkillRewards rewards = getDefaultRewards();

    AscensionSystem() {}

    public static AscensionSystem empty() { return new AscensionSystem(); }

    public static Item getDefaultIcon() { return DEFAULT_ICON; }

    static void setDefaultIcon(Item icon) { DEFAULT_ICON = icon; }

    public static SoundEvent getDefaultSound() { return DEFAULT_SOUND; }

    static void setDefaultSound(SoundEvent defaultSound) { DEFAULT_SOUND = defaultSound; }

    public static SimpleParticleType getDefaultParticleType()
    {
        return DEFAULT_PARTICLE_TYPE == null ? (SimpleParticleType) ForgeRegistries.PARTICLE_TYPES.getValue(Peritia.asResource("ascension")) : DEFAULT_PARTICLE_TYPE;
    }

    static void setDefaultParticleType(SimpleParticleType particleType) { DEFAULT_PARTICLE_TYPE = particleType; }

    public static int getDefaultParticleCount() { return DEFAULT_PARTICLE_COUNT; }

    static void setDefaultParticleCount(int particleCount) { DEFAULT_PARTICLE_COUNT = particleCount; }

    public static LevelXpFunction getDefaultXpFunction() { return DEFAULT_XP_FUNCTION; }

    static void setDefaultXpFunction(LevelXpFunction xpFunction) { DEFAULT_XP_FUNCTION = xpFunction; }

    public static SkillAttributes getDefaultAttributes() { return DEFAULT_ATTRIBUTES; }

    static void setDefaultAttributes(SkillAttributes attributes) { DEFAULT_ATTRIBUTES = attributes; }

    public static SkillRewards getDefaultRewards() { return DEFAULT_REWARDS; }

    static void setDefaultRewards(SkillRewards rewards) { DEFAULT_REWARDS = rewards; }

    public Item icon() { return icon; }

    public SoundEvent sound() { return sound; }

    public SimpleParticleType particleType() { return particleType; }

    public int particleCount() { return particleCount; }

    public LevelXpFunction xpFunction() { return xpFunction; }

    public int evaluateLevel(int xp)
    {
        int level = 0;
        boolean pass = true;

        while (pass)
        {
            int nextLevel = level + 1;
            int neededXP = xpFunction().getNeededXp(0, nextLevel);

            pass = xp >= neededXP;
            if (pass) level++;
        }
        return level;
    }

    public SkillAttributes attributes() { return attributes; }

    public SkillRewards rewards() { return rewards; }

    public List<ItemReward> itemRewards()
    {
        return rewards().rewards().stream().filter(reward -> reward.type() == PeritiaRewardTypes.ITEM.get()).map(reward -> (ItemReward) reward).toList();
    }

    public List<SkillAttributeInstance> getAttributesFor(int fromInclusive, int toInclusive)
    {
        List<SkillAttributeInstance> filtered = new ArrayList<>();
        for (SkillAttributeInstance attributeInstance : attributes())
        {
            int count = attributeInstance.at().countInclusive(fromInclusive, toInclusive);
            if (count > 0)
            {
                SkillAttributeInstance multiplied = attributeInstance.multiplyBy(count);
                filtered.add(multiplied);
            }
        }
        return filtered;
    }

    public List<Reward> getRewardsFor(int fromInclusive, int toInclusive)
    {
        List<Reward> filtered = new ArrayList<>();
        for (Reward reward : rewards())
        {
            int count = reward.at().countInclusive(fromInclusive, toInclusive);
            if (count > 0)
            {
                Reward multiplied = reward.multiplyBy(count);
                filtered.add(multiplied);
            }
        }
        return filtered;
    }

    public List<RewardLike<?>> getRewardLikesFor(int fromInclusive, int toInclusive)
    {
        List<RewardLike<?>> rewardLikes = new ArrayList<>();
        rewardLikes.addAll(getAttributesFor(fromInclusive, toInclusive));
        rewardLikes.addAll(getRewardsFor(fromInclusive, toInclusive));

        return rewardLikes;
    }


}
