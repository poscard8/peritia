package github.poscard8.peritia.network.packet.clientbound;

import github.poscard8.peritia.network.Packet;
import github.poscard8.peritia.skill.Skill;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class EncyclopediaCompletePacket implements Packet
{
    public Skill skill;

    public EncyclopediaCompletePacket(Skill skill) { this.skill = skill; }

    public EncyclopediaCompletePacket(FriendlyByteBuf buffer)
    {
        String string = buffer.readUtf();
        Skill skill = Skill.byString(string);

        if (skill == null) throw new RuntimeException("Invalid string for skill: " + string);
        this.skill = skill;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) { buffer.writeUtf(skill.stringKey()); }

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        clientHandler().handleEncyclopediaCompletePacket(this);
        return Result.SUCCESS;
    }

}
