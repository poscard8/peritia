package github.poscard8.peritia.util.text;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ColorGradients
{
    static final Map<ResourceLocation, ColorGradient> MAP = new HashMap<>();

    @Nullable
    public static ColorGradient byKey(ResourceLocation key) { return MAP.get(key); }

    public static ColorGradient register(ColorGradient gradient) { return MAP.put(gradient.key, gradient); }

    public static void register()
    {
        register(EMPTY);
        register(NOTIFY);
        register(RAINBOW);
        register(GALAXY);
        register(IRON);
        register(GOLD);
        register(DIAMOND);
        register(NETHERITE);
        register(AMETHYST);
        register(EMERALD);
        register(COMPOSITE);
    }

    public static final ColorGradient EMPTY = new ColorGradient("empty");

    public static final ColorGradient NOTIFY = new ColorGradient("notify")
            .addNonTransitionPulse(0xFF55FF, 200)
            .addNonTransitionPulse(0xC700AA, 200);

    public static final ColorGradient RAINBOW = new ColorGradient("rainbow")
            .addLongPulse(0xFF0000, 300, 200)
            .addLongPulse(0xFF7F00, 300, 200)
            .addLongPulse(0xFFFF00, 300, 200)
            .addLongPulse(0x7FFF00, 300, 200)
            .addLongPulse(0x00FF00, 300, 200)
            .addLongPulse(0x00FF7F, 300, 200)
            .addLongPulse(0x00FFFF, 300, 200)
            .addLongPulse(0x007FFF, 300, 200)
            .addLongPulse(0x0000FF, 300, 200)
            .addLongPulse(0x7F00FF, 300, 200)
            .addLongPulse(0xFF00FF, 300, 200)
            .addLongPulse(0xFF007F, 300, 200);

    public static final ColorGradient GALAXY = new ColorGradient("galaxy")
            .addPulse(0x3F3F3F)
            .addPulse(0x0D2673)
            .addLongPulse(0x4A067A, 800, 200)
            .addPulse(0xBFBFBF, 200);

    public static final ColorGradient IRON = new ColorGradient("iron")
            .addLongPulse(0x828282, 175, 75)
            .addLongPulse(0xD8D8D8, 175, 75)
            .addLongPulse(0xFFFFFF, 175, 75)
            .addLongPulse(0xD8D8D8, 175, 75);

    public static final ColorGradient GOLD = new ColorGradient("gold")
            .addPulse(0xDC9612, 500)
            .addPulse(0xEBC83F, 500)
            .addPulse(0xFFEAB0, 500)
            .addPulse(0xEBC83F, 500);

    public static final ColorGradient DIAMOND = new ColorGradient("diamond")
            .addLongPulse(0x4AEDD9, 100, 1100)
            .addLongPulse(0x189994, 100, 200)
            .addLongPulse(0xFFFFFF, 100, 200)
            .addLongPulse(0x189994, 100, 1100);

    public static final ColorGradient NETHERITE = new ColorGradient("netherite")
            .addLongPulse(0xCFCFCF, 25, 400)
            .addNonTransitionPulse(0x4D494D, 25)
            .addLongPulse(0xCFCFCF, 25, 400)
            .addNonTransitionPulse(0x4D494D, 25)
            .addLongPulse(0xFFFFFF, 25, 675)
            .addPulse(0x4C4042, 500)
            .addNonTransitionPulse(0x271C1D, 200)
            .addNonTransitionPulse(0xCFCFCF, 200)
            .addNonTransitionPulse(0x7F3F4B, 200)
            .addLongPulse(0xFFFFFF, 200, 600)
            .addPulse(0x4C4042, 500)
            .addNonTransitionPulse(0x271C1D, 200);

    public static final ColorGradient AMETHYST = new ColorGradient("amethyst")
            .addPulse(0x8D6ACC, 2000)
            .addPulse(0xCFA0F4, 2000)
            .addPulse(0xFFCCE7, 2000);

    public static final ColorGradient EMERALD = new ColorGradient("emerald")
            .addNonTransitionPulse(0x00AA2C, 400)
            .addNonTransitionPulse(0x82F692, 400)
            .addNonTransitionPulse(0x17DD62, 400)
            .addNonTransitionPulse(0xD0FFE0, 400);

    public static final ColorGradient COMPOSITE = new ColorGradient("composite")
            .addLongPulse(0xC0A0E0, 25, 400)
            .addNonTransitionPulse(0x4D494D, 25)
            .addLongPulse(0xFDD057, 25, 400)
            .addNonTransitionPulse(0x4D494D, 25)
            .addLongPulse(0x82F692, 25, 675)
            .addNonTransitionPulse(0x4C4042, 200)
            .addNonTransitionPulse(0x4AEDD9, 200)
            .addNonTransitionPulse(0x7F3F4B, 200)
            .addLongPulse(0xFFFFFF, 200, 600)
            .addNonTransitionPulse(0x4C4042, 200);


}
