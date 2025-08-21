package github.poscard8.peritia.util.skill;

import org.jetbrains.annotations.NotNull;

public interface ComparableWeightHolder extends WeightHolder, Comparable<ComparableWeightHolder>
{
    @Override
    default int compareTo(@NotNull ComparableWeightHolder other)
    {
        return Double.compare(weight(), other.weight());
    }

}
