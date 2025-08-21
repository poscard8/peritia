package github.poscard8.peritia;

import com.mojang.logging.LogUtils;
import github.poscard8.peritia.advancement.PeritiaAdvancementTriggers;
import github.poscard8.peritia.config.PeritiaClientConfig;
import github.poscard8.peritia.config.PeritiaServerConfig;
import github.poscard8.peritia.handler.SkillHandler;
import github.poscard8.peritia.handler.SkillRecipeHandler;
import github.poscard8.peritia.handler.XpSourceHandler;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.registry.*;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.recipe.SkillRecipe;
import github.poscard8.peritia.util.serialization.PeritiaResourceLoader;
import github.poscard8.peritia.util.text.ColorGradients;
import github.poscard8.peritia.util.xpsource.GearModifiers;
import github.poscard8.peritia.xpsource.XpSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import org.slf4j.Logger;

import java.util.List;

@Mod(Peritia.ID)
public class Peritia
{
    public static final String ID = "peritia";
    public static final LevelResource WORLD_DIRECTORY = new LevelResource(ID);
    public static final LevelResource CONFIG_WORLD_DIRECTORY = new LevelResource("serverconfig");
    public static final Logger LOGGER = LogUtils.getLogger();

    static final PeritiaResourceLoader RESOURCE_LOADER = new PeritiaResourceLoader();

    public static PeritiaResourceLoader resourceLoader() { return RESOURCE_LOADER; }

    public static SkillHandler skillHandler() { return resourceLoader().skillHandler; }

    public static SkillRecipeHandler skillRecipeHandler() { return resourceLoader().skillRecipeHandler; }

    public static XpSourceHandler xpSourceHandler() { return resourceLoader().xpSourceHandler; }

    public static List<Skill> skills() { return skillHandler().values(); }

    public static List<SkillRecipe> skillRecipes() { return skillRecipeHandler().values(); }

    public static List<XpSource> xpSources() { return xpSourceHandler().values(); }

    public static ResourceLocation asResource(String path) { return new ResourceLocation(ID, path); }

    public Peritia(FMLJavaModLoadingContext context)
    {
        FMLModContainer container = context.getContainer();
        container.addConfig(new ModConfig(ModConfig.Type.CLIENT, PeritiaClientConfig.SPEC, container, "peritia-client.toml"));
        container.addConfig(new ModConfig(ModConfig.Type.SERVER, PeritiaServerConfig.SPEC, container, "peritia-server.toml"));

        IEventBus bus = context.getModEventBus();

        PeritiaRewardTypes.register(bus);
        PeritiaXpSourceTypes.register(bus);
        PeritiaAttributes.register(bus);
        PeritiaMenuTypes.register(bus);
        PeritiaArgumentTypes.register(bus);
        PeritiaSoundEvents.register(bus);
        PeritiaParticleTypes.register(bus);

        PeritiaNetworkHandler.registerPackets();
        PeritiaAdvancementTriggers.register();
        GearModifiers.register();
        ColorGradients.register();
    }

}
