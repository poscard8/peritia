package github.poscard8.peritia.mixin;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.xpsource.type.EnchantmentXpSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Trigger for {@link EnchantmentXpSource}.
 */
@Mixin(EnchantmentMenu.class)
@SuppressWarnings("ALL")
public abstract class EnchantmentMenuMixin
{
    @Shadow
    Container enchantSlots;

    EnchantmentMenu self = (EnchantmentMenu) (Object) this;

    @Inject(method = "clickMenuButton", at = @At("RETURN"))
    void peritia$clickMenuButton(Player player, int index, CallbackInfoReturnable<Boolean> ci)
    {
        if (ci.getReturnValue() && player instanceof ServerPlayer serverPlayer)
        {
            ItemStack stack = enchantSlots.getItem(0);

            List<EnchantmentXpSource> enchantmentXpSources = Peritia.xpSourceHandler().byType(PeritiaXpSourceTypes.ENCHANTMENT);
            for (EnchantmentXpSource xpSource : enchantmentXpSources) xpSource.handleItem(serverPlayer, stack);
        }
    }

}
