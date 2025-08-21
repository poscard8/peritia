package github.poscard8.peritia.reward;

import github.poscard8.peritia.skill.Skill;
import net.minecraft.network.chat.Component;

public interface RewardLike<T extends RewardLike<T>>
{
    T multiplyBy(int multiplier);

    boolean shouldDisplayText(Skill skill, int level);

    Component getText(Skill skill, int level);

    int priority();

    default boolean hasText() { return true; }

    default Component tryGetText() { return getText(Skill.empty(), 0); }

}
