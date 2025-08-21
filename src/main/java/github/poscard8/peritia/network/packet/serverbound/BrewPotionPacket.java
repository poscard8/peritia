package github.poscard8.peritia.network.packet.serverbound;

import github.poscard8.peritia.network.Packet;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.type.PotionXpSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class BrewPotionPacket implements Packet
{
    public PotionXpSource xpSource;

    public BrewPotionPacket(PotionXpSource xpSource) { this.xpSource = xpSource; }

    public BrewPotionPacket(FriendlyByteBuf buffer)
    {
        String string = buffer.readUtf();

        ResourceLocation key = ResourceLocation.tryParse(string);
        if (key == null) throw new RuntimeException(String.format("Invalid xp source key: %s", string));

        XpSource xpSource = XpSource.byKey(key);
        if (xpSource == null) throw new RuntimeException(String.format("Invalid xp source key: %s", string));

        this.xpSource = (PotionXpSource) xpSource;
    }

    @Override
    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeUtf(xpSource.stringKey());
    }

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        ServerPlayer player = context.getSender();
        if (player == null) return Result.PASS;

        xpSource.addWaitingXp(player);
        return Result.SUCCESS;
    }

}
