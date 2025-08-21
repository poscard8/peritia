package github.poscard8.peritia.xpsource.type;

import com.google.gson.JsonObject;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.SerializableDate;
import github.poscard8.peritia.xpsource.DataXpSource;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.XpSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;

public class SocialXpSource extends DataXpSource
{
    protected SerializableDate lastXp = new SerializableDate();

    protected int period = 0; // in seconds
    protected int minPlayerCount = 2;
    protected boolean cumulative = false;

    public SocialXpSource(ResourceLocation key) { super(key); }

    public static SocialXpSource empty() { return new SocialXpSource(EMPTY_KEY); }

    @Nullable
    public static SocialXpSource tryLoad(JsonObject data)
    {
        @Nullable XpSource xpSource = empty().loadWithFallback(data);
        return xpSource != null ? (SocialXpSource) xpSource : null;
    }

    @Override
    public XpSourceType<?> type() { return PeritiaXpSourceTypes.SOCIAL.get(); }

    public int period() { return period; }

    public int periodInMillis() { return period() * 1000; }

    public int minPlayerCount() { return minPlayerCount; }

    public boolean cumulative() { return cumulative; }

    public int multiplier(int playerCount) { return cumulative() ? playerCount - minPlayerCount() + 1 : 1; }

    public boolean shouldAward(int playerCount) { return playerCount >= minPlayerCount(); }

    @Override
    public boolean isInvalid() { return super.isInvalid() || period() <= 0 || minPlayerCount() <= 0; }

    @Nullable
    public SerializableDate getLastXpClient()
    {
        tryLoadClient();
        return lastXp;
    }

    @Nullable
    public SerializableDate getLastXp(MinecraftServer server)
    {
        tryLoad(server);
        return lastXp;
    }

    public void setLastXp(MinecraftServer server, SerializableDate date)
    {
        this.lastXp = date;
        update(server);
    }

    public void handleServerTick(MinecraftServer server)
    {
        int playerCount = server.getPlayerCount();
        if (shouldAward(playerCount))
        {
            @Nullable SerializableDate lastXp = getLastXp(server);
            if (lastXp == null)
            {
                lastXp = new SerializableDate();
                setLastXp(server, lastXp);
            }

            long offsetInMillis = -lastXp.offsetFromNow();
            if (offsetInMillis >= periodInMillis())
            {
                int multiplier = multiplier(playerCount);
                for (ServerPlayer player : server.getPlayerList().getPlayers()) award(player, multiplier);
                setLastXp(server, new SerializableDate());
            }
        }
    }

    @Nullable
    public SerializableDate getNextXpClient() { return getNextXp(getLastXpClient()); }

    @Nullable
    public SerializableDate getNextXp(@Nullable SerializableDate lastXp)
    {
        return lastXp == null ? null : lastXp.add(Calendar.SECOND, period());
    }

    @Override
    public void loadData(JsonObject data)
    {
        this.lastXp = JsonHelper.readStringSerializable(data, "lastXp", SerializableDate::tryLoad, lastXp);
    }

    @Override
    public JsonObject saveData()
    {
        JsonObject data = new JsonObject();
        JsonHelper.write(data, "lastXp", lastXp);
        return data;
    }

    @Override
    public void loadAdditional(JsonObject data)
    {
        this.period = JsonHelper.readInt(data, "period", period);
        this.minPlayerCount = JsonHelper.readInt(data, "minPlayerCount", minPlayerCount);
        this.cumulative = JsonHelper.readBoolean(data, "cumulative", cumulative);
    }

    @Override
    public void saveAdditional(JsonObject data)
    {
        JsonHelper.write(data, "period", period);
        JsonHelper.write(data, "minPlayerCount", minPlayerCount);
        JsonHelper.write(data, "cumulative", cumulative);
    }

}
