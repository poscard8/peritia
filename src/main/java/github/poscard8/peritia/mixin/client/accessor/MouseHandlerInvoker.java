package github.poscard8.peritia.mixin.client.accessor;

import net.minecraft.client.MouseHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MouseHandler.class)
@OnlyIn(Dist.CLIENT)
@SuppressWarnings("ALL")
public interface MouseHandlerInvoker
{
    @Invoker("onMove")
    void invokeMove(long window, double mouseX, double mouseY);

}
