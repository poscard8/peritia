package github.poscard8.peritia.reward;

import com.google.gson.JsonObject;
import github.poscard8.peritia.util.serialization.Loadable;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;
import java.util.function.Function;

public class RewardType<T extends Reward>
{
    protected final Function<JsonObject, T> decoder;
    protected final int priority;
    protected final Set<String> keywords;
    protected ResourceLocation key = Loadable.EMPTY_KEY;

    public RewardType(Function<JsonObject, T> decoder, int priority, String... keywords)
    {
        this.decoder = decoder;
        this.priority = priority;
        this.keywords = Set.of(keywords);
    }

    public T load(JsonObject data) { return decoder.apply(data); }

    public int priority() { return priority; }

    public Set<String> keywords() { return keywords; }

    public boolean check(JsonObject data)
    {
        for (String key : data.keySet()) if (keywords().contains(key)) return true;
        return false;
    }

    public ResourceLocation key() { return key; }

    public boolean isEmpty() { return key().equals(Loadable.EMPTY_KEY); }

    public void assignKey(ResourceLocation key) { this.key = key; }

}
