package github.poscard8.peritia.util.xpsource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import github.poscard8.peritia.util.serialization.ElementSerializable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class ItemInputs implements Iterable<ItemInput>, Predicate<ItemStack>, ElementSerializable<ItemInputs>
{
    protected List<ItemInput> inputs;
    protected boolean acceptAll = false;

    public ItemInputs(List<ItemInput> inputs) { this.inputs = inputs; }

    public List<ItemInput> inputs() { return inputs; }

    public boolean acceptsAll() { return acceptAll; }

    public static ItemInputs empty() { return new ItemInputs(new ArrayList<>()); }

    public static ItemInputs tryLoad(JsonElement data) { return empty().loadWithFallback(data); }

    public static ItemInputs acceptAll()
    {
        ItemInputs inputs = empty();
        inputs.acceptAll = true;
        return inputs;
    }

    @Override
    public boolean test(ItemStack stack)
    {
        if (acceptsAll()) return true;

        for (ItemInput input : this)
        {
            if (input.test(stack)) return true;
        }
        return false;
    }

    @Override
    @NotNull
    public Iterator<ItemInput> iterator() { return inputs().iterator(); }

    public boolean isEmpty() { return inputs().isEmpty() && !acceptsAll(); }

    @Override
    public ItemInputs fallback() { return empty(); }

    @Override
    public ItemInputs load(JsonElement data)
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
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())
                {
                    String string = element.getAsString();
                    ItemInput input = ItemInput.tryLoad(string);
                    if (input.isValid()) inputs.add(input);
                }
            }
        }
        return this;
    }

    @Override
    public JsonElement save()
    {
        if (acceptAll) return Input.ALL_NAME_PRIMITIVE;

        JsonArray data = new JsonArray();
        for (ItemInput input : this) data.add(input.save());

        return data;
    }


}
