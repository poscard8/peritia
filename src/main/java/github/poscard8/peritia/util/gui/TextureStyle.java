package github.poscard8.peritia.util.gui;

import net.minecraft.ChatFormatting;

public enum TextureStyle
{
    VANILLA("_vanilla", ChatFormatting.WHITE, 0x6A6A6A, 0x474747, 0x6A6A6A, 0x2B3460),
    MODDED("", ChatFormatting.GRAY, 0x404040, 0x664C00, 0x706A62, 0x383870);

    private final String suffix;
    private final int[] textColors;
    private final ChatFormatting searchTextColor;

    TextureStyle(String suffix, ChatFormatting searchTextColor, int... textColors)
    {
        this.suffix = suffix;
        this.searchTextColor = searchTextColor;
        this.textColors = textColors;
    }

    public String suffix() { return suffix; }

    public ChatFormatting searchTextColor() { return searchTextColor; }

    public int primaryTextColor() { return textColors[0]; }

    public int secondaryTextColor() { return textColors[1]; }

    public int tertiaryTextColor() { return textColors[2]; }

    public int hoveredTextColor() { return textColors[3]; }

}
