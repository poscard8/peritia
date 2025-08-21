package github.poscard8.peritia.util.serialization;

import com.google.gson.JsonArray;
import github.poscard8.peritia.Peritia;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface Loadable
{
    ResourceLocation EMPTY_KEY = Peritia.asResource("empty");
    ResourceLocation ANY_KEY = new ResourceLocation("any");
    ResourceLocation ALL_KEY = new ResourceLocation("all");
    ResourceLocation NONE_KEY = new ResourceLocation("none");
    ResourceLocation HIGHEST_KEY = new ResourceLocation("highest");
    ResourceLocation LOWEST_KEY = new ResourceLocation("lowest");
    ResourceLocation MEDIAN_KEY = new ResourceLocation("median");
    ResourceLocation RANDOM_KEY = new ResourceLocation("random");

    Set<ResourceLocation> FORBIDDEN_KEYS = Set.of(ANY_KEY, ALL_KEY, NONE_KEY, HIGHEST_KEY, LOWEST_KEY, MEDIAN_KEY, RANDOM_KEY);
    List<String> DEFAULT_NAMESPACES = new ArrayList<>(List.of("minecraft"));

    static void addDefaultNamespace(String namespace) { DEFAULT_NAMESPACES.add(namespace); }

    ResourceLocation key();

    JsonArray conditions();

    default String stringKey() { return key().toString(); }

    default boolean isValidStart(String string)
    {
        return (key().getPath().startsWith(string) && DEFAULT_NAMESPACES.contains(key().getNamespace())) || stringKey().startsWith(string);
    }

    default boolean isEmpty() { return key().equals(EMPTY_KEY); }

    default boolean isInvalid() { return hasForbiddenKey(); }

    default boolean meetsConditions() { return CraftingHelper.processConditions(conditions(), ICondition.IContext.EMPTY); }

    default boolean doesNotMeetConditions() { return !meetsConditions(); }

    default boolean hasForbiddenKey() { return FORBIDDEN_KEYS.contains(key()); }

}
