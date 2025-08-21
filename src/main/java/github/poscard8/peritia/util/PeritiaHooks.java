package github.poscard8.peritia.util;

import github.poscard8.peritia.event.mod.AscensionSystemConfigureEvent;
import net.minecraftforge.fml.ModLoader;

public class PeritiaHooks
{
    public static void postAscensionConfigureEvent()
    {
        AscensionSystemConfigureEvent event = new AscensionSystemConfigureEvent();
        ModLoader.get().postEvent(event);
        event.apply();
    }

}
