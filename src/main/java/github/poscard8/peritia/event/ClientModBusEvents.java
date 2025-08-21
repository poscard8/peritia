package github.poscard8.peritia.event;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.client.PeritiaKeyMappings;
import github.poscard8.peritia.client.particle.PeritiaParticle;
import github.poscard8.peritia.client.screen.PeritiaMainScreen;
import github.poscard8.peritia.client.screen.ProfileScreen;
import github.poscard8.peritia.client.screen.SkillRecipeScreen;
import github.poscard8.peritia.client.screen.SkillScreen;
import github.poscard8.peritia.registry.PeritiaMenuTypes;
import github.poscard8.peritia.registry.PeritiaParticleTypes;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.util.gui.widget.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Peritia.ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
@SuppressWarnings("unused")
public class ClientModBusEvents
{
    @SubscribeEvent
    static void registerKeyMappings(RegisterKeyMappingsEvent event)
    {
        event.register(PeritiaKeyMappings.MAIN_MENU);
    }

    @SubscribeEvent
    static void registerParticleProviders(RegisterParticleProvidersEvent event)
    {
        event.registerSpriteSet(PeritiaParticleTypes.LEVEL_UP.get(), PeritiaParticle.LevelUpProvider::new);
        event.registerSpriteSet(PeritiaParticleTypes.ASCENSION.get(), PeritiaParticle.AscensionProvider::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event)
    {
        MenuScreens.register(PeritiaMenuTypes.MAIN.get(), PeritiaMainScreen::new);
        MenuScreens.register(PeritiaMenuTypes.SKILL.get(), SkillScreen::new);
        MenuScreens.register(PeritiaMenuTypes.SKILL_RECIPE.get(), SkillRecipeScreen::new);
        MenuScreens.register(PeritiaMenuTypes.PROFILE.get(), ProfileScreen::new);

        XpSourceWidget.registerForType(PeritiaXpSourceTypes.LOGIN.get(), LoginXpSourceWidget::new);
        XpSourceWidget.registerForType(PeritiaXpSourceTypes.SOCIAL.get(), SocialXpSourceWidget::new);
        XpSourceWidget.registerForType(PeritiaXpSourceTypes.BLOCK.get(), BlockXpSourceWidget::new);
        XpSourceWidget.registerForType(PeritiaXpSourceTypes.ENTITY.get(), EntityXpSourceWidget::new);
        XpSourceWidget.registerForType(PeritiaXpSourceTypes.STRUCTURE.get(), StructureXpSourceWidget::new);
        XpSourceWidget.registerForType(PeritiaXpSourceTypes.CHEST.get(), ChestXpSourceWidget::new);
        XpSourceWidget.registerForType(PeritiaXpSourceTypes.RECIPE.get(), RecipeXpSourceWidget::new);
        XpSourceWidget.registerForType(PeritiaXpSourceTypes.TRADE.get(), TradeXpSourceWidget::new);
        XpSourceWidget.registerForType(PeritiaXpSourceTypes.ADVANCEMENT.get(), AdvancementXpSourceWidget::new);
        XpSourceWidget.registerForType(PeritiaXpSourceTypes.ENCHANTMENT.get(), EnchantmentXpSourceWidget::new);
        XpSourceWidget.registerForType(PeritiaXpSourceTypes.POTION.get(), ItemXpSourceWidget::potion);
        XpSourceWidget.registerForType(PeritiaXpSourceTypes.CONSUME.get(), ItemXpSourceWidget::consume);
        XpSourceWidget.registerForType(PeritiaXpSourceTypes.FISH.get(), ItemXpSourceWidget::fish);
        XpSourceWidget.registerForType(PeritiaXpSourceTypes.GRINDSTONE.get(), ItemXpSourceWidget::grindstone);
        XpSourceWidget.registerForType(PeritiaXpSourceTypes.ANVIL.get(), AnvilXpSourceWidget::new);
    }

}
