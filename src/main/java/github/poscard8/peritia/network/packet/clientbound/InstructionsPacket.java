package github.poscard8.peritia.network.packet.clientbound;

import github.poscard8.peritia.network.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class InstructionsPacket implements Packet
{
    public static final byte SIMPLE_ID = 0;
    public static final byte DETAILED_ID = 1;
    public static final byte DETAILED_CHEATS_ID = 2;
    
    public byte id;
    
    public InstructionsPacket(byte id) { this.id = id; }

    public InstructionsPacket(FriendlyByteBuf buffer) { this.id = buffer.readByte(); }

    public static InstructionsPacket simple() { return new InstructionsPacket(SIMPLE_ID); }

    public static InstructionsPacket detailed(boolean allowCheats) { return allowCheats ? new InstructionsPacket(DETAILED_CHEATS_ID) : new InstructionsPacket(DETAILED_ID); }

    @Override
    public void encode(FriendlyByteBuf buffer) { buffer.writeByte(id); }

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        clientHandler().handleInstructionsPacket(this);
        return Result.SUCCESS;
    }

}
