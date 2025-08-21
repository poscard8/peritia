package github.poscard8.peritia.network.packet.clientbound;

import github.poscard8.peritia.network.Packet;
import github.poscard8.peritia.util.text.PeritiaTexts;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;

public class EncyclopediaUpdatePacket implements Packet
{
    public static final ItemStack ARBITRARY_ITEM_STACK = Items.STONE.getDefaultInstance();

    public EncyclopediaUpdatePacket() {}

    @SuppressWarnings("unused")
    public EncyclopediaUpdatePacket(FriendlyByteBuf buffer) {}

    @Override
    public void encode(FriendlyByteBuf buffer) {}

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        clientHandler().handleEncyclopediaUpdatePacket(this);
        return Result.SUCCESS;
    }

    static
    {
        ARBITRARY_ITEM_STACK.setHoverName(PeritiaTexts.encyclopediaUpdated());
    }

}
