package github.poscard8.peritia.network.packet.clientbound;

import com.google.gson.JsonObject;
import github.poscard8.peritia.network.Packet;
import github.poscard8.peritia.util.minecraft.LookContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

public class LookContextPacket implements Packet
{
    public boolean isNull;
    public JsonObject data;

    public LookContextPacket() { this(true, new JsonObject()); }

    public LookContextPacket(@NotNull ServerPlayer player) { this(new LookContext(player)); }

    public LookContextPacket(@NotNull LookContext lookContext) { this(false, lookContext.save()); }

    public LookContextPacket(boolean isNull, JsonObject data)
    {
        this.isNull = isNull;
        this.data = data;
    }

    public LookContextPacket(FriendlyByteBuf buffer)
    {
        this.isNull = buffer.readBoolean();
        this.data = readJsonObject(buffer);
    }

    @Override
    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(isNull);
        writeJsonObject(buffer, data);
    }

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        clientHandler().handleLookContextPacket(this);
        return Result.SUCCESS;
    }

}
