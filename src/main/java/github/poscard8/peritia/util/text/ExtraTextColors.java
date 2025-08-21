package github.poscard8.peritia.util.text;

import net.minecraft.network.chat.Style;

import java.util.function.UnaryOperator;

public class ExtraTextColors
{
    public static final UnaryOperator<Style> SKY_BLUE = create(0x55AAFF);
    public static final UnaryOperator<Style> DARKER_AQUA = create(0x006F6F);
    public static final UnaryOperator<Style> BROWN = create(0x904800);
    public static final UnaryOperator<Style> OLIVE = create(0x85D143);

    public static UnaryOperator<Style> create(int value) { return style -> style.withColor(value); }

}
