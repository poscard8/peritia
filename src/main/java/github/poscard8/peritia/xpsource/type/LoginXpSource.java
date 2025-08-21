package github.poscard8.peritia.xpsource.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.poscard8.peritia.network.ClientHandler;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.SerializableDate;
import github.poscard8.peritia.util.serialization.StringSerializable;
import github.poscard8.peritia.xpsource.DataXpSource;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.XpSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;

public class LoginXpSource extends DataXpSource
{
    protected final Map<String, SerializableDate> lastLoginMap = new HashMap<>();

    protected Refresh refresh = Refresh.empty();

    public LoginXpSource(ResourceLocation key) { super(key); }

    public static LoginXpSource empty() { return new LoginXpSource(EMPTY_KEY); }

    @Nullable
    public static LoginXpSource tryLoad(JsonObject data)
    {
        @Nullable XpSource xpSource = empty().loadWithFallback(data);
        return xpSource != null ? (LoginXpSource) xpSource : null;
    }

    @Override
    public XpSourceType<?> type() { return PeritiaXpSourceTypes.LOGIN.get(); }

    public Refresh refreshFunction() { return refresh; }

    public boolean shouldRefresh(@NotNull SerializableDate current, @Nullable SerializableDate old) { return old == null || refreshFunction().shouldRefresh(current, old); }

    @Nullable
    public SerializableDate getLastLoginClient()
    {
        tryLoadClient();
        return lastLoginMap.getOrDefault(ClientHandler.getPlayerUUID(), null);
    }

    @Nullable
    public SerializableDate getLastLogin(ServerPlayer player)
    {
        tryLoad(player);
        return lastLoginMap.getOrDefault(player.getStringUUID(), null);
    }

    public void setLastLogin(ServerPlayer player, SerializableDate date)
    {
        lastLoginMap.put(player.getStringUUID(), date);
        update(player);
    }

    public void handlePlayerLogin(ServerPlayer player)
    {
        SerializableDate current = new SerializableDate();
        @Nullable SerializableDate old = getLastLogin(player);

        if (shouldRefresh(current, old))
        {
            award(player);
            setLastLogin(player, current);
        }
    }

    @Nullable
    public SerializableDate getNextValidLoginClient() { return getNextValidLogin(getLastLoginClient()); }

    @Nullable
    public SerializableDate getNextValidLogin(@Nullable SerializableDate lastLogin)
    {
        return lastLogin == null ? null : refreshFunction().getNextValidLogin(lastLogin);
    }

    @Override
    public void loadData(JsonObject data)
    {
        lastLoginMap.clear();

        for (Map.Entry<String, JsonElement> entry : data.entrySet())
        {
            String string = entry.getKey();
            SerializableDate date = SerializableDate.tryLoad(entry.getValue().getAsString());

            lastLoginMap.put(string, date);
        }
    }

    @Override
    public JsonObject saveData()
    {
        JsonObject data = new JsonObject();

        for (String key : lastLoginMap.keySet())
        {
            SerializableDate value = lastLoginMap.get(key);
            if (value != null) JsonHelper.write(data, key, value);
        }
        return data;
    }

    @Override
    public void loadAdditional(JsonObject data)
    {
        this.refresh = JsonHelper.readStringSerializable(data, "refresh", Refresh::tryLoad, refresh);
    }

    @Override
    public void saveAdditional(JsonObject data)
    {
        JsonHelper.write(data, "refresh", refresh);
    }


    public enum Refresh implements StringSerializable<Refresh>
    {
        MONTHLY("monthly", (current, old) -> !current.isSameMonth(old), SerializableDate::nextMonth),
        WEEKLY("weekly", (current, old) -> !current.isSameWeek(old), SerializableDate::nextWeek),
        DAILY("daily", (current, old) -> !current.isSameDay(old), SerializableDate::nextDay),
        HOURLY("hourly", (current, old) -> !current.isSameHour(old), SerializableDate::nextHour),
        INSTANTLY("instantly", (current, old) -> true, UnaryOperator.identity());

        private final String name;
        private final BiPredicate<SerializableDate, SerializableDate> newDatePredicate;
        private final UnaryOperator<SerializableDate> nextLoginFunction;

        Refresh(String name, BiPredicate<SerializableDate, SerializableDate> newDatePredicate, UnaryOperator<SerializableDate> nextLoginFunction)
        {
            this.name = name;
            this.newDatePredicate = newDatePredicate;
            this.nextLoginFunction = nextLoginFunction;
        }

        public static Refresh empty() { return Refresh.DAILY; }

        public static Refresh tryLoad(String data) { return empty().loadWithFallback(data); }

        public String getName() { return name; }

        public boolean shouldRefresh(SerializableDate current, SerializableDate old) { return newDatePredicate.test(current, old); }

        public SerializableDate getNextValidLogin(SerializableDate lastLogin) { return nextLoginFunction.apply(lastLogin); }

        @Override
        public Refresh fallback() { return empty(); }

        @Override
        public Refresh load(String data)
        {
            return Arrays.stream(values()).filter(levelLayout -> levelLayout.getName().equals(data)).findFirst().orElse(fallback());
        }

        @Override
        public String save() { return getName(); }

    }

}
