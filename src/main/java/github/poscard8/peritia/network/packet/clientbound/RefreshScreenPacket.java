package github.poscard8.peritia.network.packet.clientbound;

import github.poscard8.peritia.network.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class RefreshScreenPacket implements Packet
{
    public RefreshScreenPacket() {}

    @SuppressWarnings("unused")
    public RefreshScreenPacket(FriendlyByteBuf buffer) {}

    @Override
    public void encode(FriendlyByteBuf buffer) {}

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        clientHandler().handleRefreshScreenPacket(this);
        return Result.SUCCESS;
    }

}
