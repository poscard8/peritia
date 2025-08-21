package github.poscard8.peritia.registry;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.client.menu.PeritiaMainMenu;
import github.poscard8.peritia.client.menu.ProfileMenu;
import github.poscard8.peritia.client.menu.SkillMenu;
import github.poscard8.peritia.client.menu.SkillRecipeMenu;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PeritiaMenuTypes
{
    public static final DeferredRegister<MenuType<?>> ALL = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Peritia.ID);

    public static RegistryObject<MenuType<PeritiaMainMenu>> MAIN = ALL.register("main", () -> new MenuType<>(PeritiaMainMenu::new, FeatureFlagSet.of()));
    public static RegistryObject<MenuType<SkillMenu>> SKILL = ALL.register("skill", () -> new MenuType<>(SkillMenu::new, FeatureFlagSet.of()));
    public static RegistryObject<MenuType<SkillRecipeMenu>> SKILL_RECIPE = ALL.register("skill_recipe", () -> new MenuType<>(SkillRecipeMenu::new, FeatureFlagSet.of()));
    public static RegistryObject<MenuType<ProfileMenu>> PROFILE = ALL.register("profile", () -> new MenuType<>(ProfileMenu::new, FeatureFlagSet.of()));

    public static void register(IEventBus bus) { ALL.register(bus); }


}
