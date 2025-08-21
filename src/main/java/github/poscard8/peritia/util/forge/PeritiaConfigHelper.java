package github.poscard8.peritia.util.forge;


import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ModConfig;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class PeritiaConfigHelper
{
    protected static final Map<String, Boolean> CONFIG_MAP = new HashMap<>();

    public static Map<String, Boolean> getConfigMap() { return Collections.unmodifiableMap(CONFIG_MAP); }

    public static Boolean getConfig(String configName) { return CONFIG_MAP.getOrDefault(configName, true); }

    public static void clearMap() { CONFIG_MAP.clear(); }

    public static void validate(ModContainer modContainer)
    {
        String modId = modContainer.getModId();
        ModConfig modConfig = getModConfig(modContainer);
        if (modConfig != null && modConfig.getConfigData() != null) validate(modId, modConfig.getConfigData());
    }

    protected static String makeConfigName(String modId, String configName) { return modId + ":" + configName; }

    @SuppressWarnings("unchecked")
    @Nullable
    protected static ModConfig getModConfig(ModContainer modContainer)
    {
        try
        {
            Field field = ModContainer.class.getDeclaredField("configs");
            field.setAccessible(true);
            EnumMap<ModConfig.Type, ModConfig> configs = (EnumMap<ModConfig.Type, ModConfig>) field.get(modContainer);
            return configs.get(ModConfig.Type.SERVER);
        }
        catch (Exception e) { return null; }
    }

    protected static void validate(String modId, CommentedConfig config)
    {
        for (Map.Entry<String, Object> entry : config.valueMap().entrySet())
        {
            if (entry.getValue() instanceof CommentedConfig innerConfig)
            {
                validate(modId, innerConfig);
            }
            else if (entry.getValue() instanceof Boolean bool)
            {
                String configName = makeConfigName(modId, entry.getKey());
                CONFIG_MAP.put(configName, bool);
            }
        }
    }

}
