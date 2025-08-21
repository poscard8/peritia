package github.poscard8.peritia.util.xpsource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import github.poscard8.peritia.util.serialization.ElementSerializable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class ResourceInputs implements Iterable<ResourceInput>, Predicate<ResourceLocation>, ElementSerializable<ResourceInputs>
{
    protected List<ResourceInput> inputs;
    protected boolean acceptAll = false;

    public ResourceInputs(List<ResourceInput> inputs) { this.inputs = inputs; }

    public List<ResourceInput> inputs() { return inputs; }

    public boolean acceptsAll() { return acceptAll; }

    public static ResourceInputs empty() { return new ResourceInputs(new ArrayList<>()); }

    public static ResourceInputs tryLoad(JsonElement data) { return empty().loadWithFallback(data); }

    public static ResourceInputs acceptAll()
    {
        ResourceInputs inputs = empty();
        inputs.acceptAll = true;
        return inputs;
    }

    @Override
    public boolean test(ResourceLocation key)
    {
        if (acceptsAll()) return true;

        for (ResourceInput input : this)
        {
            if (input.test(key)) return true;
        }
        return false;
    }

    @Override
    @NotNull
    public Iterator<ResourceInput> iterator() { return inputs().iterator(); }

    public boolean isEmpty() { return inputs().isEmpty() && !acceptsAll(); }

    @Override
    public ResourceInputs fallback() { return empty(); }

    @Override
    public ResourceInputs load(JsonElement data)
    {
        if (data.isJsonPrimitive() && data.getAsJsonPrimitive().isString())
        {
            String string = data.getAsString();
            if (string.equals(Input.ALL_NAME)) this.acceptAll = true;
        }
        else if (data.isJsonArray())
        {
            JsonArray jsonArray = data.getAsJsonArray();

            for (JsonElement element : jsonArray)
            {
                ResourceInput input = ResourceInput.tryLoad(element);
                if (input.isValid()) inputs.add(input);
            }
        }
        return this;
    }

    @Override
    public JsonElement save()
    {
        if (acceptAll) return Input.ALL_NAME_PRIMITIVE;

        JsonArray data = new JsonArray();
        for (ResourceInput input : this) data.add(input.save());

        return data;
    }


}
