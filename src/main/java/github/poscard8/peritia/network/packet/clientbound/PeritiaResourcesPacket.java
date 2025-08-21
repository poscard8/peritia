package github.poscard8.peritia.network.packet.clientbound;

import com.google.gson.JsonObject;
import github.poscard8.peritia.network.Packet;
import github.poscard8.peritia.util.serialization.PeritiaResources;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PeritiaResourcesPacket implements Packet
{
    public JsonObject data;

    public PeritiaResourcesPacket(PeritiaResources resources) { this(resources.save()); }

    public PeritiaResourcesPacket(JsonObject data) { this.data = data; }

    public PeritiaResourcesPacket(FriendlyByteBuf buffer) { this.data = readJsonObject(buffer); }

    @Override
    public void encode(FriendlyByteBuf buffer) { writeJsonObject(buffer, data); }

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        clientHandler().handlePeritiaResourcesPacket(this);
        return Result.SUCCESS;
    }

}
