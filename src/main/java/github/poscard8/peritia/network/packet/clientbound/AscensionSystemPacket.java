package github.poscard8.peritia.network.packet.clientbound;

import com.google.gson.JsonObject;
import github.poscard8.peritia.ascension.ServerAscensionSystem;
import github.poscard8.peritia.network.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class AscensionSystemPacket implements Packet
{
    public JsonObject data;

    public AscensionSystemPacket(ServerAscensionSystem ascensionSystem) { this(ascensionSystem.save()); }

    public AscensionSystemPacket(JsonObject data) { this.data = data; }

    public AscensionSystemPacket(FriendlyByteBuf buffer) { this.data = readJsonObject(buffer); }

    @Override
    public void encode(FriendlyByteBuf buffer) { writeJsonObject(buffer, data); }

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        clientHandler().handleAscensionSystemPacket(this);
        return Result.SUCCESS;
    }

}
