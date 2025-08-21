package github.poscard8.peritia.registry;

import github.poscard8.peritia.Peritia;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PeritiaAttributes
{
    public static final DeferredRegister<Attribute> ALL = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Peritia.ID);

    public static final RegistryObject<Attribute> WISDOM = ALL.register("player.wisdom", () -> new RangedAttribute("peritia.player.wisdom", 0D, -100D, Short.MAX_VALUE));
    public static final RegistryObject<Attribute> CHEST_LUCK = ALL.register("player.chest_luck", () -> new RangedAttribute("peritia.player.chest_luck", 0D, 0D, Short.MAX_VALUE));
    public static final RegistryObject<Attribute> CRIT_DAMAGE = ALL.register("player.crit_damage", () -> new RangedAttribute("peritia.player.crit_damage", 50D, -100D, Short.MAX_VALUE));
    public static final RegistryObject<Attribute> EXTRA_CRIT_CHANCE = ALL.register("player.extra_crit_chance", () -> new RangedAttribute("peritia.player.extra_crit_chance", 0D, 0D, 100D));
    public static final RegistryObject<Attribute> BLOCK_BREAK_SPEED = ALL.register("player.block_break_speed", () -> new RangedAttribute("peritia.player.block_break_speed", 1D, 0D, 1024D)); // backport

    public static void register(IEventBus bus) { ALL.register(bus); }

}
