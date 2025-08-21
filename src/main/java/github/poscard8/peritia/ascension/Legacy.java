package github.poscard8.peritia.ascension;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import github.poscard8.peritia.reward.Reward;
import github.poscard8.peritia.reward.RewardLike;
import github.poscard8.peritia.skill.SkillAttributeInstance;
import github.poscard8.peritia.skill.data.ServerSkillData;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import github.poscard8.peritia.util.skill.WeightHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Legacy implements WeightHolder, JsonSerializable<Legacy>
{
    protected static final UUID MODIFIER_UUID = UUID.fromString("335da025-1f47-477e-b7a9-f443a78af1ff");
    protected static final String MODIFIER_DESC = "Peritia ascension modifier";

    protected @NotNull AscensionSystem ascensionSystem;
    protected int pastXp;
    protected int pastScore;
    protected int ascensions;
    protected double weight;

    Legacy(@NotNull AscensionSystem ascensionSystem) { this(ascensionSystem, 0, 0, 0, 0); }

    Legacy(@NotNull AscensionSystem ascensionSystem, int pastXp, int pastScore, int ascensions, double weight)
    {
        this.ascensionSystem = ascensionSystem;
        this.pastXp = pastXp;
        this.pastScore = pastScore;
        this.ascensions = ascensions;
        this.weight = weight;
    }

    public static Legacy empty() { return new Legacy(AscensionSystem.empty()); }

    public static Legacy tryLoad(JsonObject data) { return empty().loadWithFallback(data); }

    public static Legacy of(ServerPlayer player) { return ServerSkillData.of(player).legacy(); }

    public AscensionSystem ascensionSystem() { return ascensionSystem; }

    public void setAscensionSystem(@NotNull AscensionSystem ascensionSystem) { this.ascensionSystem = ascensionSystem; }

    public Map<Attribute, Double> attributeMap()
    {
        Map<Attribute, Double> map = new HashMap<>();
        for (SkillAttributeInstance attributeInstance : ascensionSystem().attributes())
        {
            Attribute attribute = attributeInstance.attribute();
            if (attribute == null) continue;

            double existingValue = map.getOrDefault(attribute, 0.0D);
            double addedValue = attributeInstance.value() * (attributeInstance.at().countInclusive(1, pastScore));
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
            Attribute attribute = entry.getKey();
            AttributeModifier modifier = new AttributeModifier(MODIFIER_UUID, () -> MODIFIER_DESC, entry.getValue(), AttributeModifier.Operation.ADDITION);
            builder.put(attribute, modifier);
        }
        return builder.build();
    }

    public int pastXp() { return pastXp; }

    public void setPastXp(int xp) { this.pastXp = xp; }

    public void addPastXp(int xp) { setPastXp(pastXp() + xp); }

    public int pastScore() { return pastScore; }

    public void setPastScore(int pastScore) { this.pastScore = pastScore; }

    public int ascensionCount() { return ascensions; }

    public void setAscensionCount(int ascensions) { this.ascensions = ascensions; }

    public void addAscension() { setAscensionCount(ascensionCount() + 1); }

    @Override
    public double weight() { return weight; }

    public void setWeight(double weight) { this.weight = weight; }

    public void addWeight(double weight) { setWeight(weight() + weight); }

    public int getPendingScore(int xp) { return ascensionSystem().evaluateLevel(xp); }

    public int getExtraScore(int xp) { return Math.max(0, getPendingScore(xp) - pastScore()); }

    public int xpRequiredForNextPoint(int xp)
    {
        int score = getPendingScore(xp);
        int next = Math.max(score, pastScore()) + 1;
        int neededForScore = ascensionSystem().xpFunction().getNeededXp(0, score);
        int extra = xp - neededForScore;

        return ascensionSystem().xpFunction().getNeededXp(score, next) - extra;
    }

    public List<RewardLike<?>> getExistingRewardLikes() { return ascensionSystem().getRewardLikesFor(1, pastScore()); }

    public List<RewardLike<?>> getRewardLikes(int pendingScore) { return getRewardLikes(pastScore(), pendingScore); }

    public List<RewardLike<?>> getRewardLikes(int oldScore, int pendingScore) { return ascensionSystem().getRewardLikesFor(oldScore + 1, pendingScore); }

    public void reset()
    {
        this.pastXp = 0;
        this.pastScore = 0;
        this.ascensions = 0;
        this.weight = 0;
    }

    public void maxOut()
    {
        this.pastScore = Math.max(1000, pastScore);
        this.ascensions = Math.max(1, ascensions);
    }

    public void handleAscension(ServerPlayer player)
    {
        ServerSkillData skillData = ServerSkillData.of(player);
        int pastScore = pastScore();

        addPastXp(skillData.totalXp());
        addWeight(skillData.weight());
        if (skillData.hasExtraLegacyScore()) setPastScore(skillData.pendingLegacyScore());

        int newScore = pastScore();

        List<Reward> rewards = ascensionSystem().getRewardsFor(pastScore + 1, newScore);
        rewards.forEach(reward -> reward.award(player));

        addAscension();
        updatePlayerData(player);
    }

    public void updatePlayerData(ServerPlayer player) { ServerSkillData.of(player).putLegacy(this); }

    @Override
    public Legacy fallback() { return empty(); }

    @Override
    public Legacy load(JsonObject data)
    {
        this.pastXp = JsonHelper.readInt(data, "pastXp", pastXp);
        this.pastScore = JsonHelper.readInt(data, "pastScore", pastScore);
        this.ascensions = JsonHelper.readInt(data, "ascensions", ascensions);
        this.weight = JsonHelper.readDouble(data, "weight", weight);

        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();
        JsonHelper.write(data, "pastXp", pastXp);
        JsonHelper.write(data, "pastScore", pastScore);
        JsonHelper.write(data, "ascensions", ascensions);
        JsonHelper.write(data, "weight", weight);

        return data;
    }

}
