package github.poscard8.peritia.event.mod;

import github.poscard8.peritia.ascension.AscensionSystem;
import github.poscard8.peritia.ascension.AscensionSystemModifier;
import github.poscard8.peritia.reward.Reward;
import github.poscard8.peritia.skill.SkillAttributeInstance;
import github.poscard8.peritia.skill.SkillAttributes;
import github.poscard8.peritia.skill.SkillRewards;
import github.poscard8.peritia.util.skill.LevelXpFunction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AscensionSystemConfigureEvent extends Event implements IModBusEvent, AscensionSystemModifier
{
    protected Item icon = AscensionSystem.getDefaultIcon();
    protected SoundEvent sound = AscensionSystem.getDefaultSound();
    protected SimpleParticleType particleType = AscensionSystem.getDefaultParticleType();
    protected int particleCount = AscensionSystem.getDefaultParticleCount();
    protected LevelXpFunction xpFunction = AscensionSystem.getDefaultXpFunction();
    protected SkillAttributes attributes = AscensionSystem.getDefaultAttributes().copy();
    protected SkillRewards rewards = AscensionSystem.getDefaultRewards().copy();

    public AscensionSystemConfigureEvent() {}

    public Item icon() { return icon; }

    public void setIcon(Item icon) { this.icon = icon; }

    public SoundEvent sound() { return sound; }

    public void setSound(SoundEvent sound) { this.sound = sound; }

    public SimpleParticleType particleType() { return particleType; }

    public void setParticleType(SimpleParticleType particleType) { this.particleType = particleType; }

    public int particleCount() { return particleCount; }

    public void setParticleCount(int particleCount) { this.particleCount = particleCount; }

    public LevelXpFunction xpFunction() { return xpFunction; }

    public void setXpFunction(LevelXpFunction xpFunction) { this.xpFunction = xpFunction; }

    public SkillAttributes attributes() { return attributes; }

    public void setAttributes(SkillAttributes attributes) { this.attributes = attributes; }

    public void addAttribute(SkillAttributeInstance attributeInstance) { attributes.attributes().add(attributeInstance); }

    public SkillRewards rewards() { return rewards; }

    public void setRewards(SkillRewards rewards) { this.rewards = rewards; }

    public void addReward(Reward reward) { rewards.rewards().add(reward); }

    @Override
    public boolean isCancelable() { return false; }

}
