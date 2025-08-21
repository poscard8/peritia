package github.poscard8.peritia.network.packet.clientbound;

import com.google.gson.JsonObject;
import github.poscard8.peritia.network.Packet;
import github.poscard8.peritia.util.minecraft.GameContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.network.NetworkEvent;

public class GameContextPacket implements Packet
{
    public JsonObject data;

    public GameContextPacket(MinecraftServer server) { this(new GameContext(server)); }

    public GameContextPacket(GameContext gameContext) { this(gameContext.save()); }

    public GameContextPacket(JsonObject data) { this.data = data; }

    public GameContextPacket(FriendlyByteBuf buffer) { this.data = readJsonObject(buffer); }

    @Override
    public void encode(FriendlyByteBuf buffer) { writeJsonObject(buffer, data); }

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        clientHandler().handleGameContextPacket(this);
        return Result.SUCCESS;
    }

}
