package github.poscard8.peritia.reward;

import com.google.gson.JsonObject;
import github.poscard8.peritia.registry.PeritiaRewardTypes;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.skill.AtFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class EmptyReward extends Reward
{
    public EmptyReward(AtFunction at) { super(at); }

    public static EmptyReward empty() { return new EmptyReward(AtFunction.empty()); }

    @SuppressWarnings("unused")
    public static EmptyReward tryLoad(JsonObject data) { return empty(); }

    @Override
    public RewardType<?> type() { return PeritiaRewardTypes.EMPTY.get(); }

    @Override
    public void award(ServerPlayer player) {}

    @Override
    public EmptyReward multiplyBy(int multiplier) { return this; }

    @Override
    public boolean shouldDisplayText(Skill skill, int level) { return false; }

    @Override
    public Component getText(Skill skill, int level) { return Component.empty(); }

    @Override
    public boolean hasText() { return false; }

    @Override
    public Reward load(JsonObject data) { return this; }

    @Override
    public JsonObject save() { return new JsonObject(); }

}
