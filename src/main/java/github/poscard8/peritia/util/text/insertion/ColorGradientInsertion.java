package github.poscard8.peritia.util.text.insertion;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.util.serialization.StringSerializable;
import github.poscard8.peritia.util.text.ColorGradient;
import github.poscard8.peritia.util.text.ColorGradients;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record ColorGradientInsertion(ColorGradient gradient) implements StringSerializable<ColorGradientInsertion>
{
    public static final String PREFIX = "colorGradient";

    public static ColorGradientInsertion empty() { return new ColorGradientInsertion(ColorGradients.EMPTY); }

    @Nullable
    public static ColorGradientInsertion tryLoad(@Nullable String insertion) { return empty().loadWithFallback(insertion); }

    @Override
    @Nullable
    public ColorGradientInsertion fallback() { return null; }

    public ColorGradientInsertion load(@Nullable String data)
    {
        if (data == null || !data.startsWith(PREFIX)) return null;

        String[] split = data.split(",");
        String stringKey = split[1];
        ResourceLocation key = ResourceLocation.tryParse(stringKey);
        if (key == null)
        {
            Peritia.LOGGER.error("Invalid color gradient key: {}", stringKey);
            return null;
        }

        ColorGradient gradient = ColorGradients.byKey(key);
        return gradient == null ? null : new ColorGradientInsertion(gradient);
    }

    public String save() { return String.format("%s,%s", PREFIX, gradient.key); }

    @Override
    public String toString() { return save(); }

}
