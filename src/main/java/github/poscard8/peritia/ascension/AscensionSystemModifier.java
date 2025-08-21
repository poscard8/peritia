package github.poscard8.peritia.ascension;

import github.poscard8.peritia.skill.SkillAttributes;
import github.poscard8.peritia.skill.SkillRewards;
import github.poscard8.peritia.util.skill.LevelXpFunction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;

public interface AscensionSystemModifier
{
    Item icon();

    SoundEvent sound();

    SimpleParticleType particleType();

    int particleCount();

    LevelXpFunction xpFunction();

    SkillAttributes attributes();

    SkillRewards rewards();

    default void apply()
    {
        AscensionSystem.setDefaultIcon(icon());
        AscensionSystem.setDefaultSound(sound());
        AscensionSystem.setDefaultParticleType(particleType());
        AscensionSystem.setDefaultParticleCount(particleCount());
        AscensionSystem.setDefaultXpFunction(xpFunction());
        AscensionSystem.setDefaultAttributes(attributes());
        AscensionSystem.setDefaultRewards(rewards());
    }
    
}
