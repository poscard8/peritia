package github.poscard8.peritia.config;

import github.poscard8.peritia.command.CommandPredicate;
import net.minecraftforge.common.ForgeConfigSpec;

public class PeritiaServerConfig
{
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER;

    public static final ForgeConfigSpec.DoubleValue UNIVERSAL_XP_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue ENABLE_ASCENSIONS;
    public static final ForgeConfigSpec.BooleanValue KEEP_SKILL_RECIPES;
    public static final ForgeConfigSpec.BooleanValue KEEP_UNLOCKED_ITEMS;
    public static final ForgeConfigSpec.EnumValue<CommandPredicate> HELP_COMMAND;
    public static final ForgeConfigSpec.EnumValue<CommandPredicate> VIEW_COMMAND;

    static
    {
        BUILDER = new ForgeConfigSpec.Builder();

        UNIVERSAL_XP_MULTIPLIER = BUILDER
                .comment("XP multiplier for all skills")
                .defineInRange("universalXpMultiplier", 1.0D, 0.0D, 100.0D);

        ENABLE_ASCENSIONS = BUILDER.define("enableAscensions", true);

        KEEP_SKILL_RECIPES = BUILDER
                .comment("The ability to keep unlocked skill recipes after ascensions")
                .define("keepSkillRecipes", true);

        KEEP_UNLOCKED_ITEMS = BUILDER
                .comment("Some skills may lock certain items behind certain levels")
                .comment("The ability to use unlocked items after ascensions")
                .define("keepUnlockedItems", true);

        HELP_COMMAND = BUILDER
                .comment("Help command is '/peritia help' and it explains how the mod works")
                .comment("If enabled, any player can use it")
                .comment("If disabled, it is considered a cheat, meaning that only the operators can use it")
                .comment("'ESSENTIAL_MOD_ONLY' makes it enabled ONLY IF the Essential mod is loaded")
                .defineEnum("helpCommand", CommandPredicate.ENABLED);

        VIEW_COMMAND = BUILDER
                .comment("View command is '/peritia view' and it allows players to view other players' skills")
                .comment("If enabled, any player can use it")
                .comment("If disabled, it is considered a cheat, meaning that only the operators can use it")
                .comment("'ESSENTIAL_MOD_ONLY' makes it enabled ONLY IF the Essential mod is loaded")
                .defineEnum("viewCommand", CommandPredicate.ESSENTIAL_MOD_ONLY);

        SPEC = BUILDER.build();
    }


}
