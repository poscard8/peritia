package github.poscard8.peritia.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class PeritiaParticle extends TextureSheetParticle
{
    public final SpriteSet spriteSet;

    public PeritiaParticle(ClientLevel clientLevel, double x, double y, double z, double xd, double yd, double zd, SpriteSet spriteSet)
    {
        super(clientLevel, x, y, z, xd, yd, zd);
        this.spriteSet = spriteSet;
        this.friction = 0.96F;
        this.quadSize *= 0.9F;
        this.hasPhysics = false;
        setSpriteFromAge(spriteSet);
    }

    @Override
    @NotNull
    public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }

    @Override
    protected int getLightColor(float v) { return 15 << 20 | super.getLightColor(v); }

    @Override
    public void tick()
    {
        super.tick();
        setSpriteFromAge(spriteSet);
    }

    @OnlyIn(Dist.CLIENT)
    public static class LevelUpProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet spriteSet;

        public LevelUpProvider(SpriteSet spriteSet) { this.spriteSet = spriteSet; }

        @Override
        @Nullable
        public PeritiaParticle createParticle(SimpleParticleType particleType, ClientLevel clientLevel, double x, double y, double z, double xd, double yd, double zd)
        {
            PeritiaParticle particle = new PeritiaParticle(clientLevel, x, y, z, xd, yd, zd, spriteSet);
            particle.setParticleSpeed(xd * 0.6D, yd * 0.6D, zd * 0.6D);
            particle.setLifetime(getLifetime());

            return particle;
        }

        public int getLifetime() { return 5 + new Random().nextInt(10); }

    }

    @OnlyIn(Dist.CLIENT)
    public static class AscensionProvider implements ParticleProvider<SimpleParticleType>
    {
        protected final SpriteSet spriteSet;

        public AscensionProvider(SpriteSet spriteSet) { this.spriteSet = spriteSet; }

        @Override
        @Nullable
        public PeritiaParticle createParticle(SimpleParticleType particleType, ClientLevel clientLevel, double x, double y, double z, double xd, double yd, double zd)
        {
            PeritiaParticle particle = new PeritiaParticle(clientLevel, x, y, z, xd, yd, zd, spriteSet);
            particle.setParticleSpeed(xd * 0.4D, yd * 0.4D, zd * 0.4D);
            particle.setLifetime(getLifetime());

            float brightness = getBrightness();
            particle.setColor(brightness, brightness, brightness);

            return particle;
        }

        public int getLifetime() { return 20 + new Random().nextInt(20); }

        public float getBrightness()
        {
            float f = new Random().nextFloat();
            return (float) (f >= 0.5F ? 1 - Math.pow(1 - f, 2) : Math.pow(f, 2));
        }

    }

}
