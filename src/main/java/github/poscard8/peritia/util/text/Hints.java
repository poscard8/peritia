package github.poscard8.peritia.util.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import github.poscard8.peritia.util.serialization.ArraySerializable;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Hints implements Iterable<Hint>, ArraySerializable<Hints>
{
    protected final List<Hint> list = new ArrayList<>();

    public Hints() {}

    public static Hints empty() { return new Hints(); }

    public static Hints tryLoad(JsonArray data) { return empty().loadWithFallback(data); }

    public List<Hint> list() { return list; }

    public int size() { return list().size(); }

    public List<Component> getTexts()
    {
        List<Component> texts = new ArrayList<>();
        for (Hint hint : this)
        {
            if (!hint.isEmpty()) texts.add(hint.text());
        }
        return texts;
    }

    @Override
    @NotNull
    public Iterator<Hint> iterator() { return list.iterator(); }

    @Override
    public Hints fallback() { return empty(); }

    @Override
    public Hints load(JsonArray data)
    {
        for (JsonElement element : data)
        {
            try
            {
                list.add(Hint.tryLoad(element));
            }
            catch (Exception ignored) {}
        }
        return this;
    }

    @Override
    public JsonArray save()
    {
        JsonArray data = new JsonArray();
        for (Hint hint : this) data.add(hint.save());

        return data;
    }
}
