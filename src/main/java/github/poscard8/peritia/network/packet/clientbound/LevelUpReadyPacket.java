package github.poscard8.peritia.network.packet.clientbound;

import github.poscard8.peritia.network.Packet;
import github.poscard8.peritia.skill.Skill;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class LevelUpReadyPacket implements Packet
{
    public Skill skill;
    public boolean playSound;

    public LevelUpReadyPacket(Skill skill, boolean playSound)
    {
        this.skill = skill;
        this.playSound = playSound;
    }

    public LevelUpReadyPacket(FriendlyByteBuf buffer)
    {
        String string = buffer.readUtf();
        Skill skill = Skill.byString(string);

        if (skill == null) throw new RuntimeException("Invalid string for skill: " + string);
        this.skill = skill;
        this.playSound = buffer.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeUtf(skill.stringKey());
        buffer.writeBoolean(playSound);
    }

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        clientHandler().handleLevelUpReadyPacket(this);
        return Result.SUCCESS;
    }

}
