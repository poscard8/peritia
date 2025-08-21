package github.poscard8.peritia.util.minecraft;

import com.google.gson.JsonObject;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import github.poscard8.peritia.util.serialization.SerializableDate;
import net.minecraft.server.MinecraftServer;

public class GameContext implements JsonSerializable<GameContext>
{
    protected StructureContext structureContext;
    protected AdvancementContext advancementContext;
    protected LootTableContext lootTableContext;
    protected SerializableDate lastLogin;

    public GameContext()
    {
        this.structureContext = StructureContext.empty();
        this.advancementContext = AdvancementContext.empty();
        this.lootTableContext = LootTableContext.empty();
        this.lastLogin = new SerializableDate();
    }

    public GameContext(MinecraftServer server)
    {
        this.structureContext = new StructureContext(server);
        this.advancementContext = new AdvancementContext(server);
        this.lootTableContext = new LootTableContext(server);
        this.lastLogin = new SerializableDate();
    }

    public static GameContext empty() { return new GameContext(); }

    public static GameContext tryLoad(JsonObject data) { return empty().loadWithFallback(data); }

    public StructureContext structureContext() { return structureContext; }

    public AdvancementContext advancementContext() { return advancementContext; }

    public LootTableContext lootTableContext() { return lootTableContext; }

    public SerializableDate lastLogin() { return lastLogin; }

    @Override
    public GameContext fallback() { return empty(); }

    @Override
    public GameContext load(JsonObject data)
    {
        this.structureContext = JsonHelper.readJsonSerializable(data, "structureContext", StructureContext::tryLoad, structureContext);
        this.advancementContext = JsonHelper.readJsonSerializable(data, "advancementContext", AdvancementContext::tryLoad, advancementContext);
        this.lootTableContext = JsonHelper.readArraySerializable(data, "lootTableContext", LootTableContext::tryLoad, lootTableContext);
        this.lastLogin = JsonHelper.readStringSerializable(data, "lastLogin", SerializableDate::tryLoad, lastLogin);

        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();
        JsonHelper.write(data, "structureContext", structureContext);
        JsonHelper.write(data, "advancementContext", advancementContext);
        JsonHelper.write(data, "lootTableContext", lootTableContext);
        JsonHelper.write(data, "lastLogin", lastLogin);

        return data;
    }

}
