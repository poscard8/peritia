package github.poscard8.peritia.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import github.poscard8.peritia.util.serialization.PeritiaResourceHandler;
import github.poscard8.peritia.xpsource.DataXpSource;
import github.poscard8.peritia.xpsource.GuiXpSource;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.XpSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@ParametersAreNonnullByDefault
@SuppressWarnings("unused")
public class XpSourceHandler extends SimpleJsonResourceReloadListener implements PeritiaResourceHandler
{
    protected static final String KEY = "peritia/xp_source";

    public List<XpSource> xpSources = new ArrayList<>();

    public XpSourceHandler() { super(GSON, KEY); }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller filler)
    {
        xpSources = new ArrayList<>();
        int index = 1;

        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet())
        {
            ResourceLocation key = entry.getKey();

            if (!entry.getValue().isJsonObject())
            {
                MOD_LOGGER.error("Parsing error loading xp source {}", key);
                continue;
            }

            JsonObject jsonObject = entry.getValue().getAsJsonObject();

            try
            {
                XpSource xpSource = XpSource.tryLoad(key, jsonObject);

                if (xpSource.isInvalid())
                {
                    MOD_LOGGER.error("Xp source {} is invalid, therefore not loaded", key);
                    continue;
                }

                if (!xpSource.isEmpty()) xpSources.add(xpSource);
            }
            catch (IllegalArgumentException | JsonParseException jsonParseException)
            {
                MOD_LOGGER.error("Parsing error loading xp source {}", key, jsonParseException);
            }
        }

        xpSources = xpSources.stream().sorted(XpSource::compareTo).collect(Collectors.toList());
        MOD_LOGGER.info("Loaded {} xp sources", values().size());
    }

    @Override
    public void revalidate()
    {
        MOD_LOGGER.info("Revalidating xp sources");

        int initialSize = xpSources.size();
        Iterator<XpSource> iterator = xpSources.iterator();

        while (iterator.hasNext())
        {
            XpSource xpSource = iterator.next();

            if (xpSource.doesNotMeetConditions())
            {
                MOD_LOGGER.info("Removing xp source {} as it does not meet conditions", xpSource.key());
                iterator.remove();
            }
        }
        int newSize = xpSources.size();
        int removed = initialSize - newSize;

        if (removed > 0) MOD_LOGGER.info("{} xp sources removed, {} xp sources remain", removed, newSize);
    }

    @Nullable
    public XpSource byKey(ResourceLocation key)
    {
        for (XpSource xpSource : values())
        {
            if (xpSource.key().equals(key)) return xpSource;
        }
        return null;
    }

    public <T extends XpSource> List<T> byType(Supplier<XpSourceType<T>> typeGetter) { return byType(typeGetter.get()); }

    @SuppressWarnings("unchecked")
    public <T extends XpSource> List<T> byType(XpSourceType<T> type)
    {
        return values().stream().filter(xpSource -> xpSource.type() == type).map(xpSource -> (T) xpSource).toList();
    }

    public List<ResourceLocation> keys()
    {
        List<ResourceLocation> keys = new ArrayList<>();

        for (XpSource xpSource : values()) keys.add(xpSource.key());
        return keys;
    }

    public List<XpSource> values() { return xpSources; }

    public List<GuiXpSource> guiXpSources()
    {
        return values().stream().filter(xpSource -> xpSource instanceof GuiXpSource).map(GuiXpSource.class::cast).toList();
    }

    public List<DataXpSource> dataXpSources()
    {
        return values().stream().filter(xpSource -> xpSource instanceof DataXpSource dataXpSource && dataXpSource.isValidDataSource())
                .map(DataXpSource.class::cast).toList();
    }

}
