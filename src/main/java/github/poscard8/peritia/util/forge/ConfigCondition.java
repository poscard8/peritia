package github.poscard8.peritia.util.forge;

import com.google.gson.JsonObject;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.util.serialization.JsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class ConfigCondition implements ICondition
{
    public static final ResourceLocation ID = Peritia.asResource("config");

    public final String configName;
    public final boolean invert;

    public ConfigCondition(String configName, boolean invert)
    {
        this.configName = configName;
        this.invert = invert;
    }

    @Override
    public ResourceLocation getID() { return ID; }

    @Override
    public boolean test(IContext context) { return invert != PeritiaConfigHelper.getConfig(configName); }


    public static class Serializer implements IConditionSerializer<ConfigCondition>
    {
        public static final Serializer INSTANCE = new Serializer();

        protected Serializer() {}

        @Override
        public ConfigCondition read(JsonObject data)
        {
            String configName = JsonHelper.readString(data, "config");
            boolean invert = JsonHelper.readBoolean(data, "invert", false);
            return new ConfigCondition(configName, invert);
        }

        @Override
        public void write(JsonObject data, ConfigCondition condition)
        {
            JsonHelper.write(data, "config", condition.configName);
            JsonHelper.write(data, "invert", condition.invert);
        }

        @Override
        public ResourceLocation getID() { return ID; }

    }

}
