package github.poscard8.peritia.util.xpsource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import github.poscard8.peritia.util.serialization.ArraySerializable;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class PropertyPredicates implements Predicate<BlockState>, ArraySerializable<PropertyPredicates>
{
    protected final List<PropertyPredicate> predicates = new ArrayList<>();

    public PropertyPredicates() {}

    public static PropertyPredicates empty() { return new PropertyPredicates(); }

    public static PropertyPredicates tryLoad(JsonArray data) { return empty().loadWithFallback(data); }

    public List<PropertyPredicate> predicates() { return predicates; }

    public Map<String, PropertyPredicate> asMap()
    {
        Map<String, PropertyPredicate> map = new HashMap<>();
        predicates().forEach(predicate -> map.put(predicate.propertyName(), predicate));

        map.remove(PropertyPredicate.EMPTY_PROPERTY_KEY);
        return map;
    }

    @Override
    public boolean test(BlockState state)
    {
        Map<String, PropertyPredicate> map = asMap();
        String serialized = BlockStateParser.serialize(state);

        for (Property<?> property : state.getProperties())
        {
            String propertyName = property.getName();
            PropertyPredicate predicate = map.get(propertyName);
            if (predicate == null) continue;

            boolean inverted = predicate.inverted();
            boolean contains = serialized.contains(predicate.fullName());
            if ((!inverted && !contains) || (inverted && contains)) return false;
        }
        return true;
    }

    public boolean isEmpty() { return predicates().isEmpty(); }

    @Override
    public PropertyPredicates fallback() { return empty(); }

    @Override
    public PropertyPredicates load(JsonArray data)
    {
        for (JsonElement element : data)
        {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())
            {
                String string = element.getAsString();
                PropertyPredicate predicate = PropertyPredicate.tryLoad(string);
                if (!predicate.isEmpty()) predicates.add(predicate);
            }
        }
        return this;
    }

    @Override
    public JsonArray save()
    {
        JsonArray data = new JsonArray();
        for (PropertyPredicate predicate : predicates) data.add(predicate.save());

        return data;
    }


}
