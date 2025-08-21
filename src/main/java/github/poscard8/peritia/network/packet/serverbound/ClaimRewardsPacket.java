package github.poscard8.peritia.network.packet.serverbound;

import github.poscard8.peritia.network.Packet;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.data.ServerSkillData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ClaimRewardsPacket implements Packet
{
    public Skill skill;
    public int oldLevel;
    public int newLevel;

    public ClaimRewardsPacket(Skill skill, int level) { this(skill, level - 1, level); }

    public ClaimRewardsPacket(Skill skill, int oldLevel, int newLevel)
    {
        this.skill = skill;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public ClaimRewardsPacket(FriendlyByteBuf buffer)
    {
        String string = buffer.readUtf();
        int oldLevel = buffer.readInt();
        int newLevel = buffer.readInt();

        ResourceLocation key = ResourceLocation.tryParse(string);
        if (key == null) throw new RuntimeException(String.format("Invalid skill key: %s", string));

        Skill skill = Skill.byKey(key);
        if (skill == null) throw new RuntimeException(String.format("Invalid skill key: %s", string));

        this.skill = skill;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    @Override
    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeUtf(skill.stringKey());
        buffer.writeInt(oldLevel);
        buffer.writeInt(newLevel);
    }

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        ServerPlayer player = context.getSender();
        if (player == null) return Result.PASS;

        ServerSkillData.of(player).claimRewards(skill, oldLevel, newLevel);
        return Result.SUCCESS;
    }

}
