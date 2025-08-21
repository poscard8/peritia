package github.poscard8.peritia.network.packet.clientbound;

import com.google.gson.JsonObject;
import github.poscard8.peritia.network.Packet;
import github.poscard8.peritia.util.skill.XpGainContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class XpGainPacket implements Packet
{
    public XpGainContext context;
    public boolean playSound;

    public XpGainPacket(XpGainContext context, boolean playSound)
    {
        this.context = context;
        this.playSound = playSound;
    }

    public XpGainPacket(FriendlyByteBuf buffer)
    {
        JsonObject data = readJsonObject(buffer);

        this.context = XpGainContext.tryLoad(data);
        this.playSound = buffer.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buffer)
    {
        writeJsonObject(buffer, context.save());
        buffer.writeBoolean(playSound);
    }

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        clientHandler().handleGainXpPacket(this);
        return Result.SUCCESS;
    }

}
