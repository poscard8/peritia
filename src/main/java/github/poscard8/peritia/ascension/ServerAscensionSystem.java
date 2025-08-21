package github.poscard8.peritia.ascension;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.clientbound.AscensionSystemPacket;
import github.poscard8.peritia.skill.SkillAttributes;
import github.poscard8.peritia.skill.SkillRewards;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import github.poscard8.peritia.util.skill.LevelXpFunction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class ServerAscensionSystem extends AscensionSystem implements JsonSerializable<ServerAscensionSystem>
{
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final String FILE_NAME = "peritia-ascensionSystem.json";

    MinecraftServer server;
    File file;

    ServerAscensionSystem(MinecraftServer server)
    {
        super();
        this.server = server;
        this.file = getOrCreateFile(server);
    }

    public static ServerAscensionSystem of(MinecraftServer server)
    {
        ServerAscensionSystem unloaded = new ServerAscensionSystem(server);
        JsonObject data = unloaded.fileAsJson();

        return unloaded.loadWithFallback(data, unloaded);
    }

    public static File getOrCreateFile(MinecraftServer server)
    {
        File directory = server.getWorldPath(Peritia.CONFIG_WORLD_DIRECTORY).toFile();
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
            String fileContent = Files.readString(file().toPath());
            return GSON.fromJson(fileContent, JsonObject.class);
        }
        catch (IOException exception) { throw new RuntimeException(exception); }
    }

    public void update(ServerPlayer player)
    {
        updateFile();
        updateClient(player);
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

    public void updateClient(ServerPlayer player) { PeritiaNetworkHandler.sendToClient(new AscensionSystemPacket(this), player); }

    @Override
    public ServerAscensionSystem fallback() { return null; }

    @Override
    public ServerAscensionSystem load(JsonObject data)
    {
        ParticleType<?> particleType2 = JsonHelper.readRegistrable(data, "particleType", ForgeRegistries.PARTICLE_TYPES, particleType);
        SimpleParticleType particleType3 = particleType2 instanceof SimpleParticleType simpleParticleType ? simpleParticleType : particleType;

        this.icon = JsonHelper.readRegistrable(data, "icon", ForgeRegistries.ITEMS, icon);
        this.sound = JsonHelper.readRegistrable(data, "sound", ForgeRegistries.SOUND_EVENTS, sound);
        this.particleType = particleType3;
        this.particleCount = JsonHelper.readInt(data, "particleCount", particleCount);
        this.xpFunction = JsonHelper.readJsonSerializable(data, "xpFunction", LevelXpFunction::tryLoad, xpFunction);
        this.attributes = JsonHelper.readArraySerializable(data, "attributes", SkillAttributes::tryLoad, attributes);
        this.rewards = JsonHelper.readArraySerializable(data, "rewards", SkillRewards::tryLoad, rewards);

        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();
        JsonHelper.write(data, "icon", icon, ForgeRegistries.ITEMS);
        JsonHelper.write(data, "sound", sound, ForgeRegistries.SOUND_EVENTS);
        JsonHelper.write(data, "particleType", particleType, ForgeRegistries.PARTICLE_TYPES);
        JsonHelper.write(data, "particleCount", particleCount);
        JsonHelper.write(data, "xpFunction", xpFunction);
        JsonHelper.write(data, "attributes", attributes);
        JsonHelper.write(data, "rewards", rewards);

        return data;
    }

}
