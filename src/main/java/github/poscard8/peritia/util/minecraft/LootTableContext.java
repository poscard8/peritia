package github.poscard8.peritia.util.minecraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import github.poscard8.peritia.util.serialization.ArraySerializable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.loot.LootDataType;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

public class LootTableContext implements ArraySerializable<LootTableContext> {

    protected Collection<ResourceLocation> keys;

    public LootTableContext() { this.keys = new HashSet<>(); }

    public LootTableContext(MinecraftServer server)
    {
        this.keys = server.getLootData().getKeys(LootDataType.TABLE);
    }

    public static LootTableContext empty() { return new LootTableContext(); }

    public static LootTableContext tryLoad(JsonArray data) { return empty().loadWithFallback(data); }

    public Collection<ResourceLocation> keys() { return keys; }

    public Collection<ResourceLocation> chestKeys() { return chestKeys(true); }

    public Collection<ResourceLocation> chestKeys(boolean modify)
    {
        return keys().stream().filter(key -> key.getPath().contains("chests/"))
                .map(key ->
                {
                    String newPath = key.getPath().replaceAll("chests/", "");
                    return modify ? new ResourceLocation(key.getNamespace(), newPath) : key;
                })
                .collect(Collectors.toSet());
    }

    @Override
    public LootTableContext fallback() { return empty(); }

    @Override
    public LootTableContext load(JsonArray data)
    {
        for (JsonElement element : data)
        {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())
            {
                String string = element.getAsString();
                ResourceLocation key = ResourceLocation.tryParse(string);
                if (key != null) keys.add(key);
            }
        }
        return this;
    }

    @Override
    public JsonArray save()
    {
        JsonArray data = new JsonArray();
        for (ResourceLocation key : keys) data.add(key.toString());

        return data;
    }
}
