package github.poscard8.peritia.skill.data;

import com.google.gson.JsonObject;
import github.poscard8.peritia.ascension.Legacy;
import github.poscard8.peritia.network.ClientHandler;
import github.poscard8.peritia.skill.HighScoreMap;
import github.poscard8.peritia.skill.SkillMap;
import github.poscard8.peritia.util.minecraft.ResourceSet;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.SerializableDate;

public final class ClientSkillData extends SkillData
{
    ClientSkillData(JsonObject data) { load(data); }

    public static ClientSkillData getInstance() { return ClientHandler.getSkillData(); }

    public static ClientSkillData empty() { return new ClientSkillData(new JsonObject()); }

    public static ClientSkillData tryLoad(JsonObject data) { return empty().load(data); }

    public ClientSkillData load(JsonObject data)
    {
        this.skillMap = JsonHelper.readJsonSerializable(data, "skillMap", SkillMap::tryLoad, skillMap);
        this.highScoreMap = JsonHelper.readJsonSerializable(data, "highScoreMap", HighScoreMap::tryLoad, highScoreMap);
        this.legacy = JsonHelper.readJsonSerializable(data, "legacy", Legacy::tryLoad, legacy);
        this.discoveredXpSources = JsonHelper.readArraySerializable(data, "discoveredXpSources", ResourceSet::tryLoad, discoveredXpSources);
        this.additional = JsonHelper.readObject(data, "additional", additional);
        this.timePlayed = JsonHelper.readInt(data, "timePlayed", timePlayed);
        this.lastUpdate = new SerializableDate();
        this.lastAscended = JsonHelper.readStringSerializable(data, "lastAscended", SerializableDate::tryLoad, lastAscended);

        legacy().setAscensionSystem(ClientHandler.getAscensionSystem());
        return this;
    }

}
