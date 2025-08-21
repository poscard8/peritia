package github.poscard8.peritia.reward;

import com.google.gson.JsonObject;
import github.poscard8.peritia.util.PeritiaRegistries;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import github.poscard8.peritia.util.skill.AtFunction;
import github.poscard8.peritia.util.skill.AtFunctionHolder;
import net.minecraft.server.level.ServerPlayer;

public abstract class Reward implements JsonSerializable<Reward>, AtFunctionHolder, RewardLike<Reward>
{
    protected AtFunction at;

    public Reward(AtFunction at) { this.at = at; }

    public static Reward empty() { return EmptyReward.empty(); }

    public static Reward tryLoad(JsonObject data)
    {
        for (RewardType<?> type : PeritiaRegistries.rewardTypes().getValues())
        {
            if (type.check(data)) return type.load(data);
        }
        return empty();
    }

    public abstract RewardType<?> type();

    @Override
    public int priority() { return type().priority(); }

    @Override
    public AtFunction at() { return at; }

    public abstract void award(ServerPlayer player);

    @Override
    public Reward fallback() { return empty(); }

}
