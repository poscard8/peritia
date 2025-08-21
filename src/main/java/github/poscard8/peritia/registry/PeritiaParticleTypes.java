package github.poscard8.peritia.registry;

import github.poscard8.peritia.Peritia;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PeritiaParticleTypes
{
    public static final DeferredRegister<ParticleType<?>> ALL = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Peritia.ID);

    public static final RegistryObject<SimpleParticleType> LEVEL_UP = ALL.register("level_up", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> ASCENSION = ALL.register("ascension", () -> new SimpleParticleType(false));

    public static void register(IEventBus bus) { ALL.register(bus); }

}
