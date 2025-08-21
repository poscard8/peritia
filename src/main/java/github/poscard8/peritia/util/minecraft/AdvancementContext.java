package github.poscard8.peritia.util.minecraft;

import com.google.gson.JsonObject;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AdvancementContext implements JsonSerializable<AdvancementContext>
{
    protected final Map<ResourceLocation, Component> nameMap = new HashMap<>();

    public AdvancementContext() {}

    public AdvancementContext(MinecraftServer server)
    {
        for (Advancement advancement : server.getAdvancements().getAllAdvancements())
        {
            if (advancement.getId().getPath().contains("recipes/")) continue;

            DisplayInfo displayInfo = advancement.getDisplay();
            Component name = displayInfo == null ? Component.empty() : displayInfo.getTitle();
            nameMap.put(advancement.getId(), name);
        }
    }

    public static AdvancementContext empty() { return new AdvancementContext(); }

    public static AdvancementContext tryLoad(JsonObject data) { return empty().loadWithFallback(data); }

    public Set<ResourceLocation> keys() { return nameMap.keySet(); }

    public Component nameOf(ResourceLocation advancementKey) { return nameOf(advancementKey, true); }

    public Component nameOf(ResourceLocation advancementKey, boolean grayscale)
    {
        Component text = nameMap.getOrDefault(advancementKey, Component.empty());
        return grayscale ? text.plainCopy().withStyle(ChatFormatting.GRAY) : text;
    }

    @Override
    public AdvancementContext fallback() { return empty(); }

    @Override
    public AdvancementContext load(JsonObject data)
    {
        for (String string : data.keySet())
        {
            ResourceLocation key = ResourceLocation.tryParse(string);
            if (key == null) continue;

            Component name = JsonHelper.readText(data, string);
            nameMap.put(key, name);
        }
        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();

        for (Map.Entry<ResourceLocation, Component> entry : nameMap.entrySet())
        {
            String stringKey = entry.getKey().toString();
            Component name = entry.getValue();
            JsonHelper.write(data, stringKey, name);
        }
        return data;
    }
}
