package github.poscard8.peritia.network.packet.serverbound;

import github.poscard8.peritia.network.Packet;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.type.GrindstoneXpSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class UseGrindstonePacket implements Packet
{
    public GrindstoneXpSource xpSource;
    public int multiplier;

    public UseGrindstonePacket(GrindstoneXpSource xpSource, int multiplier)
    {
        this.xpSource = xpSource;
        this.multiplier = multiplier;
    }

    public UseGrindstonePacket(FriendlyByteBuf buffer)
    {
        String string = buffer.readUtf();
        int multiplier = buffer.readInt();

        ResourceLocation key = ResourceLocation.tryParse(string);
        if (key == null) throw new RuntimeException(String.format("Invalid xp source key: %s", string));

        XpSource xpSource = XpSource.byKey(key);
        if (xpSource == null) throw new RuntimeException(String.format("Invalid xp source key: %s", string));

        this.xpSource = (GrindstoneXpSource) xpSource;
        this.multiplier = multiplier;
    }

    @Override
    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeUtf(xpSource.stringKey());
        buffer.writeInt(multiplier);
    }

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        ServerPlayer player = context.getSender();
        if (player == null) return Result.PASS;

        xpSource.addWaitingXp(player, multiplier);
        return Result.SUCCESS;
    }


}
