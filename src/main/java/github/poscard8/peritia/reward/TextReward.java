package github.poscard8.peritia.reward;

import com.google.gson.JsonObject;
import github.poscard8.peritia.registry.PeritiaRewardTypes;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.PeritiaHelper;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.skill.AtFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class TextReward extends Reward
{
    protected Component text;

    public TextReward(AtFunction at, Component text)
    {
        super(at);
        this.text = text;
    }

    public static TextReward empty() { return new TextReward(AtFunction.empty(), Component.empty()); }

    public static TextReward tryLoad(JsonObject data)
    {
        return empty().loadWithFallback(data) instanceof TextReward textReward ? textReward : empty();
    }

    @Override
    public RewardType<?> type() { return PeritiaRewardTypes.TEXT.get(); }

    @Override
    public TextReward multiplyBy(int multiplier) { return this; }

    @Override
    public Component getText(Skill skill, int level) { return text; }

    @Override
    public boolean shouldDisplayText(Skill skill, int level) { return true; }

    @Override
    public void award(ServerPlayer player) {}

    @Override
    public Reward load(JsonObject data)
    {
        this.at = JsonHelper.readElementSerializable(data, "at", AtFunction::tryLoad, at);
        this.text = PeritiaHelper.deserializeText(data);

        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = PeritiaHelper.serializeText(text);
        JsonHelper.write(data, "at", at);

        return data;
    }

}
