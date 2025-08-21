package github.poscard8.peritia.util.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Roman numeral system that works with numbers 1-1000.
 */
public class RomanNumeral
{
    static final String[] HUNDREDS = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
    static final String[] TENS = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
    static final String[] UNITS = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

    final String string;

    public RomanNumeral(String string) { this.string = string; }

    public static RomanNumeral of(int number)
    {
        if (number < 0 || number > 1000) return new RomanNumeral(Integer.toString(number));
        if (number == 0) return new RomanNumeral("∅");
        if (number == 1000) return new RomanNumeral("M");

        int firstDigit = number / 100;
        int secondDigit = (number % 100) / 10;
        int thirdDigit = number % 10;

        return new RomanNumeral(HUNDREDS[firstDigit] + TENS[secondDigit] + UNITS[thirdDigit]);
    }

    public MutableComponent asText() { return Component.literal(string); }

    @Override
    public String toString() { return string; }

}
