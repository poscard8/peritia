package github.poscard8.peritia.util.xpsource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import github.poscard8.peritia.util.serialization.ElementSerializable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class EntityInputs implements Iterable<EntityInput>, Predicate<EntityType<?>>, ElementSerializable<EntityInputs>
{
    protected List<EntityInput> inputs;
    protected boolean acceptAll = false;

    public EntityInputs(List<EntityInput> inputs) { this.inputs = inputs; }

    public List<EntityInput> inputs() { return inputs; }

    public boolean acceptsAll() { return acceptAll; }

    public static EntityInputs empty() { return new EntityInputs(new ArrayList<>()); }

    public static EntityInputs tryLoad(JsonElement data) { return empty().loadWithFallback(data); }

    public static EntityInputs acceptAll()
    {
        EntityInputs inputs = empty();
        inputs.acceptAll = true;
        return inputs;
    }

    public boolean test(Entity entity) { return test(entity.getType()); }

    @Override
    public boolean test(EntityType<?> entityType)
    {
        if (acceptsAll()) return true;

        for (EntityInput input : this)
        {
            if (input.test(entityType)) return true;
        }
        return false;
    }

    @Override
    @NotNull
    public Iterator<EntityInput> iterator() { return inputs().iterator(); }

    public boolean isEmpty() { return inputs().isEmpty() && !acceptsAll(); }

    @Override
    public EntityInputs fallback() { return empty(); }

    @Override
    public EntityInputs load(JsonElement data)
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
                    EntityInput input = EntityInput.tryLoad(string);
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
        for (EntityInput input : this) data.add(input.save());

        return data;
    }


}
