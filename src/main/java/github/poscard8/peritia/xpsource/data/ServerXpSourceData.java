package github.poscard8.peritia.xpsource.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.clientbound.XpSourceDataPacket;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import github.poscard8.peritia.util.xpsource.DataXpSourceFunction;
import github.poscard8.peritia.xpsource.DataXpSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class ServerXpSourceData extends XpSourceData implements JsonSerializable<ServerXpSourceData>
{
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final String FILE_NAME = "xp_source_data.json";

    MinecraftServer server;
    File file;

    ServerXpSourceData(MinecraftServer server)
    {
        super(new HashMap<>());
        this.server = server;
        this.file = getOrCreateFile(server);
    }

    public static ServerXpSourceData of(MinecraftServer server)
    {
        ServerXpSourceData unloaded = new ServerXpSourceData(server);
        JsonObject data = unloaded.fileAsJson();

        return unloaded.loadWithFallback(data, unloaded);
    }

    public static File getOrCreateFile(MinecraftServer server)
    {

        File directory = server.getWorldPath(Peritia.WORLD_DIRECTORY).toFile();
        File file = new File(directory, FILE_NAME);

        if (!directory.exists())
        {
            try
            {
                Files.createDirectory(directory.toPath());
            }
            catch (IOException e) { throw new RuntimeException(e); }
        }

        try
        {
            boolean ignored = file.createNewFile();
            String fileContent = Files.readString(file.toPath());
            if (fileContent.isEmpty()) FileUtils.writeStringToFile(file, "{}", StandardCharsets.UTF_8);
        }
        catch (IOException e) { throw new RuntimeException(e); }

        return file;
    }

    public MinecraftServer server() { return server; }

    public File file() { return file; }

    public JsonObject fileAsJson()
    {
        try
        {
            String fileContent = Files.readString(file.toPath());
            return GSON.fromJson(fileContent, JsonObject.class);
        }
        catch (IOException exception) { throw new RuntimeException(exception); }
    }

    public void updateXpSource(DataXpSource xpSource)
    {
        dataMap().put(xpSource, xpSource.saveData());
        update();
    }

    public void updateXpSource(DataXpSource xpSource, ServerPlayer player)
    {
        dataMap().put(xpSource, xpSource.saveData());
        update(Set.of(player));
    }

    public void reset(Collection<ServerPlayer> players, DataXpSourceFunction xpSourceFunction)
    {
        for (DataXpSource xpSource : xpSourceFunction)
        {
            JsonObject data = dataMap().getOrDefault(xpSource, new JsonObject());

            for (ServerPlayer player : players)
            {
                if (data.has(player.getStringUUID())) data.remove(player.getStringUUID());
            }
            xpSource.loadData(data);
        }
        update();
    }

    public void update()
    {
        if (server != null) update(server.getPlayerList().getPlayers());
    }

    public void update(Collection<ServerPlayer> players)
    {
        updateFile();
        updateClient(players);
    }

    public void updateFile()
    {
        try
        {
            String newFileContent = GSON.toJson(save());
            FileUtils.writeStringToFile(file(), newFileContent, StandardCharsets.UTF_8);
        }
        catch (IOException exception) { throw new RuntimeException(exception); }
    }

    public void updateClient(Collection<ServerPlayer> players)
    {
        for (ServerPlayer player : players) PeritiaNetworkHandler.sendToClient(new XpSourceDataPacket(this), player);
    }

    /**
     * Should NOT be used.
     */
    @Override
    public ServerXpSourceData fallback() { return null; }

    @Override
    public ServerXpSourceData load(JsonObject data)
    {
        for (Map.Entry<String, JsonElement> entry : data.entrySet())
        {
            String name = entry.getKey();
            JsonElement element = entry.getValue();

            if (element.isJsonObject())
            {
                @Nullable DataXpSource xpSource = DataXpSource.byName(name);
                JsonObject jsonObject = element.getAsJsonObject();

                if (xpSource != null) dataMap.put(xpSource, jsonObject);
            }
        }
        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();
        for (Map.Entry<DataXpSource, JsonObject> entry : dataMap.entrySet()) data.add(entry.getKey().stringKey(), entry.getValue());

        return data;
    }

}
