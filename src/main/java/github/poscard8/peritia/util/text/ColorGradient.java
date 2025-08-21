package github.poscard8.peritia.util.text;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.util.serialization.SerializableDate;
import github.poscard8.peritia.util.text.insertion.ColorGradientInsertion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
public class ColorGradient implements UnaryOperator<Style>
{
    public static final int FPS = 40;
    public static final int FRAME_TIME = 25;

    public final ResourceLocation key;
    public final List<Pulse> pulses;

    public ColorGradient(String path, int... colors) { this(Peritia.asResource(path), colors); }

    public ColorGradient(ResourceLocation key, int... colors)
    {
        this.key = key;
        this.pulses = new ArrayList<>();

        for (int color : colors) addPulse(color);
    }

    @Nullable
    public static ColorGradient byKey(ResourceLocation key) { return ColorGradients.byKey(key); }

    @Nullable
    public static ColorGradient ofNbt(@Nullable CompoundTag nbt)
    {
        if (nbt == null) return null;

        String string = nbt.getString(ColorGradientInsertion.PREFIX);
        ResourceLocation key = ResourceLocation.tryParse(string);

        return !string.isEmpty() && key != null ? byKey(key) : null;
    }

    public static void addToItem(ColorGradient gradient, ItemStack stack)
    {
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putString(ColorGradientInsertion.PREFIX, gradient.key.toString());
    }

    public static void removeFromItem(ItemStack stack)
    {
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.remove(ColorGradientInsertion.PREFIX);

        if (nbt.isEmpty()) stack.setTag(null);
    }

    public ColorGradient addPulse(int color) { return addPulse(color, 1000); }

    public ColorGradient addPulse(int color, int milliseconds)
    {
        pulses.add(new Pulse(color, milliseconds));
        return this;
    }

    public ColorGradient addNonTransitionPulse(int color, int milliseconds) { return addLongPulse(color, milliseconds, 0); }

    public ColorGradient addLongPulse(int color, int stayMilliseconds, int fadeMilliseconds)
    {
        addPulse(color, stayMilliseconds);
        return addPulse(color, fadeMilliseconds);
    }

    public int getPeriod()
    {
        int period = 0;
        for (Pulse pulse : pulses) period += pulse.milliseconds();
        return period;
    }


    @Nullable
    public TextColor getTextColor()
    {
        if (pulses.isEmpty()) return null;
        if (pulses.size() == 1) return TextColor.fromRgb(pulses.get(0).color());

        int time = 0;

        Calendar calendar = SerializableDate.calendar();
        time += calendar.get(Calendar.MINUTE) * 60000;
        time += calendar.get(Calendar.SECOND) * 1000;
        time += calendar.get(Calendar.MILLISECOND);

        int remainderTime = time % getPeriod();
        int color = 0xFFFFFF;

        for (int i = 0; i < pulses.size(); i++)
        {
            Pulse current = pulses.get(i);
            int ms = current.milliseconds();

            if (remainderTime >= ms)
            {
                remainderTime -= ms;
                continue;
            }

            int nextIndex = i + 1 == pulses.size() ? 0 : i + 1;
            Pulse next = pulses.get(nextIndex);

            color = getColorInBetween(current, next, remainderTime);
            break;
        }
        return TextColor.fromRgb(color);
    }

    int getColorInBetween(Pulse current, Pulse next, int remainderTime)
    {
        int period = current.milliseconds();
        int totalTicks = period / FRAME_TIME;
        int ticks = remainderTime / FRAME_TIME;
        int remaining = totalTicks - ticks;

        int color = current.color();
        int nextColor = next.color();

        int[] firstColorRGB = new int[]{color / 65536, (color / 256) % 256, color % 256};
        int[] secondColorRGB = new int[]{nextColor / 65536, (nextColor / 256) % 256, nextColor % 256};

        int red = (firstColorRGB[0] * remaining + secondColorRGB[0] * ticks) / totalTicks;
        int green = (firstColorRGB[1] * remaining + secondColorRGB[1] * ticks) / totalTicks;
        int blue = (firstColorRGB[2] * remaining + secondColorRGB[2] * ticks) / totalTicks;

        return red * 65536 + green * 256 + blue;
    }

    public ColorGradientInsertion asInsertion() { return new ColorGradientInsertion(this); }

    public Rarity asRarity() { return Rarity.create(key.toString(), this); }

    @Override
    public Style apply(Style style) { return style.withInsertion(asInsertion().save()); }


    public record Pulse(int color, int milliseconds) {}

}
