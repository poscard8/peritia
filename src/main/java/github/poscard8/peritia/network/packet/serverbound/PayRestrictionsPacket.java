package github.poscard8.peritia.network.packet.serverbound;

import github.poscard8.peritia.network.Packet;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.data.ServerSkillData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class PayRestrictionsPacket implements Packet
{
    public Skill skill;
    public int level;

    public PayRestrictionsPacket(Skill skill, int level)
    {
        this.skill = skill;
        this.level = level;
    }

    public PayRestrictionsPacket(FriendlyByteBuf buffer)
    {
        String string = buffer.readUtf();
        int level = buffer.readInt();

        ResourceLocation key = ResourceLocation.tryParse(string);
        if (key == null) throw new RuntimeException(String.format("Invalid skill key: %s", string));

        Skill skill = Skill.byKey(key);
        if (skill == null) throw new RuntimeException(String.format("Invalid skill key: %s", string));

        this.skill = skill;
        this.level = level;
    }

    @Override
    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeUtf(skill.stringKey());
        buffer.writeInt(level);
    }

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        ServerPlayer player = context.getSender();
        if (player == null) return Result.PASS;

        ServerSkillData.of(player).payRestrictions(skill, level);
        return Result.SUCCESS;
    }

}
