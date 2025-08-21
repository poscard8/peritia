package github.poscard8.peritia.skill.data;

import com.google.common.collect.ImmutableMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.advancement.PeritiaAdvancementTriggers;
import github.poscard8.peritia.ascension.Legacy;
import github.poscard8.peritia.ascension.ServerAscensionSystem;
import github.poscard8.peritia.network.Packet;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.clientbound.*;
import github.poscard8.peritia.registry.PeritiaAttributes;
import github.poscard8.peritia.skill.HighScoreMap;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.SkillInstance;
import github.poscard8.peritia.skill.SkillMap;
import github.poscard8.peritia.util.minecraft.ResourceSet;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import github.poscard8.peritia.util.serialization.SerializableDate;
import github.poscard8.peritia.util.skill.SkillFunction;
import github.poscard8.peritia.util.skill.XpGainContext;
import github.poscard8.peritia.util.xpsource.XpSourceFunction;
import github.poscard8.peritia.xpsource.XpSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public final class ServerSkillData extends SkillData implements JsonSerializable<ServerSkillData>
{
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final String INNER_DIRECTORY_NAME = "skill_data";
    public static final String JSON_SUFFIX = ".json";

    MinecraftServer server;
    File file;
    ServerPlayer player;

    /**
     * Skill map, high score map, and ascensions are loaded afterward.
     */
    ServerSkillData(ServerPlayer player)
    {
        MinecraftServer server = player.getServer();
        if (server == null) throw new RuntimeException("Tried to generate server skill data without a server");

        this.server = server;
        this.file = getOrCreateFile(player);
        this.player = player;
    }

    public static ServerSkillData of(ServerPlayer player)
    {
        ServerSkillData unloaded = new ServerSkillData(player);
        JsonObject data = unloaded.fileAsJson();

        return unloaded.loadWithFallback(data, unloaded);
    }

    public static File getOrCreateFile(ServerPlayer player)
    {
        MinecraftServer server = player.getServer();
        if (server == null) throw new RuntimeException(String.format("No server found to write skill data for player %s", player.getName().getString()));

        File directory = server.getWorldPath(Peritia.WORLD_DIRECTORY).toFile();
        File innerDirectory = new File(directory, INNER_DIRECTORY_NAME);
        File file = new File(innerDirectory, player.getStringUUID() + JSON_SUFFIX);

        if (!directory.exists())
        {
            try
            {
                Files.createDirectory(directory.toPath());
            }
            catch (IOException e) { throw new RuntimeException(e); }
        }

        if (!innerDirectory.exists())
        {
            try
            {
                Files.createDirectory(innerDirectory.toPath());
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

    public ServerPlayer player() { return player; }

    public void sendToClient(Packet packet) { PeritiaNetworkHandler.sendToClient(packet, player()); }

    public double wisdomMultiplier() { return (player().getAttributeValue(PeritiaAttributes.WISDOM.get()) / 100.0D) + 1; }

    public boolean hasCompletedEncyclopediaFor(Skill skill) { return super.hasCompletedEncyclopediaFor(player(), skill); }

    public void reset()
    {
        skillMap = SkillMap.empty();
        highScoreMap = HighScoreMap.empty();
        legacy.reset();
        discoveredXpSources.remove(XpSourceFunction.All.INSTANCE);

        update();
    }

    public void maxOut()
    {
        skillMap = SkillMap.max();
        highScoreMap = HighScoreMap.max();
        legacy.maxOut();
        discoveredXpSources.add(XpSourceFunction.All.INSTANCE);

        update();
    }

    public void claimRewards(Skill skill, int oldLevel, int newLevel)
    {
        getSkill(skill).tryClaimRewards(this, oldLevel, newLevel);
        update();
    }

    public void claimAllRewards()
    {
        for (SkillInstance instance : skillMap().values()) instance.tryClaimRewards(this, instance.minLevel() - 1, instance.level());
        update();
    }

    public void payRestrictions(Skill skill, int level)
    {
        getSkill(skill).tryPayRestrictions(this, level);
        update();
        sendToClient(new RefreshScreenPacket());
    }

    public void addXpToSkills(SkillFunction skillFunction, @Nullable XpSource xpSource, int xp) { addXpToSkills(skillFunction, xpSource, xp, true, false); }

    public void addXpToSkills(SkillFunction skillFunction, @Nullable XpSource xpSource, int xp, boolean manually, boolean ignoreRestrictions)
    {
        long uncapped = Math.round(xpMultiplier() * wisdomMultiplier() * xp);
        int modifiedXp = uncapped > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) uncapped;
        int finalXp = manually ? modifiedXp : xp;

        XpGainContext context = new XpGainContext(finalXp);
        boolean levelUp;
        boolean playSound = true;
        Map<Skill, Boolean> encyclopediaMap = new HashMap<>();

        for (Skill skill : skillFunction.getSkills(this))
        {
            SkillInstance instance = getSkill(skill);
            levelUp = instance.addXp(this, finalXp, manually, playSound, ignoreRestrictions);
            context.add(instance);

            if (levelUp) playSound = false;
        }

        if (xpSource != null && manually)
        {
            for (Skill skill : Peritia.skills()) encyclopediaMap.put(skill, hasCompletedEncyclopediaFor(skill));

            boolean hideInitially = xpSource.shouldHide(player(), this);
            discoveredXpSources().add(xpSource.key());

            boolean showNow = xpSource.shouldShow(player(), this);
            if (hideInitially && showNow)
            {
                sendToClient(new EncyclopediaUpdatePacket());
            }

            for (Skill skill : encyclopediaMap.keySet())
            {
                if (encyclopediaMap.get(skill)) continue; // return if already completed
                if (hasCompletedEncyclopediaFor(skill)) sendToClient(new EncyclopediaCompletePacket(skill));
            }
        }

        update();
        if (manually) sendToClient(new XpGainPacket(context, playSound));
    }

    public void addLevelsToSkills(SkillFunction skillFunction, int levels)
    {
        for (Skill skill : skillFunction.getSkills(this)) getSkill(skill).addLevel(this, levels);
        update();
    }

    public void setXpOfSkills(SkillFunction skillFunction, int xp, boolean ignoreRestrictions)
    {
        boolean levelUp;
        boolean playSound = true;

        for (Skill skill : skillFunction.getSkills(this))
        {
            levelUp = getSkill(skill).setXp(this, xp, playSound, ignoreRestrictions);
            if (levelUp) playSound = false;
        }
        update();
    }

    public void setLevelsOfSkills(SkillFunction skillFunction, int level)
    {
        for (Skill skill : skillFunction.getSkills(this)) getSkill(skill).setLevel(this, level);
        update();
    }

    public void resetSkills() { resetSkills(SkillFunction.All.INSTANCE); }

    public void resetSkills(SkillFunction skillFunction)
    {
        for (Skill skill : skillFunction.getSkills(this)) getSkill(skill).reset(this);
        update();
    }

    public void ascend(boolean addCooldown)
    {
        if (addCooldown) lastAscended = new SerializableDate();

        legacy().handleAscension(player());
        resetSkills();
    }

    public void coverXpSources(XpSourceFunction function)
    {
        discoveredXpSources().remove(function);
        update();
    }

    public void discoverXpSources(XpSourceFunction function)
    {
        discoveredXpSources().add(function);
        update();
    }

    public void putSkill(SkillInstance instance)
    {
        skillMap().putSkill(instance);
        highScoreMap().tryPutHighScore(instance);
    }

    public void putLegacy(Legacy legacy)
    {
        this.lastAscended = new SerializableDate();
        this.legacy = legacy;
        update();
    }

    public void writeAdditionalDataNoUpdate(Consumer<JsonObject> consumer) { consumer.accept(additionalData()); }

    public void writeAdditionalData(Consumer<JsonObject> consumer)
    {
        writeAdditionalDataNoUpdate(consumer);
        update();
    }

    public void update()
    {
        updateFile();
        updateAdvancements();
        updateAttributes();
        updateClient();
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

    public void updateAdvancements() { PeritiaAdvancementTriggers.trigger(player(), this); }

    public void updateClient()
    {
        sendToClient(new SkillDataPacket(this));
        sendToClient(new AttributePacket(player()));
    }

    public void updateAttributes()
    {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = new ImmutableMultimap.Builder<>();

        for (SkillInstance instance : skillMap().values()) builder.putAll(instance.attributeModifierMap());
        builder.putAll(legacy().attributeModifierMap());
        ImmutableMultimap<Attribute, AttributeModifier> multimap = builder.build();

        try
        {
            player().getAttributes().removeAttributeModifiers(multimap);
            player().getAttributes().addTransientAttributeModifiers(multimap);
        }
        catch (Exception exception)
        {
            Peritia.LOGGER.error("Error adding attribute modifiers to player {}", player().getName().getString());
        }
    }

    /**
     * Should NOT be used.
     */
    @Override
    public ServerSkillData fallback() { return null; }

    @Override
    public ServerSkillData load(JsonObject data)
    {
        this.skillMap = JsonHelper.readJsonSerializable(data, "skillMap", SkillMap::tryLoad, skillMap);
        this.highScoreMap = JsonHelper.readJsonSerializable(data, "highScoreMap", HighScoreMap::tryLoad, highScoreMap);
        this.legacy = JsonHelper.readJsonSerializable(data, "legacy", Legacy::tryLoad, legacy);
        this.discoveredXpSources = JsonHelper.readArraySerializable(data, "discoveredXpSources", ResourceSet::tryLoad, discoveredXpSources);
        this.additional = JsonHelper.readObject(data, "additional", additional);
        this.lastAscended = JsonHelper.readStringSerializable(data, "lastAscended", SerializableDate::tryLoad, lastAscended);

        legacy().setAscensionSystem(ServerAscensionSystem.of(server()));
        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();
        JsonHelper.write(data, "skillMap", skillMap);
        JsonHelper.write(data, "highScoreMap", highScoreMap);
        JsonHelper.write(data, "legacy", legacy);
        JsonHelper.write(data, "discoveredXpSources", discoveredXpSources);
        JsonHelper.write(data, "additional", additional);
        JsonHelper.write(data, "lastAscended", lastAscended);

        return data;
    }

    @Override
    public JsonObject saveForClient()
    {
        timePlayed = player().getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) / 20;

        JsonObject data = new JsonObject();
        JsonHelper.write(data, "skillMap", skillMap);
        JsonHelper.write(data, "highScoreMap", highScoreMap);
        JsonHelper.write(data, "legacy", legacy);
        JsonHelper.write(data, "discoveredXpSources", discoveredXpSources);
        JsonHelper.write(data, "additional", additional);
        JsonHelper.write(data, "timePlayed", timePlayed);
        JsonHelper.write(data, "lastAscended", lastAscended);

        return data;
    }

}
