package github.poscard8.peritia.util.skill;

public interface AtFunctionHolder
{
    AtFunction at();

    default boolean isAvailableFor(int level) { return at().test(level); }

}
