package github.poscard8.peritia.network.packet.clientbound;

import com.google.gson.JsonArray;
import github.poscard8.peritia.network.Packet;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.skill.LevelUpContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class LevelUpPacket implements Packet
{
    public LevelUpContext context;
    public boolean manually;

    public LevelUpPacket(Skill skill, int oldLevel, int newLevel, boolean manually)
    {
        this.context = new LevelUpContext();
        this.manually = manually;

        context.add(skill, oldLevel, newLevel);
    }

    public LevelUpPacket(LevelUpContext context, boolean manually)
    {
        this.context = context;
        this.manually = manually;
    }

    public LevelUpPacket(FriendlyByteBuf buffer)
    {
        JsonArray data = readJsonArray(buffer);

        this.context = LevelUpContext.tryLoad(data);
        this.manually = buffer.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buffer)
    {
        writeJsonArray(buffer, context.save());
        buffer.writeBoolean(manually);
    }

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        clientHandler().handleLevelUpPacket(this);
        return Result.SUCCESS;
    }

}
