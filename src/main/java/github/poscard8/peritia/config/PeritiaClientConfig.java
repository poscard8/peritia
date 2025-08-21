package github.poscard8.peritia.config;

import github.poscard8.peritia.util.gui.TextureStyle;
import github.poscard8.peritia.util.text.NumberType;
import github.poscard8.peritia.util.text.ProgressTextType;
import net.minecraftforge.common.ForgeConfigSpec;

public class PeritiaClientConfig
{
    public static final ForgeConfigSpec SPEC;
    static final ForgeConfigSpec.Builder BUILDER;

    public static final ForgeConfigSpec.EnumValue<TextureStyle> UI_TEXTURE_STYLE;
    public static final ForgeConfigSpec.BooleanValue UI_FADE_ANIMATION;
    public static final ForgeConfigSpec.BooleanValue CUSTOM_SKILL_MENUS;

    public static final ForgeConfigSpec.BooleanValue XP_GAIN_SOUND;
    public static final ForgeConfigSpec.BooleanValue LEVEL_UP_SOUND;
    public static final ForgeConfigSpec.BooleanValue ASCENSION_SOUND;

    public static final ForgeConfigSpec.EnumValue<NumberType> NUMBER_TYPE;
    public static final ForgeConfigSpec.EnumValue<ProgressTextType> PROGRESS_TEXT_TYPE;
    public static final ForgeConfigSpec.IntValue PROGRESS_TEXT_THRESHOLD;
    public static final ForgeConfigSpec.BooleanValue LEVEL_UP_TEXT;
    public static final ForgeConfigSpec.BooleanValue ASCENSION_TEXT;
    public static final ForgeConfigSpec.BooleanValue ENCYCLOPEDIA_TEXT;
    public static final ForgeConfigSpec.BooleanValue SPLASH_TEXT;

    public static final ForgeConfigSpec.BooleanValue LEVEL_UP_TOAST;
    public static final ForgeConfigSpec.BooleanValue ASCENSION_TOAST;
    public static final ForgeConfigSpec.BooleanValue CHEST_LUCK_TOAST;
    public static final ForgeConfigSpec.BooleanValue ENCYCLOPEDIA_TOAST;

    public static final ForgeConfigSpec.BooleanValue LEVEL_UP_PARTICLE;
    public static final ForgeConfigSpec.BooleanValue ASCENSION_PARTICLE;

    static
    {
        BUILDER = new ForgeConfigSpec.Builder();

        BUILDER.push("User Interface (UI)");

        UI_TEXTURE_STYLE = BUILDER
                .comment("Texture style of the mod menus")
                .comment("'VANILLA' is similar to inventory menu")
                .defineEnum("uiTextureStyle", TextureStyle.MODDED);

        UI_FADE_ANIMATION = BUILDER
                .comment("Fade animation for mod menus")
                .comment("Turn off for a more vanilla feel")
                .define("uiFadeAnimation", true);

        CUSTOM_SKILL_MENUS = BUILDER
                .comment("Custom menu & milestone textures for certain skills")
                .define("customSkillMenus", true);

        BUILDER.pop();

        BUILDER.push("Sounds");

        XP_GAIN_SOUND = BUILDER
                .comment("Sound played when the player gains xp on a skill")
                .comment("NOTE: This sound will only be played if there is a text displayed alongside")
                .define("xpGainSound", true);

        LEVEL_UP_SOUND = BUILDER
                .comment("Sound played when the player levels up a skill")
                .define("levelUpSound", true);

        ASCENSION_SOUND = BUILDER
                .comment("Sound played when the player ascends")
                .comment("This sound can be changed at <world save file>/serverconfig/peritia-ascensionSystem.json")
                .define("ascensionSound", true);

        BUILDER.pop();

        BUILDER.push("Texts");

        NUMBER_TYPE = BUILDER
                .comment("The way skill levels are displayed")
                .comment("Example: 'Mining XV' or 'Mining 15'")
                .defineEnum("numberType", NumberType.ROMAN);

        PROGRESS_TEXT_TYPE = BUILDER
                .comment("The way skill progress is displayed when the player gains XP")
                .comment("Example: '+10 Mining (80/200)' or '+10 Mining (40%)'")
                .defineEnum("progressTextType", ProgressTextType.NUMERIC);

        PROGRESS_TEXT_THRESHOLD = BUILDER
                .comment("The minimum XP threshold to show the progress texts on the screen")
                .comment("Example: If the value is 5 and the player gains 4 XP, no text will be displayed")
                .comment("If the player gains 5 or more XP, a texts will be displayed")
                .comment("Passing -1 completely disables progress texts")
                .defineInRange("progressTextThreshold", 1, -1, 10000);

        LEVEL_UP_TEXT = BUILDER
                .comment("Text displayed when the player levels up a skill")
                .define("levelUpText", true);

        ASCENSION_TEXT = BUILDER
                .comment("Text displayed when the player ascends")
                .define("ascensionText", true);

        ENCYCLOPEDIA_TEXT = BUILDER
                .comment("Text displayed (briefly) when the player discovers a new way to gain skill XP")
                .define("encyclopediaText", true);

        SPLASH_TEXT = BUILDER
                .comment("Splash texts are just like Minecraft main screen texts, but are written by the mod developer")
                .comment("They are displayed whenever the player maxes out a skill or ascends (without cheats)")
                .define("splashText", true);

        BUILDER.pop();

        BUILDER.push("Toasts");

        LEVEL_UP_TOAST = BUILDER
                .comment("Toast displayed (in the top-right corner) when the player levels up a skill")
                .define("levelUpToast", true);

        ASCENSION_TOAST = BUILDER
                .comment("Toast displayed when the player ascends")
                .define("ascensionToast", true);

        CHEST_LUCK_TOAST = BUILDER
                .comment("Toast displayed when the player gets additional loot from a chest")
                .define("chestLuckToast", false);

        ENCYCLOPEDIA_TOAST = BUILDER
                .comment("Toast displayed when the player completes a skill's encyclopedia")
                .define("encyclopediaToast", true);

        BUILDER.pop();

        BUILDER.push("Particles");

        LEVEL_UP_PARTICLE = BUILDER
                .comment("Particles displayed when a player levels up a skill")
                .define("levelUpParticle", true);

        ASCENSION_PARTICLE = BUILDER
                .comment("Particles displayed when a player ascends")
                .define("ascensionParticle", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

}
