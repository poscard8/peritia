package github.poscard8.peritia.skill;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import github.poscard8.peritia.reward.Reward;
import github.poscard8.peritia.util.serialization.ArraySerializable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SkillRewards implements Iterable<Reward>, ArraySerializable<SkillRewards>
{
    protected List<Reward> rewards;

    public SkillRewards(List<Reward> rewards) { this.rewards = rewards; }

    public static SkillRewards empty() { return new SkillRewards(new ArrayList<>()); }

    public static SkillRewards tryLoad(JsonArray data) { return empty().loadWithFallback(data); }

    public List<Reward> rewards() { return rewards; }

    @Override
    @NotNull
    public Iterator<Reward> iterator() { return rewards().iterator(); }

    public SkillRewards copy() { return new SkillRewards(new ArrayList<>(rewards())); }

    @Override
    public SkillRewards fallback() { return empty(); }

    @Override
    public SkillRewards load(JsonArray data)
    {
        for (JsonElement element : data)
        {
            if (element.isJsonObject()) rewards.add(Reward.tryLoad(element.getAsJsonObject()));
        }
        return this;
    }

    @Override
    public JsonArray save()
    {
        JsonArray data = new JsonArray();
        for (Reward reward : this) data.add(reward.save());

        return data;
    }

}
