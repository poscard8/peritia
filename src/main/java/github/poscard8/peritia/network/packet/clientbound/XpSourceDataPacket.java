package github.poscard8.peritia.network.packet.clientbound;

import com.google.gson.JsonObject;
import github.poscard8.peritia.network.Packet;
import github.poscard8.peritia.xpsource.data.ServerXpSourceData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class XpSourceDataPacket implements Packet
{
    public JsonObject data;

    public XpSourceDataPacket(ServerXpSourceData xpSourceData) { this(xpSourceData.save()); }

    public XpSourceDataPacket(JsonObject data) { this.data = data; }

    public XpSourceDataPacket(FriendlyByteBuf buffer) { this.data = readJsonObject(buffer); }

    @Override
    public void encode(FriendlyByteBuf buffer) { writeJsonObject(buffer, data); }

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        clientHandler().handleXpSourceDataPacket(this);
        return Result.SUCCESS;
    }

}
