package github.poscard8.peritia.util.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import github.poscard8.peritia.util.PeritiaHelper;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.function.Function;

@SuppressWarnings("unused")
public class JsonHelper
{
    public static <T> T readJsonSerializable(JsonObject jsonObject, String key, Function<JsonObject, T> function, T initial)
    {
        return jsonObject.has(key) ? function.apply(readObject(jsonObject, key)) : initial;
    }

    public static <T> T readArraySerializable(JsonObject jsonObject, String key, Function<JsonArray, T> function, T initial)
    {
        return jsonObject.has(key) ? function.apply(readArray(jsonObject, key)) : initial;
    }

    public static <T> T readElementSerializable(JsonObject jsonObject, String key, Function<JsonElement, T> function, T initial)
    {
        return jsonObject.has(key) ? function.apply(jsonObject.get(key)) : initial;
    }

    public static <T> T readStringSerializable(JsonObject jsonObject, String key, Function<String, T> function, T initial)
    {
        return jsonObject.has(key) ? function.apply(readString(jsonObject, key)) : initial;
    }

    public static <T> TagKey<T> readTag(JsonObject jsonObject, String key, ResourceKey<Registry<T>> resourceKey)
    {
        ResourceLocation resource = readResource(jsonObject, key);
        return TagKey.create(resourceKey, resource);
    }
    
    public static <T> T readRegistrable(JsonObject jsonObject, String key, IForgeRegistry<T> registry, T initial)
    {
        if (!jsonObject.has(key)) return initial;

        ResourceLocation resource = readResource(jsonObject, key);
        T value = registry.getValue(resource);

        return value == null ? initial : value;
    }

    public static ItemStack readItem(JsonObject jsonObject, String key, ItemStack initial)
    {
        return jsonObject.has(key) ? ShapedRecipe.itemStackFromJson(readObject(jsonObject, key)) : initial;
    }

    public static Ingredient readIngredient(JsonObject jsonObject, String key) { return readIngredient(jsonObject, key, Ingredient.EMPTY); }

    public static Ingredient readIngredient(JsonObject jsonObject, String key, Ingredient initial)
    {
        return jsonObject.has(key) ? Ingredient.fromJson(readElement(jsonObject, key)) : initial;
    }

    public static MutableComponent readText(JsonObject jsonObject, String key) { return readText(jsonObject, key, Component.empty()); }

    public static MutableComponent readText(JsonObject jsonObject, String key, Component initial) { return readText(jsonObject, key, initial.copy()); }

    public static MutableComponent readText(JsonObject jsonObject, String key, MutableComponent initial)
    {
        return jsonObject.has(key) ? PeritiaHelper.deserializeText(readObject(jsonObject, key)) : initial;
    }

    public static ResourceLocation readResource(JsonObject jsonObject, String key) { return readResource(jsonObject, key, Loadable.EMPTY_KEY); }

    public static ResourceLocation readResource(JsonObject jsonObject, String key, ResourceLocation initial)
    {
        return jsonObject.has(key) ? ResourceLocation.tryParse(readString(jsonObject, key)) : initial;
    }

    public static JsonObject readObject(JsonObject jsonObject, String key) { return readObject(jsonObject, key, new JsonObject()); }

    public static JsonObject readObject(JsonObject jsonObject, String key, JsonObject initial)
    {
        return jsonObject.has(key) ? jsonObject.get(key).getAsJsonObject() : initial;
    }

    public static JsonArray readArray(JsonObject jsonObject, String key) { return readArray(jsonObject, key, new JsonArray()); }

    public static JsonArray readArray(JsonObject jsonObject, String key, JsonArray initial)
    {
        return jsonObject.has(key) ? jsonObject.get(key).getAsJsonArray() : initial;
    }

    public static JsonElement readElement(JsonObject jsonObject, String key) { return readElement(jsonObject, key, JsonNull.INSTANCE); }

    public static JsonElement readElement(JsonObject jsonObject, String key, JsonElement initial)
    {
        return jsonObject.has(key) ? jsonObject.get(key) : initial;
    }

    public static String readString(JsonObject jsonObject, String key) { return readString(jsonObject, key, ""); }

    public static String readString(JsonObject jsonObject, String key, String initial)
    {
        return jsonObject.has(key) ? jsonObject.get(key).getAsString() : initial;
    }

    public static boolean[] readBooleanArray(JsonObject jsonObject, String key, boolean[] initial)
    {
        if (jsonObject.has(key))
        {
            String defaultString = "0".repeat(initial.length);
            String string = readString(jsonObject, key, defaultString);
            boolean[] copy = new boolean[initial.length];

            for (int i = 0; i < copy.length; i++)
            {
                char c;

                try
                {
                    c = string.charAt(i);
                }
                catch (IndexOutOfBoundsException exception) { c = '0'; }

                copy[i] = c == '1';
            }
            return copy;
        }
        return initial;
    }

    public static double readDouble(JsonObject jsonObject, String key, double initial)
    {
        return jsonObject.has(key) ? jsonObject.get(key).getAsDouble() : initial;
    }

    public static float readFloat(JsonObject jsonObject, String key, float initial)
    {
        return jsonObject.has(key) ? jsonObject.get(key).getAsFloat() : initial;
    }

    public static int readInt(JsonObject jsonObject, String key, int initial)
    {
        return jsonObject.has(key) ? jsonObject.get(key).getAsInt() : initial;
    }

    public static short readShort(JsonObject jsonObject, String key, short initial)
    {
        return jsonObject.has(key) ? jsonObject.get(key).getAsShort() : initial;
    }

    public static boolean readBoolean(JsonObject jsonObject, String key, boolean initial)
    {
        return jsonObject.has(key) ? jsonObject.get(key).getAsBoolean() : initial;
    }


    public static <T extends JsonSerializable<T>> void write(JsonObject jsonObject, String key, T value) { write(jsonObject, key, value.save()); }

    public static <T extends ArraySerializable<T>> void write(JsonObject jsonObject, String key, T value) { write(jsonObject, key, value.save()); }

    public static <T extends ElementSerializable<T>> void write(JsonObject jsonObject, String key, T value) { write(jsonObject, key, value.save()); }

    public static <T extends StringSerializable<T>> void write(JsonObject jsonObject, String key, T value) { write(jsonObject, key, value.save()); }

    public static <T> void write(JsonObject jsonObject, String key, TagKey<T> value) { write(jsonObject, key, value.location()); }

    public static <T> void write(JsonObject jsonObject, String key, T value, IForgeRegistry<T> registry)
    {
        ResourceLocation resource = registry.getKey(value) == null ? registry.getDefaultKey() : registry.getKey(value);

        assert resource != null;
        write(jsonObject, key, resource);
    }

    public static void write(JsonObject jsonObject, String key, ItemStack value) { write(jsonObject, key, PeritiaHelper.serializeItem(value)); }

    public static void write(JsonObject jsonObject, String key, Ingredient value) { write(jsonObject, key, value.toJson()); }

    public static void write(JsonObject jsonObject, String key, Component value) { write(jsonObject, key, PeritiaHelper.serializeText(value)); }

    public static void write(JsonObject jsonObject, String key, ResourceLocation value) { write(jsonObject, key, value.toString()); }

    public static void write(JsonObject jsonObject, String key, JsonObject value) { jsonObject.add(key, value); }

    public static void write(JsonObject jsonObject, String key, JsonArray value) { jsonObject.add(key, value); }

    public static void write(JsonObject jsonObject, String key, JsonElement value) { jsonObject.add(key, value); }

    public static void write(JsonObject jsonObject, String key, String value) { jsonObject.addProperty(key, value); }

    public static void write(JsonObject jsonObject, String key, boolean[] value)
    {
        StringBuilder builder = new StringBuilder();

        for (boolean bool : value)
        {
            char c = bool ? '1' : '0';
            builder.append(c);
        }

        write(jsonObject, key, builder.toString());
    }

    public static void write(JsonObject jsonObject, String key, double value) { jsonObject.addProperty(key, value); }

    public static void write(JsonObject jsonObject, String key, float value) { jsonObject.addProperty(key, value); }

    public static void write(JsonObject jsonObject, String key, int value) { jsonObject.addProperty(key, value); }

    public static void write(JsonObject jsonObject, String key, short value) { jsonObject.addProperty(key, value); }

    public static void write(JsonObject jsonObject, String key, boolean value) { jsonObject.addProperty(key, value); }

}
