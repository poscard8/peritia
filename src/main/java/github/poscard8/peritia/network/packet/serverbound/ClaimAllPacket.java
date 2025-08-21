package github.poscard8.peritia.network.packet.serverbound;

import github.poscard8.peritia.network.Packet;
import github.poscard8.peritia.skill.data.ServerSkillData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ClaimAllPacket implements Packet
{
    public ClaimAllPacket() {}

    @SuppressWarnings("unused")
    public ClaimAllPacket(FriendlyByteBuf buffer) {}

    @Override
    public void encode(FriendlyByteBuf buffer) {}

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        ServerPlayer player = context.getSender();
        if (player == null) return Result.PASS;

        ServerSkillData.of(player).claimAllRewards();
        return Result.SUCCESS;
    }

}
