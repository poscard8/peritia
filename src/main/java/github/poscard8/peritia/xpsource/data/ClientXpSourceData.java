package github.poscard8.peritia.xpsource.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.poscard8.peritia.network.ClientHandler;
import github.poscard8.peritia.xpsource.DataXpSource;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class ClientXpSourceData extends XpSourceData
{
    ClientXpSourceData(JsonObject data)
    {
        super(new HashMap<>());
        load(data);
    }

    public static ClientXpSourceData getInstance() { return ClientHandler.getXpSourceData(); }

    public static ClientXpSourceData empty() { return new ClientXpSourceData(new JsonObject()); }

    public static ClientXpSourceData tryLoad(JsonObject data) { return empty().load(data); }

    public ClientXpSourceData load(JsonObject data)
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

}
