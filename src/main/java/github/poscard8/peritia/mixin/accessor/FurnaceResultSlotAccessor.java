package github.poscard8.peritia.mixin.accessor;

import net.minecraft.world.inventory.FurnaceResultSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FurnaceResultSlot.class)
@SuppressWarnings("ALL")
public interface FurnaceResultSlotAccessor
{
    @Accessor
    int getRemoveCount();

}
