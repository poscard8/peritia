package github.poscard8.peritia.util.skill;

import com.google.gson.JsonObject;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;

/**
 * Polynomials for skills according to max skill levels:
 * (will be used in Poscard's Skills)
 * 60: .04x^3+0.6x^2+18x+5
 * 40: .025x^3+2.5x^2+45x
 * 30: 5x^2+55x
 * 20: 200x
 */
public class LevelXpFunction implements JsonSerializable<LevelXpFunction>
{
    protected Polynomial polynomial = Polynomial.of(0.025D, 2.5D, 45, 0); // ideal for a skill with 40 levels
    protected boolean round = true;

    public LevelXpFunction() {}

    public static LevelXpFunction empty() { return new LevelXpFunction(); }

    public static LevelXpFunction tryLoad(JsonObject data) { return empty().loadWithFallback(data); }

    public static LevelXpFunction of(Polynomial polynomial, boolean round)
    {
        LevelXpFunction xpFunction = empty();
        xpFunction.polynomial = polynomial;
        xpFunction.round = round;
        return xpFunction;
    }

    public Polynomial polynomial() { return polynomial; }

    public boolean rounds() { return round; }

    public int getRoundingCoefficient(double value)
    {
        if (value <= 200) return 25;

        double log = Math.log10(value);
        int intLog = (int) log;
        double logRemainder = log - intLog;

        int coefficient = (int) Math.pow(10, intLog);     // 0.60206 = log10(4), 0.30103 = log10(2)
        return logRemainder >= 0.60206D ? coefficient : logRemainder >= 0.30103D ? coefficient / 2 : coefficient / 4;
    }

    /**
     * XP needed to get to level <i>n</i> from <i>n-1</i>
     */
    public int getNeededXp(int level)
    {
        if (!rounds()) return polynomial().evaluateRounded(level);

        double actualValue = polynomial().evaluate(level);
        int roundCoefficient = getRoundingCoefficient(actualValue);

        double remainder = actualValue % roundCoefficient;
        double roundedBack = actualValue - remainder;
        double roundedForward = roundedBack + roundCoefficient;

        double finalValue = remainder >= (roundCoefficient / 2.0D) ? roundedForward : roundedBack;
        return (int) Math.round(finalValue);
    }

    /**
     * XP needed to get to level <i>n</i> from <i>m</i>
     */
    public int getNeededXp(int oldLevel, int newLevel)
    {
        int xp = 0;

        for (int i = oldLevel + 1; i <= newLevel; i++)
        {
            xp += getNeededXp(i);
        }
        return xp;
    }

    public int getNeededTotalXp(Skill skill) { return getNeededXp(skill.minLevel(), skill.maxLevel()); }

    /**
     * XP needed to get to level <i>x</i> from <i>beginning</i>
     */
    public int getNeededTotalXp(Skill skill, int level) { return getNeededXp(skill.minLevel(), level); }

    @Override
    public LevelXpFunction fallback() { return empty(); }

    @Override
    public LevelXpFunction load(JsonObject data)
    {
        this.polynomial = JsonHelper.readArraySerializable(data, "polynomial", Polynomial::tryLoad, polynomial);
        this.round = JsonHelper.readBoolean(data, "round", round);
        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();
        JsonHelper.write(data, "polynomial", polynomial);
        JsonHelper.write(data, "round", round);

        return data;
    }


}
