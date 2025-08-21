package github.poscard8.peritia.network.packet.clientbound;

import com.google.gson.JsonObject;
import github.poscard8.peritia.network.Packet;
import github.poscard8.peritia.skill.data.ServerSkillData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SkillDataPacket implements Packet
{
    public JsonObject data;

    public SkillDataPacket(ServerSkillData serverSkillData) { this(serverSkillData.saveForClient()); }

    public SkillDataPacket(JsonObject data) { this.data = data; }

    public SkillDataPacket(FriendlyByteBuf buffer) { this.data = readJsonObject(buffer); }

    @Override
    public void encode(FriendlyByteBuf buffer) { writeJsonObject(buffer, data); }

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        clientHandler().handleSkillDataPacket(this);
        return Result.SUCCESS;
    }

}
