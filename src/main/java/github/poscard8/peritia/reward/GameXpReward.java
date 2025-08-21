package github.poscard8.peritia.reward;

import com.google.gson.JsonObject;
import github.poscard8.peritia.registry.PeritiaRewardTypes;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.PeritiaHelper;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.skill.AtFunction;
import github.poscard8.peritia.util.text.PeritiaTexts;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class GameXpReward extends Reward
{
    protected int xp;

    public GameXpReward(AtFunction at, int xp)
    {
        super(at);
        this.xp = Math.max(0, xp);
    }

    public static GameXpReward empty() { return new GameXpReward(AtFunction.empty(), 0); }

    public static GameXpReward tryLoad(JsonObject data)
    {
        return empty().loadWithFallback(data) instanceof GameXpReward xpReward ? xpReward : empty();
    }

    @Override
    public RewardType<?> type() { return PeritiaRewardTypes.GAME_XP.get(); }

    public int xp() { return xp; }

    public boolean isEmpty() { return xp() == 0; }

    @Override
    public GameXpReward multiplyBy(int multiplier) { return new GameXpReward(at(), xp() * multiplier); }

    @Override
    public boolean shouldDisplayText(Skill skill, int level) { return !isEmpty(); }

    @Override
    public Component getText(Skill skill, int level) { return PeritiaTexts.minecraftXp(xp); }

    @Override
    public void award(ServerPlayer player) { PeritiaHelper.giveExperienceToPlayer(player, xp()); }

    @Override
    public Reward fallback() { return empty(); }

    @Override
    public Reward load(JsonObject data)
    {
        AtFunction at = JsonHelper.readElementSerializable(data, "at", AtFunction::tryLoad, this.at);
        int xp = JsonHelper.readInt(data, "xp", this.xp);

        return new GameXpReward(at, xp);
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();
        JsonHelper.write(data, "at", at);
        JsonHelper.write(data, "xp", xp);

        return data;
    }

}
