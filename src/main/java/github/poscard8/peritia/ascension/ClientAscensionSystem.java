package github.poscard8.peritia.ascension;

import com.google.gson.JsonObject;
import github.poscard8.peritia.network.ClientHandler;
import github.poscard8.peritia.skill.SkillAttributes;
import github.poscard8.peritia.skill.SkillRewards;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.skill.LevelXpFunction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.ForgeRegistries;

public final class ClientAscensionSystem extends AscensionSystem
{
    ClientAscensionSystem(JsonObject data) { load(data); }

    public static ClientAscensionSystem getInstance() { return ClientHandler.getAscensionSystem(); }

    public static ClientAscensionSystem empty() { return new ClientAscensionSystem(new JsonObject()); }

    public static ClientAscensionSystem tryLoad(JsonObject data) { return empty().load(data); }

    public ClientAscensionSystem load(JsonObject data)
    {
        ParticleType<?> particleType2 = JsonHelper.readRegistrable(data, "particleType", ForgeRegistries.PARTICLE_TYPES, particleType);
        SimpleParticleType particleType3 = particleType2 instanceof SimpleParticleType simpleParticleType ? simpleParticleType : particleType;

        this.icon = JsonHelper.readRegistrable(data, "icon", ForgeRegistries.ITEMS, icon);
        this.sound = JsonHelper.readRegistrable(data, "sound", ForgeRegistries.SOUND_EVENTS, sound);
        this.particleType = particleType3;
        this.particleCount = JsonHelper.readInt(data, "particleCount", particleCount);
        this.xpFunction = JsonHelper.readJsonSerializable(data, "xpFunction", LevelXpFunction::tryLoad, xpFunction);
        this.attributes = JsonHelper.readArraySerializable(data, "attributes", SkillAttributes::tryLoad, attributes);
        this.rewards = JsonHelper.readArraySerializable(data, "rewards", SkillRewards::tryLoad, rewards);

        return this;
    }

}
