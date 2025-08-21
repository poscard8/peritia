package github.poscard8.peritia.network.packet.serverbound;

import github.poscard8.peritia.network.Packet;
import github.poscard8.peritia.skill.data.ServerSkillData;
import github.poscard8.peritia.util.text.PeritiaTexts;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;

public class AscendPacket implements Packet
{
    public AscendPacket() {}

    @SuppressWarnings("unused")
    public AscendPacket(FriendlyByteBuf buffer) {}

    @Override
    public void encode(FriendlyByteBuf buffer) {}

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        ServerPlayer player = context.getSender();
        if (player == null) return Result.PASS;

        ServerSkillData skillData = ServerSkillData.of(player);
        skillData.ascend(true);

        Objects.requireNonNull(player.getServer()).getPlayerList().broadcastSystemMessage(PeritiaTexts.playerAscended(player, skillData.legacyScore()), false);
        return Result.SUCCESS;
    }

}
