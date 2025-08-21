package github.poscard8.peritia.network.packet.clientbound;

import github.poscard8.peritia.network.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class ChestLuckPacket implements Packet
{
    public String itemKey;
    public int rolls;

    public ChestLuckPacket(@Nullable Item icon, int rolls)
    {
        this.rolls = rolls;

        if (icon == null || icon == Items.AIR)
        {
            this.itemKey = "minecraft:chest";
            return;
        }

        ResourceLocation key = ForgeRegistries.ITEMS.getKey(icon);

        this.itemKey = key == null || key.equals(new ResourceLocation("air")) ?
                "minecraft:chest" :
                key.toString();
    }

    public ChestLuckPacket(FriendlyByteBuf buffer)
    {
        this.itemKey = buffer.readUtf();
        this.rolls = buffer.readInt();
    }

    @Override
    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeUtf(itemKey);
        buffer.writeInt(rolls);
    }

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        clientHandler().handleChestLuckPacket(this);
        return Result.SUCCESS;
    }

}
