package github.poscard8.peritia.network.packet.clientbound;

import com.google.gson.JsonObject;
import github.poscard8.peritia.network.Packet;
import github.poscard8.peritia.util.minecraft.SimpleAttributeMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class AttributePacket implements Packet
{
    public JsonObject data;

    public AttributePacket(ServerPlayer player) { this(new SimpleAttributeMap(player)); }

    public AttributePacket(SimpleAttributeMap attributeMap) { this(attributeMap.save()); }

    public AttributePacket(JsonObject data) { this.data = data; }

    public AttributePacket(FriendlyByteBuf buffer) { this.data = readJsonObject(buffer); }

    @Override
    public void encode(FriendlyByteBuf buffer) { writeJsonObject(buffer, data); }

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        clientHandler().handleAttributePacket(this);
        return Result.SUCCESS;
    }

}
