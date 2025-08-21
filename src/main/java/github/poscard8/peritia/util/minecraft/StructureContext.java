package github.poscard8.peritia.util.minecraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class StructureContext implements JsonSerializable<StructureContext>
{
    protected final Map<TagKey<Structure>, Set<ResourceLocation>> tagMap = new HashMap<>();

    public StructureContext() {}

    public StructureContext(MinecraftServer server)
    {
        RegistryAccess registryAccess = server.registryAccess();
        registryAccess.registry(Registries.STRUCTURE).ifPresent(registry ->
                {
                    Set<TagKey<Structure>> tags = registry.getTagNames().collect(Collectors.toSet());
                    for (TagKey<Structure> tag : tags)
                    {
                        Set<ResourceLocation> keys = new HashSet<>();

                        registry.holders().forEach(holder ->
                        {
                            if (holder.is(tag)) keys.add(holder.key().location());
                        });
                        tagMap.put(tag, keys);
                    }
                }
        );
    }

    public static StructureContext empty() { return new StructureContext(); }

    public static StructureContext tryLoad(JsonObject data) { return empty().loadWithFallback(data); }

    public Map<TagKey<Structure>, Set<ResourceLocation>> tagMap() { return tagMap; }

    public Set<ResourceLocation> keysForTag(TagKey<Structure> tag) { return tagMap().getOrDefault(tag, new HashSet<>()); }

    @Override
    public StructureContext fallback() { return empty(); }

    @Override
    public StructureContext load(JsonObject data)
    {
        for (Map.Entry<String, JsonElement> entry : data.entrySet())
        {
            String string = entry.getKey();
            JsonElement value = entry.getValue();
            ResourceLocation tagKey = ResourceLocation.tryParse(string);

            if (tagKey == null || !value.isJsonArray()) continue;

            TagKey<Structure> tag = TagKey.create(Registries.STRUCTURE, tagKey);
            Set<ResourceLocation> keys = new HashSet<>();
            JsonArray jsonArray = value.getAsJsonArray();

            for (JsonElement element : jsonArray)
            {
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())
                {
                    String string2 = element.getAsString();
                    ResourceLocation key = ResourceLocation.tryParse(string2);
                    if (key != null) keys.add(key);
                }
            }
            tagMap.put(tag, keys);
        }

        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();
        for (Map.Entry<TagKey<Structure>, Set<ResourceLocation>> entry : tagMap.entrySet())
        {
            ResourceLocation tagKey = entry.getKey().location();
            Set<ResourceLocation> keys = entry.getValue();
            JsonArray jsonArray = new JsonArray();

            for (ResourceLocation key : keys) jsonArray.add(key.toString());
            JsonHelper.write(data, tagKey.toString(), jsonArray);
        }
        return data;
    }

}
