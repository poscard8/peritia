package github.poscard8.peritia.registry;

import github.poscard8.peritia.Peritia;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PeritiaSoundEvents
{
    public static final DeferredRegister<SoundEvent> ALL = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Peritia.ID);

    public static final RegistryObject<SoundEvent> XP_GAIN = registerSound("xp_gain");
    public static final RegistryObject<SoundEvent> LEVEL_UP = registerSound("level_up");
    public static final RegistryObject<SoundEvent> NOTIFY = registerSound("notify");

    static RegistryObject<SoundEvent> registerSound(String name) { return ALL.register(name, () -> SoundEvent.createVariableRangeEvent(Peritia.asResource(name))); }

    public static void register(IEventBus bus) { ALL.register(bus); }
}
