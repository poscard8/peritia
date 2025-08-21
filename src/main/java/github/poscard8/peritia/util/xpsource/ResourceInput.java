package github.poscard8.peritia.util.xpsource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.poscard8.peritia.util.serialization.ElementSerializable;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.Loadable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public abstract class ResourceInput implements Input<ResourceLocation>, ElementSerializable<ResourceInput>
{
    public static ResourceInput empty() { return Single.empty(); }

    public static ResourceInput tryLoad(JsonElement data)
    {
        if (data.isJsonObject()) return Contains.tryLoad(data);
        if (data.isJsonPrimitive() && data.getAsJsonPrimitive().isString()) return Single.tryLoad(data);

        return empty();
    }

    @Override
    public boolean isValid() { return true; }

    @Override
    public ResourceInput fallback() { return empty(); }


    public static class Single extends ResourceInput
    {
        protected ResourceLocation key;

        public Single(ResourceLocation key) { this.key = key; }

        public static Single empty() { return new Single(Loadable.EMPTY_KEY); }

        public static Single tryLoad(JsonElement data)
        {
            return empty().loadWithFallback(data) instanceof Single single ? single : empty();
        }

        public ResourceLocation key() { return key; }

        @Override
        public boolean isValid() { return !key().equals(Loadable.EMPTY_KEY); }

        @Override
        public boolean test(ResourceLocation key) { return key().equals(key); }

        @Override
        public ResourceInput load(JsonElement data)
        {
            String string = data.getAsString();
            ResourceLocation key = ResourceLocation.tryParse(string);
            if (key == null) throw new RuntimeException(String.format("Invalid key: %s", string));

            this.key = key;
            return this;
        }

        @Override
        public JsonElement save() { return new JsonPrimitive(key.toString()); }

    }

    public static class Contains extends ResourceInput
    {
        protected @Nullable String modId;
        protected @Nullable String string;

        public Contains(@Nullable String modId, @Nullable String string)
        {
            this.modId = modId;
            this.string = string;
        }

        public static Contains empty() { return new Contains(null, null); }

        public static Contains tryLoad(JsonElement data)
        {
            return empty().loadWithFallback(data) instanceof Contains contains ? contains : empty();
        }

        @Nullable
        public String modId() { return modId; }

        public boolean testModId(ResourceLocation key) { return modId == null || key.getNamespace().equals(modId); }

        @Nullable
        public String string() { return string; }

        public boolean testString(ResourceLocation key) { return string == null || key.getPath().contains(string); }

        @Override
        public boolean test(ResourceLocation key) { return testModId(key) && testString(key); }

        @Override
        public ResourceInput load(JsonElement data)
        {
            JsonObject jsonObject = data.getAsJsonObject();

            this.modId = JsonHelper.readString(jsonObject, "modId", null);
            this.string = JsonHelper.readString(jsonObject, "contains", null);
            return this;
        }

        @Override
        public JsonObject save()
        {
            JsonObject data = new JsonObject();
            if (modId != null) JsonHelper.write(data, "modId", modId);
            if (string != null) JsonHelper.write(data, "contains", string);

            return data;
        }
    }

}
