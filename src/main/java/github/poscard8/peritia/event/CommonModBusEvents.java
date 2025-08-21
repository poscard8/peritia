package github.poscard8.peritia.event;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.registry.PeritiaAttributes;
import github.poscard8.peritia.util.PeritiaHooks;
import github.poscard8.peritia.util.PeritiaRegistries;
import github.poscard8.peritia.util.forge.ConfigCondition;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.NewRegistryEvent;

@Mod.EventBusSubscriber(modid = Peritia.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@SuppressWarnings("unused")
public class CommonModBusEvents
{
    @SubscribeEvent
    static void addRegistries(NewRegistryEvent event)
    {
        PeritiaRegistries.REWARD_TYPES_SUPPLIER = event.create(PeritiaRegistries.REWARD_TYPES_BUILDER);
        PeritiaRegistries.XP_SOURCE_TYPES_SUPPLIER = event.create(PeritiaRegistries.XP_SOURCE_TYPES_BUILDER);
    }

    @SubscribeEvent
    static void onAttributeModification(EntityAttributeModificationEvent event)
    {
        event.add(EntityType.PLAYER, PeritiaAttributes.WISDOM.get());
        event.add(EntityType.PLAYER, PeritiaAttributes.CHEST_LUCK.get());
        event.add(EntityType.PLAYER, PeritiaAttributes.CRIT_DAMAGE.get());
        event.add(EntityType.PLAYER, PeritiaAttributes.EXTRA_CRIT_CHANCE.get());
        event.add(EntityType.PLAYER, PeritiaAttributes.BLOCK_BREAK_SPEED.get());
    }

    @SubscribeEvent
    static void onCommonSetup(FMLCommonSetupEvent event)
    {
        CraftingHelper.register(ConfigCondition.Serializer.INSTANCE);
        PeritiaHooks.postAscensionConfigureEvent();
    }

}
