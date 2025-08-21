package github.poscard8.peritia.network.packet.serverbound;

import github.poscard8.peritia.network.Packet;
import github.poscard8.peritia.skill.recipe.SkillRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * Used for skill recipes, not regular crafting.
 */
public class CraftPacket implements Packet
{
    public SkillRecipe recipe;
    public boolean craftStack;

    public CraftPacket(SkillRecipe recipe, boolean craftStack)
    {
        this.recipe = recipe;
        this.craftStack = craftStack;
    }

    public CraftPacket(FriendlyByteBuf buffer)
    {
        String string = buffer.readUtf();
        boolean craftStack = buffer.readBoolean();

        ResourceLocation key = ResourceLocation.tryParse(string);
        if (key == null) throw new RuntimeException(String.format("Invalid skill recipe key: %s", string));

        SkillRecipe recipe = SkillRecipe.byKey(key);
        if (recipe == null) throw new RuntimeException(String.format("Invalid skill recipe key: %s", string));

        this.recipe = recipe;
        this.craftStack = craftStack;
    }

    @Override
    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeUtf(recipe.stringKey());
        buffer.writeBoolean(craftStack);
    }

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        ServerPlayer player = context.getSender();
        if (player == null) return Result.PASS;

        if (craftStack)
        {
            recipe.tryCraftStack(player);
        }
        else recipe.tryCraft(player);

        return Result.SUCCESS;
    }

}
