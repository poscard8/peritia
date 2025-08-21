package github.poscard8.peritia.util.minecraft;

import com.google.gson.JsonObject;
import github.poscard8.peritia.skill.data.ClientSkillData;
import github.poscard8.peritia.skill.data.ServerSkillData;
import github.poscard8.peritia.skill.data.SkillData;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class LookContext implements JsonSerializable<LookContext>
{
    protected String uuid;
    protected String name;
    protected SkillData skillData;
    protected SimpleAttributeMap attributeMap;

    public LookContext()
    {
        this.uuid = "";
        this.name = "Player";
        this.skillData = ClientSkillData.empty();
        this.attributeMap = SimpleAttributeMap.empty();
    }

    public LookContext(ServerPlayer player)
    {
        this.uuid = player.getStringUUID();
        this.name = player.getName().getString();
        this.skillData = ServerSkillData.of(player);
        this.attributeMap = new SimpleAttributeMap(player);
    }

    @Nullable
    public static LookContext tryLoad(JsonObject data) { return new LookContext().loadWithFallback(data); }

    public String uuid() { return uuid; }

    public UUID rawUUID() { return UUID.fromString(uuid()); }

    public String playerName() { return name; }

    public ServerSkillData skillDataFromServer() { return (ServerSkillData) skillData; }

    public ClientSkillData skillDataFromClient() { return (ClientSkillData) skillData; }

    public SimpleAttributeMap attributeMap() { return attributeMap; }

    @Override
    public LookContext fallback() { return null; }

    @Override
    public LookContext load(JsonObject data)
    {
        this.uuid = JsonHelper.readString(data, "uuid", uuid);
        this.name = JsonHelper.readString(data, "name", name);
        this.skillData = JsonHelper.readJsonSerializable(data, "skillData", ClientSkillData::tryLoad, skillData);
        this.attributeMap = JsonHelper.readJsonSerializable(data, "attributeMap", SimpleAttributeMap::tryLoad, attributeMap);

        if (uuid.isEmpty()) throw new RuntimeException("Look context has no player UUID");
        return this;
    }

    @Override
    @Nullable
    public JsonObject save()
    {
        try
        {
            JsonObject data = new JsonObject();
            JsonHelper.write(data, "uuid", uuid);
            JsonHelper.write(data, "name", name);
            JsonHelper.write(data, "skillData", skillDataFromServer().saveForClient());
            JsonHelper.write(data, "attributeMap", attributeMap);

            return data;
        }
        catch (Exception e) { return null; }
    }

}
