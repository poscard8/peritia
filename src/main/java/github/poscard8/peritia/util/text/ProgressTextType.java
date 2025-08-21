package github.poscard8.peritia.util.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.BiFunction;

public enum ProgressTextType
{
    NUMERIC(ProgressTextType::getNumericText),
    PERCENTILE(ProgressTextType::getPercentileText);

    final BiFunction<Integer, Integer, MutableComponent> textFunction;

    ProgressTextType(BiFunction<Integer, Integer, MutableComponent> textFunction) { this.textFunction = textFunction; }

    public static MutableComponent getNumericText(int xp, int neededXp)
    {
        String xpString = PeritiaTexts.format(xp);
        String neededXpString = PeritiaTexts.format(neededXp);
        String finalString = String.format("%s/%s", xpString, neededXpString);

        return Component.literal(finalString);
    }

    public static MutableComponent getPercentileText(int xp, int neededXp)
    {
        float ratio = (float) xp / (float) neededXp;
        float percentage = 100 * ratio;

        return PeritiaTexts.makePercentage(percentage);
    }

    public MutableComponent getText(int xp, int neededXp) { return textFunction.apply(xp, neededXp); }

    public MutableComponent getTextWithParenthesis(int xp, int neededXp)
    {
        String string = String.format("(%s)", getText(xp, neededXp).getString());
        return Component.literal(string);
    }

}
