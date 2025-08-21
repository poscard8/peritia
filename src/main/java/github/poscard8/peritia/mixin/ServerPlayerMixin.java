package github.poscard8.peritia.mixin;


import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.mixin.accessor.FurnaceResultSlotAccessor;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.xpsource.type.RecipeXpSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Trigger for {@link RecipeXpSource}.
 */
@Mixin(ServerPlayer.class)
@SuppressWarnings("ALL")
public abstract class ServerPlayerMixin
{
    ServerPlayer self = (ServerPlayer) (Object) this;

    @Inject(method = "triggerRecipeCrafted", at = @At("HEAD"), cancellable = true)
    void peritia$triggerRecipeCrafted(Recipe<?> recipe, List<ItemStack> items, CallbackInfo ci)
    {
        List<RecipeXpSource> recipeXpSources = Peritia.xpSourceHandler().byType(PeritiaXpSourceTypes.RECIPE);
        for (RecipeXpSource xpSource : recipeXpSources)
        {
            if (xpSource.category() == RecipeXpSource.Category.CRAFT)
            {
                xpSource.handleRecipe(self, recipe, items);
                continue;
            }

            if (self.containerMenu instanceof AbstractFurnaceMenu menu)
            {
                FurnaceResultSlot slot = (FurnaceResultSlot) menu.getSlot(menu.getResultSlotIndex());
                int multiplier = ((FurnaceResultSlotAccessor) slot).getRemoveCount();

                xpSource.handleRecipe(self, recipe, items, multiplier);
            }
            else xpSource.handleRecipe(self, recipe, items);
        }
    }
    
}
