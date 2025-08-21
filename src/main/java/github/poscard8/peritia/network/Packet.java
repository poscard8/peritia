package github.poscard8.peritia.network;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import github.poscard8.peritia.Peritia;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public interface Packet
{
    Gson GSON = new Gson();

    void encode(FriendlyByteBuf buffer);

    Result consume(NetworkEvent.Context context);


    default ClientHandler clientHandler() { return ClientHandler.getInstance(); }

    default void consumeWrapper(Supplier<NetworkEvent.Context> contextGetter)
    {
        NetworkEvent.Context context = contextGetter.get();
        Result result;

        try
        {
            result = consume(context);
        }
        catch (Exception exception) { result = Result.FAIL; }

        switch (result)
        {
            case PASS -> Peritia.LOGGER.warn("{} returned the value PASS", getClass().getSimpleName());
            case FAIL -> Peritia.LOGGER.error("{} returned the value FAIL", getClass().getSimpleName());
        }
        context.setPacketHandled(true);
    }

    default void writeLongString(FriendlyByteBuf buffer, String string)
    {
        int length = string.length();
        List<String> chunks = new ArrayList<>();

        for (int i = 0; i < length; i += 32767)
        {
            int end = Math.min(length, i + 32767);
            chunks.add(string.substring(i, end));
        }
        int chunkCount = chunks.size();

        buffer.writeInt(chunkCount);
        for (String chunk : chunks) buffer.writeUtf(chunk);
    }

    default void writeJsonObject(FriendlyByteBuf buffer, JsonObject data) { writeLongString(buffer, data.toString()); }

    default void writeJsonArray(FriendlyByteBuf buffer, JsonArray data) { writeLongString(buffer, data.toString()); }

    default JsonObject readJsonObject(FriendlyByteBuf buffer)
    {
        int chunkCount = buffer.readInt();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < chunkCount; i++)
        {
            String chunk = buffer.readUtf();
            builder.append(chunk);
        }
        String string = builder.toString();
        return GSON.fromJson(string, JsonObject.class);
    }

    default JsonArray readJsonArray(FriendlyByteBuf buffer)
    {
        int chunkCount = buffer.readInt();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < chunkCount; i++)
        {
            String chunk = buffer.readUtf();
            builder.append(chunk);
        }
        String string = builder.toString();
        return GSON.fromJson(string, JsonArray.class);
    }


    enum Result
    {
        SUCCESS,
        PASS,
        FAIL
    }

}
