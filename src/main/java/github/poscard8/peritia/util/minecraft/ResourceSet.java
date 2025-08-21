package github.poscard8.peritia.util.minecraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import github.poscard8.peritia.util.serialization.ArraySerializable;
import github.poscard8.peritia.util.serialization.Loadable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

public class ResourceSet implements Iterable<ResourceLocation>, ArraySerializable<ResourceSet>
{
    protected final Set<ResourceLocation> set = new HashSet<>();

    ResourceSet() {}

    public static ResourceSet empty() { return new ResourceSet(); }

    public static ResourceSet tryLoad(JsonArray data) { return empty().loadWithFallback(data); }

    public Set<ResourceLocation> set() { return set; }

    public int size() { return set().size(); }

    public boolean contains(ResourceLocation key) { return set().contains(key); }

    public <T extends Loadable> boolean contains(T object) { return contains(object, Loadable::key); }

    public <T> boolean contains(T object, Function<T, ResourceLocation> function) { return contains(function.apply(object)); }

    public boolean containsAnyKey(Iterable<ResourceLocation> iterable)
    {
        for (ResourceLocation key : iterable)
        {
            if (contains(key)) return true;
        }
        return false;
    }

    public <T extends Loadable> boolean containsAny(Iterable<T> iterable) { return containsAny(iterable, Loadable::key); }

    public <T> boolean containsAny(Iterable<T> iterable, Function<T, ResourceLocation> function)
    {
        for (T object : iterable)
        {
            if (contains(object, function)) return true;
        }
        return false;
    }

    public boolean containsAllKeys(Iterable<ResourceLocation> iterable)
    {
        for (ResourceLocation key : iterable)
        {
            if (!contains(key)) return false;
        }
        return true;
    }

    public <T extends Loadable> boolean containsAll(Iterable<T> iterable) { return containsAll(iterable, Loadable::key); }

    public <T> boolean containsAll(Iterable<T> iterable, Function<T, ResourceLocation> function)
    {
        for (T object : iterable)
        {
            if (!contains(object, function)) return false;
        }
        return true;
    }

    public void add(ResourceLocation... keys)
    {
        for (ResourceLocation key : keys) set().add(key);
    }

    public <T extends Loadable> void add(Iterable<T> iterable) { add(iterable, Loadable::key); }

    public <T> void add(Iterable<T> iterable, Function<T, ResourceLocation> function)
    {
        for (T object : iterable) set().add(function.apply(object));
    }

    public void remove(ResourceLocation... keys)
    {
        for (ResourceLocation key : keys) set().remove(key);
    }

    public <T extends Loadable> void remove(Iterable<T> iterable) { remove(iterable, Loadable::key); }

    public <T> void remove(Iterable<T> iterable, Function<T, ResourceLocation> function)
    {
        for (T object : iterable) set().remove(function.apply(object));
    }

    @Override
    @NotNull
    public Iterator<ResourceLocation> iterator() { return set().iterator(); }

    @Override
    public ResourceSet fallback() { return empty(); }

    @Override
    public ResourceSet load(JsonArray data)
    {
        for (JsonElement element : data)
        {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())
            {
                String string = element.getAsString();
                ResourceLocation key = ResourceLocation.tryParse(string);
                if (key != null) set.add(key);
            }
        }
        return this;
    }

    @Override
    public JsonArray save()
    {
        JsonArray data = new JsonArray();
        for (ResourceLocation key : this) data.add(key.toString());
        return data;
    }


}
