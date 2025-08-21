package github.poscard8.peritia.util.minecraft;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class SimpleAttributeMap implements JsonSerializable<SimpleAttributeMap>
{
    protected final Map<Attribute, Double> valueMap = new HashMap<>();

    public SimpleAttributeMap() {}

    public SimpleAttributeMap(ServerPlayer player)
    {
        for (Attribute attribute : ForgeRegistries.ATTRIBUTES.getValues())
        {
            try
            {
                double value = player.getAttributeValue(attribute);
                valueMap().put(attribute, value);
            }
            catch (Exception ignored) {}
        }
    }

    public static SimpleAttributeMap empty() { return new SimpleAttributeMap(); }

    public static SimpleAttributeMap tryLoad(JsonObject data) { return empty().loadWithFallback(data); }

    public Map<Attribute, Double> valueMap() { return valueMap; }

    public double valueOf(Attribute attribute) { return valueMap.getOrDefault(attribute, 0.0D); }

    public int roundedValueOf(Attribute attribute) { return Math.round((float) valueOf(attribute)); }

    @Override
    public SimpleAttributeMap fallback() { return empty(); }

    @Override
    public SimpleAttributeMap load(JsonObject data)
    {
        for (Map.Entry<String, JsonElement> entry : data.entrySet())
        {
            ResourceLocation key = ResourceLocation.tryParse(entry.getKey());
            if (key == null) continue;

            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(key);
            if (attribute == null) continue;

            double value = entry.getValue().getAsDouble();
            valueMap.put(attribute, value);
        }
        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();
        for (Attribute attribute : valueMap.keySet())
        {
            ResourceLocation key = ForgeRegistries.ATTRIBUTES.getKey(attribute);
            if (key != null) JsonHelper.write(data, key.toString(), valueOf(attribute));
        }
        return data;
    }

}
