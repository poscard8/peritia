package github.poscard8.peritia.mixin.client.accessor;

import net.minecraft.client.gui.Gui;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Gui.class)
@OnlyIn(Dist.CLIENT)
@SuppressWarnings("ALL")
public interface GuiAccessor
{
    @Accessor("lastToolHighlight")
    ItemStack getLastToolHighlight();

    @Accessor("lastToolHighlight")
    void setLastToolHighLight(ItemStack lastToolHighLight);

    @Accessor("toolHighlightTimer")
    int getToolHighlightTimer();

    @Accessor("toolHighlightTimer")
    void setToolHighlightTimer(int toolHighlightTimer);

}
