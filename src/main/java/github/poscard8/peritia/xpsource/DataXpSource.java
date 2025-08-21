package github.poscard8.peritia.xpsource;

import com.google.gson.JsonObject;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.xpsource.data.ClientXpSourceData;
import github.poscard8.peritia.xpsource.data.ServerXpSourceData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public abstract class DataXpSource extends XpSource
{
    protected boolean loaded = false;

    public DataXpSource(ResourceLocation key) { super(key); }

    @Nullable
    public static DataXpSource byName(String name)
    {
        @Nullable ResourceLocation parsed = ResourceLocation.tryParse(name);
        return parsed != null ? byKey(parsed) : null;
    }

    @Nullable
    public static DataXpSource byKey(ResourceLocation key)
    {
        for (DataXpSource xpSource : Peritia.xpSourceHandler().dataXpSources())
        {
            if (xpSource.key().equals(key)) return xpSource;
        }
        return null;
    }

    public abstract void loadData(JsonObject data);

    public abstract JsonObject saveData();

    public boolean isValidDataSource() { return true; }

    public boolean isLoaded() { return loaded; }

    public boolean isUnloaded() { return !isLoaded(); }

    public void tryLoadClient() { if (isUnloaded()) loadClient(); }

    public void loadClient()
    {
        ClientXpSourceData.getInstance().loadXpSource(this);
        loaded = true;
    }

    public void tryLoad(ServerPlayer player) { if (isUnloaded()) load(player); }

    public void load(ServerPlayer player)
    {
        MinecraftServer server = player.getServer();
        if (server != null) load(server);
    }

    public void tryLoad(MinecraftServer server) { if (isUnloaded()) load(server); }

    public void load(MinecraftServer server)
    {
        ServerXpSourceData.of(server).loadXpSource(this);
        loaded = true;
    }

    public void update(ServerPlayer player)
    {
        MinecraftServer server = player.getServer();
        if (server != null) ServerXpSourceData.of(server).updateXpSource(this, player);
    }

    public void update(MinecraftServer server) { ServerXpSourceData.of(server).updateXpSource(this); }

}
