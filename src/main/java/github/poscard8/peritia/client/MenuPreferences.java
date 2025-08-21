package github.poscard8.peritia.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import github.poscard8.peritia.util.xpsource.XpSourceSortFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class MenuPreferences implements JsonSerializable<MenuPreferences>
{
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    static MenuPreferences INSTANCE;
    static String DIRECTORY_NAME = "config";
    static String FILE_NAME = "peritia-menuPreferences.json";

    File file;
    XpSource xpSourcePreferences = XpSource.empty();

    private MenuPreferences() {}

    public static MenuPreferences getInstance() { return INSTANCE == null ? load() : INSTANCE; }

    public static MenuPreferences load()
    {
        MenuPreferences preferences = new MenuPreferences();

        try
        {
            File gameDirectory = Minecraft.getInstance().gameDirectory;
            File configDirectory = new File(gameDirectory, DIRECTORY_NAME);
            File file = new File(configDirectory, FILE_NAME);

            try
            {
                boolean ignored = file.createNewFile();


                String fileContent = Files.readString(file.toPath());
                if (fileContent.isEmpty()) FileUtils.writeStringToFile(file, "{}", StandardCharsets.UTF_8);
            }
            catch (IOException e) { throw new RuntimeException(e); }

            preferences.file = file;

            JsonObject data = preferences.fileAsJson();
            preferences = preferences.loadWithFallback(data);
        }
        catch (Exception ignored) {}

        INSTANCE = preferences;
        return INSTANCE;
    }

    public File file() { return file; }

    public JsonObject fileAsJson()
    {
        try
        {
            String fileContent = Files.readString(file().toPath());
            return GSON.fromJson(fileContent, JsonObject.class);
        }
        catch (IOException exception) { throw new RuntimeException(exception); }
    }

    public XpSource xpSource() { return xpSourcePreferences; }

    public XpSourceSortFunction getSortFunction(Skill skill) { return xpSource().getSortFunction(skill); }

    public void setSortFunction(Skill skill, XpSourceSortFunction sortFunction)
    {
        xpSource().sortMap().put(skill.key(), sortFunction);
        update();
    }

    public void update()
    {
        try
        {
            String newFileContent = GSON.toJson(save());
            FileUtils.writeStringToFile(file(), newFileContent, StandardCharsets.UTF_8);
        }
        catch (IOException exception) { throw new RuntimeException(exception); }
    }

    @Override
    public MenuPreferences fallback() { return new MenuPreferences(); }

    @Override
    public MenuPreferences load(JsonObject data)
    {
        this.xpSourcePreferences = JsonHelper.readJsonSerializable(data, "xpSource", XpSource::tryLoad, xpSourcePreferences);
        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();
        JsonHelper.write(data, "xpSource", xpSourcePreferences);

        return data;
    }


    public static class XpSource implements JsonSerializable<XpSource>
    {
        protected final Map<ResourceLocation, XpSourceSortFunction> sortMap = new HashMap<>();

        public XpSource() {}

        public static XpSource empty() { return new XpSource(); }

        public static XpSource tryLoad(JsonObject data) { return empty().loadWithFallback(data); }

        public Map<ResourceLocation, XpSourceSortFunction> sortMap() { return sortMap; }

        public XpSourceSortFunction getSortFunction(ResourceLocation skillKey) { return sortMap().getOrDefault(skillKey, XpSourceSortFunction.empty()); }

        public XpSourceSortFunction getSortFunction(Skill skill) { return getSortFunction(skill.key()); }

        @Override
        public XpSource fallback() { return empty(); }

        @Override
        public XpSource load(JsonObject data)
        {
            for (Map.Entry<String, JsonElement> entry : data.entrySet())
            {
                ResourceLocation skillKey = ResourceLocation.tryParse(entry.getKey());
                if (skillKey == null) continue;

                JsonElement element = entry.getValue();
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())
                {
                    String string = element.getAsString();
                    XpSourceSortFunction sortFunction = XpSourceSortFunction.tryLoad(string);
                    sortMap.put(skillKey, sortFunction);
                }
            }
            return this;
        }

        @Override
        public JsonObject save()
        {
            JsonObject data = new JsonObject();
            for (Map.Entry<ResourceLocation, XpSourceSortFunction> entry : sortMap().entrySet())
            {
                JsonHelper.write(data, entry.getKey().toString(), entry.getValue());
            }
            return data;
        }
    }

}
