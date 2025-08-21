package github.poscard8.peritia.util.text;

import github.poscard8.peritia.config.PeritiaClientConfig;
import github.poscard8.peritia.network.ClientHandler;
import github.poscard8.peritia.registry.PeritiaAttributes;
import github.poscard8.peritia.reward.ItemReward;
import github.poscard8.peritia.reward.RewardLike;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.SkillAttributeInstance;
import github.poscard8.peritia.skill.SkillInstance;
import github.poscard8.peritia.skill.data.SkillData;
import github.poscard8.peritia.util.PeritiaHelper;
import github.poscard8.peritia.util.minecraft.LookContext;
import github.poscard8.peritia.util.serialization.SerializableDate;
import github.poscard8.peritia.util.skill.*;
import github.poscard8.peritia.util.xpsource.PeritiaJadeHelper;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.type.BlockXpSource;
import github.poscard8.peritia.xpsource.type.ChestXpSource;
import github.poscard8.peritia.xpsource.type.EnchantmentXpSource;
import github.poscard8.peritia.xpsource.type.EntityXpSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.*;

@SuppressWarnings("unused")
public class PeritiaTexts
{
    static final NumberFormat NUMBER_FORMAT = new DecimalFormat("#,###.##");

    static String format(Number number) { return NUMBER_FORMAT.format(number); }

    public static MutableComponent makeText(Number number, ChatFormatting... formatting) { return Component.literal(format(number)).withStyle(formatting); }

    public static MutableComponent makePlus(Number number, ChatFormatting... formatting) { return number.doubleValue() >= 0 ? Component.literal("+").withStyle(formatting).append(makeText(number, formatting)) : makeText(number, formatting); }

    public static MutableComponent makePercentage(Number number, ChatFormatting... formatting) { return Component.translatable("generic.peritia.percentage", makeText(number, formatting)).withStyle(formatting); }

    public static MutableComponent makePerHour(Number number, ChatFormatting... formatting) { return Component.translatable("generic.peritia.per_hour", makeText(number, formatting)).withStyle(formatting); }

    public static MutableComponent empty() { return Component.empty(); }

    public static MutableComponent space() { return Component.literal(" "); }

    public static MutableComponent doubleSpace() { return Component.literal("  "); }

    public static MutableComponent indent() { return Component.literal("    "); }

    public static MutableComponent newLine() { return CommonComponents.NEW_LINE.copy(); }

    public static Component modName() { return Component.translatable("generic.peritia.name"); }

    public static Component level() { return Component.translatable("generic.peritia.level"); }

    public static Component levelUp() { return Component.translatable("generic.peritia.level_up"); }

    public static Component levelUpReady() { return Component.translatable("generic.peritia.level_up_ready"); }

    public static Component levelUpPopup() { return Component.translatable("popup.peritia.level_up"); }

    public static Component levelUpReadyPopup() { return Component.translatable("popup.peritia.level_up_ready"); }

    public static Component ready() { return Component.translatable("popup.peritia.ready").withStyle(ChatFormatting.AQUA); }

    public static Component loginReady() { return Component.translatable("popup.peritia.login_ready").withStyle(ChatFormatting.AQUA); }

    public static Component chestLuckPopup() { return Component.translatable("popup.peritia.chest_luck").withStyle(ColorGradients.NOTIFY); }

    public static Component magentaExclamationMark() { return Component.literal("!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD); }

    public static Component redExclamationMark() { return Component.literal("!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD); }

    public static Component turquoiseExclamationMark() { return Component.literal("!").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD); }

    public static Component glowExclamationMark() { return Component.literal("!").withStyle(ColorGradients.NOTIFY).withStyle(ChatFormatting.BOLD); }

    public static Component skillCrafting() { return Component.translatable("generic.peritia.skill_crafting"); }

    public static Component attributeBuffs() { return Component.translatable("label.peritia.attribute_buffs").withStyle(ChatFormatting.GRAY); }

    public static Component reward() { return Component.translatable("label.peritia.reward").withStyle(ChatFormatting.GRAY); }

    public static Component rewards() { return Component.translatable("label.peritia.rewards").withStyle(ChatFormatting.GRAY); }

    public static Component currentReward() { return Component.translatable("label.peritia.current_reward").withStyle(ChatFormatting.GRAY); }

    public static Component currentRewards() { return Component.translatable("label.peritia.current_rewards").withStyle(ChatFormatting.GRAY); }

    public static Component claimRewards() { return Component.translatable("generic.peritia.claim_rewards").withStyle(ChatFormatting.BLUE); }

    public static Component payRestrictions() { return Component.translatable("generic.peritia.pay_restrictions").withStyle(ChatFormatting.BLUE); }

    public static Component locked() { return Component.translatable("generic.peritia.locked").withStyle(ChatFormatting.RED); }

    public static Component unlocked() { return Component.translatable("generic.peritia.unlocked").withStyle(ChatFormatting.GREEN); }

    public static Component pending() { return Component.translatable("generic.peritia.pending").withStyle(ChatFormatting.RED); }

    public static Component back() { return Component.translatable("generic.peritia.back").withStyle(ChatFormatting.BLUE); }

    public static Component previousPage() { return Component.translatable("generic.peritia.previous_page").withStyle(ChatFormatting.BLUE); }

    public static Component nextPage() { return Component.translatable("generic.peritia.next_page").withStyle(ChatFormatting.BLUE); }

    public static Component previousSkill() { return Component.translatable("generic.peritia.previous_skill").withStyle(ChatFormatting.BLUE); }

    public static Component nextSkill() { return Component.translatable("generic.peritia.next_skill").withStyle(ChatFormatting.BLUE); }

    public static Component claim() { return Component.translatable("generic.peritia.claim"); }

    public static Component claimAll() { return Component.translatable("generic.peritia.claim_all").withStyle(ChatFormatting.BLUE); }

    public static Component required() { return Component.translatable("label.peritia.required").withStyle(ChatFormatting.GRAY); }

    public static Component timeUntilNextXp() { return Component.translatable("label.peritia.time_until_next_xp").withStyle(ChatFormatting.GRAY); }

    public static Component onlinePlayers() { return Component.translatable("label.peritia.online_players").withStyle(ChatFormatting.GRAY); }

    public static Component notEnoughPlayers() { return Component.translatable("generic.peritia.not_enough_players").withStyle(ChatFormatting.RED); }

    public static Component waitingForPlayers() { return Component.translatable("generic.peritia.waiting_for_players").withStyle(ChatFormatting.WHITE); }

    public static Component ascensionSuccessToast() { return Component.translatable("generic.peritia.ascension_success"); }

    public static Component mayGiveXp() { return Component.translatable("generic.peritia.may_give_xp").withStyle(ChatFormatting.GRAY); }

    public static Component encyclopediaUpdated() { return Component.translatable("generic.peritia.encyclopedia_updated").withStyle(style -> style.withItalic(false).withColor(ChatFormatting.LIGHT_PURPLE)); }

    public static Component encyclopediaComplete() { return Component.translatable("generic.peritia.encyclopedia_complete").withStyle(ChatFormatting.GOLD); }

    public static Component validIngredients() { return Component.translatable("label.peritia.valid_ingredients").withStyle(ChatFormatting.DARK_GREEN); }

    public static Component validProfessions() { return Component.translatable("label.peritia.valid_professions").withStyle(ChatFormatting.DARK_GREEN); }

    public static Component leftSlot() { return Component.translatable("label.peritia.left_slot").withStyle(ChatFormatting.DARK_GREEN); }

    public static Component rightSlot() { return Component.translatable("label.peritia.right_slot").withStyle(ChatFormatting.DARK_GREEN); }

    public static Component anyAdvancementFrom() { return Component.translatable("label.peritia.any_advancement_from").withStyle(ChatFormatting.DARK_PURPLE); }

    public static Component anyRecipeFrom() { return Component.translatable("label.peritia.any_recipe_from").withStyle(ChatFormatting.DARK_PURPLE); }

    public static Component anyChestFrom() { return Component.translatable("label.peritia.any_chest_from").withStyle(ChatFormatting.DARK_PURPLE); }

    public static Component relatedTo() { return Component.translatable("label.peritia.related_to").withStyle(ChatFormatting.DARK_PURPLE); }

    public static Component ascensionTitle() { return Component.translatable("generic.peritia.ascension"); }

    public static Component skillsTitle() { return Component.translatable("generic.peritia.skills"); }

    public static Component skillRecipesTitle() { return Component.translatable("generic.peritia.skill_recipes"); }

    public static Component legacyTitle() { return Component.translatable("generic.peritia.legacy").withStyle(ChatFormatting.GOLD); }

    public static Component allSkills() { return Component.translatable("generic.peritia.all_skills"); }

    public static Component encyclopedia() { return Component.translatable("generic.peritia.encyclopedia"); }

    public static Component craftSingle() { return Component.translatable("generic.peritia.craft_single").withStyle(ChatFormatting.BLUE); }

    public static Component craftStack() { return Component.translatable("generic.peritia.craft_stack").withStyle(ChatFormatting.BLUE); }

    public static Component clickToView() { return Component.translatable("generic.peritia.click_to_view").withStyle(ChatFormatting.BLUE); }

    public static Component clickToAscend() { return Component.translatable("generic.peritia.click_to_ascend").withStyle(ChatFormatting.BLUE); }

    public static Component clickToConfirm() { return Component.translatable("generic.peritia.click_to_confirm").withStyle(ChatFormatting.BLUE); }

    public static Component ascensionsDisabled() { return Component.translatable("generic.peritia.ascensions_disabled").withStyle(ChatFormatting.RED); }

    public static Component incorrectTool() { return Component.translatable("generic.peritia.incorrect_tool").withStyle(ChatFormatting.RED); }

    public static Component holdShift() { return Component.translatable("generic.peritia.hold_shift").withStyle(ChatFormatting.BLUE); }

    public static MutableComponent highestSkill() { return Component.translatable("label.peritia.highest_skill").withStyle(ChatFormatting.GRAY); }

    public static MutableComponent avgSkillLevel() { return Component.translatable("label.peritia.avg_skill_level").withStyle(ChatFormatting.GRAY); }

    public static MutableComponent totalXp() { return Component.translatable("label.peritia.total_xp").withStyle(ChatFormatting.GRAY); }

    public static MutableComponent levelPrefix() { return Component.translatable("label.peritia.level").withStyle(ChatFormatting.GRAY); }

    public static MutableComponent legacyScore() { return Component.translatable("label.peritia.legacy_score").withStyle(ChatFormatting.GRAY); }

    public static MutableComponent yourLegacyScore() { return Component.translatable("label.peritia.your_legacy_score").withStyle(ChatFormatting.GRAY); }

    public static MutableComponent pendingLegacyScore() { return Component.translatable("label.peritia.pending_legacy_score").withStyle(ChatFormatting.GRAY); }

    public static MutableComponent ascendInfo() { return Component.translatable("label.peritia.ascend").withStyle(ChatFormatting.GRAY); }

    public static MutableComponent ascendNowInfo() { return Component.translatable("label.peritia.ascend_now").withStyle(ChatFormatting.GRAY); }

    public static MutableComponent xpGainRate() { return Component.translatable("label.peritia.xp_gain_rate").withStyle(ChatFormatting.GRAY); }

    public static MutableComponent xpMultiplier() { return Component.translatable("label.peritia.xp_multiplier").withStyle(ChatFormatting.GRAY); }

    public static Component xp() { return Component.translatable("generic.peritia.xp"); }

    public static Component noXp() { return Component.translatable("generic.peritia.no_xp").withStyle(ChatFormatting.RED); }

    public static Component anyItem() { return Component.translatable("generic.peritia.any_item").withStyle(ChatFormatting.GREEN); }

    public static Component anyBlock() { return Component.translatable("generic.peritia.any_block").withStyle(ChatFormatting.GREEN); }

    public static Component anyEntity() { return Component.translatable("generic.peritia.any_entity").withStyle(ChatFormatting.GREEN); }

    public static Component anyStructure() { return Component.translatable("generic.peritia.any_structure").withStyle(ChatFormatting.GREEN); }

    public static Component anyAdvancement() { return Component.translatable("generic.peritia.any_advancement").withStyle(ChatFormatting.GREEN); }

    public static Component anyRecipe() { return Component.translatable("generic.peritia.any_recipe").withStyle(ChatFormatting.GREEN); }

    public static Component anyChest() { return Component.translatable("generic.peritia.any_chest").withStyle(ChatFormatting.GREEN); }

    public static Component anyProfession() { return Component.translatable("generic.peritia.any_profession").withStyle(ChatFormatting.GREEN); }

    public static Component all() { return Component.translatable("generic.peritia.all"); }

    public static Component none() { return Component.translatable("generic.peritia.none"); }

    public static Component optional() { return Component.translatable("generic.peritia.optional"); }

    public static Component partially() { return Component.translatable("generic.peritia.partially"); }

    public static Component timePlayed() { return Component.translatable("label.peritia.time_played").withStyle(ChatFormatting.GOLD); }

    public static Component wisdomInfo() { return Component.translatable("generic.peritia.wisdom_info").withStyle(ChatFormatting.GRAY); }

    public static Component chestLuckInfo() { return Component.translatable("generic.peritia.chest_luck_info").withStyle(ChatFormatting.GRAY); }

    public static Component splash()
    {
        int index = new Random().nextInt(0, 35);
        MutableComponent text = Component.translatable("splash.peritia." + index).withStyle(ChatFormatting.YELLOW);

        switch (index)
        {
            case 10 ->
            {
                String id = "Li4j82QbBvk";
                Component idText = Component.literal(id).withStyle(ChatFormatting.WHITE).withStyle(style ->
                {
                    return style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, id))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click")));
                });
                return text.append(idText);
            }
            case 34 ->
            {
                return text.withStyle(ExtraTextColors.OLIVE);
            }
            default -> {}
        }
        return text;
    }

    public static Component search(ChatFormatting color) { return Component.translatable("generic.peritia.search").withStyle(color, ChatFormatting.ITALIC); }

    public static Component ascensionCount(int count)
    {
        if (count > 2) return Component.translatable("generic.peritia.ascend_multiple", makeText(count, ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY);
        String[] keywords = new String[]{"never", "once", "twice"};
        String keyword = keywords[count];
        String translationKey = "generic.peritia.ascend_" + keyword;
        return Component.translatable(translationKey).withStyle(ChatFormatting.GRAY);
    }

    public static Component ascensionCooldown(SerializableDate lastAscended)
    {
        SerializableDate nextDate = lastAscended.add(Calendar.MINUTE, 1);
        Component durationText = duration(nextDate.offsetFromNow(), empty(), ChatFormatting.RED);
        return Component.translatable("label.peritia.cannot_ascend").withStyle(ChatFormatting.RED).append(durationText);
    }

    public static Component ascensionRewardContext(ItemReward itemReward)
    {
        AtFunction atFunction = itemReward.at();

        if (atFunction instanceof AtFunction.Single single)
        {
            Component numberText = makeText(single.number(), ChatFormatting.LIGHT_PURPLE);
            return Component.translatable("generic.peritia.at_legacy.single", numberText).withStyle(ChatFormatting.GRAY);
        }
        else if (atFunction instanceof AtFunction.From from)
        {
            Component numberText = makeText(from.number(), ChatFormatting.LIGHT_PURPLE);
            return Component.translatable("generic.peritia.at_legacy.from", numberText).withStyle(ChatFormatting.GRAY);
        }
        else if (atFunction instanceof AtFunction.To to)
        {
            Component numberText = makeText(to.number(), ChatFormatting.LIGHT_PURPLE);
            return Component.translatable("generic.peritia.at_legacy.to", numberText).withStyle(ChatFormatting.GRAY);
        }
        else if (atFunction instanceof AtFunction.Range range)
        {
            Component numberText = makeText(range.from(), ChatFormatting.LIGHT_PURPLE).append("-");
            Component numberText2 = makeText(range.to(), ChatFormatting.LIGHT_PURPLE);
            return Component.translatable("generic.peritia.at_legacy.range", numberText, numberText2).withStyle(ChatFormatting.GRAY);
        }
        else if (atFunction instanceof AtFunction.List list)
        {
            MutableComponent numberText = empty();

            if (!list.numbers().isEmpty())
            {
                List<Integer> sorted = list.sortedNumbers();
                int lastIndex = sorted.size() - 1;

                for (int i = 0; i < lastIndex; i++)
                {
                    numberText.append(makeText(sorted.get(i), ChatFormatting.LIGHT_PURPLE));
                    numberText.append(Component.literal(", ").withStyle(ChatFormatting.LIGHT_PURPLE));
                }
                numberText.append(makeText(sorted.get(lastIndex), ChatFormatting.LIGHT_PURPLE));
            }
            return Component.translatable("generic.peritia.at_legacy.list", numberText).withStyle(ChatFormatting.GRAY);
        }
        else if (atFunction instanceof AtFunction.Periodic periodic)
        {
            int[] numbers = new int[4];

            int number = periodic.offset() > 0 ? periodic.offset() : periodic.offset() + periodic.period();
            for (int i = 0; i < 4; i++)
            {
                numbers[i] = number;
                number += periodic.period();
            }

            Component numberText = makeText(numbers[0], ChatFormatting.LIGHT_PURPLE);
            Component numberText2 = makeText(numbers[1], ChatFormatting.LIGHT_PURPLE);
            Component numberText3 = makeText(numbers[2], ChatFormatting.LIGHT_PURPLE);
            Component numberText4 = makeText(numbers[3], ChatFormatting.LIGHT_PURPLE);

            return Component.translatable("generic.peritia.at_legacy.periodic", numberText, numberText2, numberText3, numberText4).withStyle(ChatFormatting.GRAY);
        }
        return empty();
    }

    public static Component lootChance(double chance, int multiplier, ChatFormatting... formatting)
    {
        String[] keywords = new String[]{"multiple", "multiple", "double", "triple", "quadruple"};
        String keyword = multiplier >= 5 ? "multiple" : keywords[multiplier];
        String translationKey = String.format("generic.peritia.%s_loot", keyword);

        Component chanceText = makePercentage(chance, formatting);
        return Component.translatable(translationKey, chanceText).withStyle(ChatFormatting.GRAY);
    }

    public static Component modName(String modId) { return modName(modId, ChatFormatting.BLUE, ChatFormatting.ITALIC); }

    public static Component modName(String modId, ChatFormatting... formatting)
    {
        Optional<? extends ModContainer> optional = ModList.get().getModContainerById(modId);
        if (optional.isEmpty()) return Component.literal(modId).withStyle(formatting);

        String name = optional.get().getModInfo().getDisplayName();
        return Component.literal(name).withStyle(formatting);
    }

    public static Component ofPlayer(LookContext lookContext) { return Component.translatable("generic.peritia.of_player", lookContext.playerName()); }

    public static Component playerName(Player player, @Nullable LookContext lookContext)
    {
        MutableComponent name = lookContext == null ? player.getName().copy() : Component.literal(lookContext.playerName());
        return name.withStyle(ChatFormatting.GOLD);
    }

    public static Component playerMaxedOutSkill(Player player, Skill skill) { return Component.translatable("generic.peritia.player_maxed_out", player.getName(), skill(skill, ChatFormatting.GREEN)); }

    public static Component playerAscended(Player player, int legacyScore) { return Component.translatable("generic.peritia.player_ascended", player.getName(), legacyScore); }

    public static Component nLoot(int n, ChatFormatting... formatting) { return Component.translatable("generic.peritia.n_loot", format(n)).withStyle(formatting); }

    public static Component nMore(int n, ChatFormatting... formatting) { return Component.translatable("generic.peritia.n_more", format(n)).withStyle(formatting); }

    public static Component duration(long offset) { return duration(offset, loginReady(), ChatFormatting.WHITE); }

    public static Component duration(long offset, Component readyText, ChatFormatting... formatting)
    {
        if (offset <= 0) return readyText;

        Duration duration = Duration.ofMillis(offset);
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        MutableComponent text = empty().withStyle(formatting);

        if (days > 0)
        {
            text.append(Component.translatable("generic.peritia.duration.d", format(days)));
            text.append(space());
        }
        if (hours > 0)
        {
            text.append(Component.translatable("generic.peritia.duration.h", format(hours)));
            text.append(space());
        }
        if (minutes > 0)
        {
            text.append(Component.translatable("generic.peritia.duration.m", format(minutes)));
            text.append(space());
        }
        if (seconds > 0)
        {
            text.append(Component.translatable("generic.peritia.duration.s", format(seconds)));
            text.append(space());
        }

        text.getSiblings().remove(text.getSiblings().size() - 1); // last text is always a space
        return text;
    }

    public static Component skill(Skill skill, ChatFormatting... formatting) { return skill.plainName().copy().withStyle(formatting); }

    public static Component skill(SkillInstance instance, ChatFormatting... formatting) { return skill(instance.skill(), formatting); }

    public static Component level(int level, ChatFormatting... formatting)
    {
        NumberType numberType = PeritiaClientConfig.NUMBER_TYPE.get();
        return numberType == NumberType.ARABIC ? makeText(level, formatting) : RomanNumeral.of(level).asText().withStyle(formatting);
    }

    public static Component skillWithLevel(Skill skill, int level, ChatFormatting... formatting)
    {
        MutableComponent skillText = skill(skill, formatting).copy();
        return skillText.append(space()).append(level(level));
    }

    public static Component skillWithLevel(SkillInstance instance, ChatFormatting... formatting) { return skillWithLevel(instance.skill(), instance.level(), formatting); }

    public static Component requisite(SkillRequisite requisite, ChatFormatting... formatting)
    {
        if (requisite.type() == SkillRequisite.Type.SINGLE)
        {
            Component levelComponent = skillWithLevel(requisite.skill(), requisite.level(), formatting);
            return Component.translatable("generic.peritia.requisite", levelComponent).withStyle(formatting);
        }
        else
        {
            String translationKey = String.format("generic.peritia.%s_requisite", requisite.type().name().toLowerCase());
            Component levelComponent = level(requisite.level(), formatting);
            return Component.translatable(translationKey, levelComponent).withStyle(formatting);
        }
    }

    public static Component itemRequisite(SkillRequisite requisite, ChatFormatting... formatting)
    {
        Component levelComponent = skillWithLevel(requisite.skill(), requisite.level(), formatting);
        return Component.translatable("generic.peritia.item_requisite", levelComponent).withStyle(formatting);
    }

    public static Component skillLevelUpReady(Skill skill, ChatFormatting... formatting)
    {
        return Component.translatable("generic.peritia.skill_level_up_ready", skill(skill).getString()).withStyle(formatting);
    }

    public static Component skillLevelUpReady(SkillInstance instance, ChatFormatting... formatting) { return skillLevelUpReady(instance.skill(), formatting); }

    public static Component levelUpIndicator(Skill skill, int oldLevel, int newLevel, ChatFormatting... formatting)
    {
        MutableComponent name = skill.plainName().copy().withStyle(formatting);
        Component arrow = Component.literal(" -> ").withStyle(formatting);
        Component oldComponent = level(oldLevel, formatting);
        Component newComponent = level(newLevel, ChatFormatting.GREEN, ChatFormatting.BOLD);

        return name.append(space()).append(oldComponent).append(arrow).append(newComponent);
    }

    public static Component xpSourceTitle(Skill skill) { return skill(skill).copy().append(": ").append(encyclopedia()); }

    public static Component skillTitle(XpSource xpSource, @Nullable Skill menuSkill) { return xpSource.skillFunction().xpSourceText(menuSkill); }

    public static Component xpTitle(XpSource xpSource, float multiplier, ChatFormatting... formatting) { return xpTitle(Math.round(xpSource.xp() * multiplier), formatting); }

    public static Component xpTitle(int xp, ChatFormatting... formatting) { return Component.literal(format(xp)).withStyle(formatting).append(space()).append(xp()); }

    public static Component xpGain(XpGainContext context)
    {
        switch (context.size())
        {
            case 0 -> { return empty(); }
            case 1 -> { return xpGain(context, context.getComponent(0)); }
            case 2 ->
            {
                Component first = xpGain(context, context.getComponent(0));
                Component second = xpGain(context, context.getComponent(1));
                ChatFormatting columnColor = context.blocked() ? ChatFormatting.RED : context.forUI() ? ChatFormatting.AQUA : ChatFormatting.DARK_AQUA;

                return empty()
                        .append(first)
                        .append(Component.literal(", ").withStyle(columnColor))
                        .append(second);
            }
            default ->
            {
                int index = context.forUI() ? 0 : new Random().nextInt(context.size());
                ChatFormatting color = context.blocked() ? ChatFormatting.RED : ChatFormatting.AQUA;

                return empty()
                        .append(xpGain(context, context.getComponent(index)))
                        .append(nMore(context.size() - 1, color));
            }
        }
    }

    public static Component xpGain(XpGainContext context, XpGainContext.Component component)
    {
        SkillInstance instance = component.asSkillInstance();

        int existingXp = instance.xp();
        int neededXp = instance.xpNeededForLevelUp();
        boolean random = context.forUI() && context.random();

        ChatFormatting primaryColor = context.blocked() ? ChatFormatting.RED : ChatFormatting.AQUA;
        ChatFormatting secondaryColor = context.blocked() ? ChatFormatting.RED : ChatFormatting.DARK_AQUA;

        MutableComponent plusSign = makePlus(component.deltaXp(), primaryColor).append(space());
        Component skillName = random ?
                Component.literal("12345678").withStyle(Style.EMPTY.withObfuscated(true).withColor(primaryColor)) :
                skill(instance, primaryColor);

        if (instance.isMaxLevel() || context.forUI()) return plusSign.append(skillName);

        Component progressText = PeritiaClientConfig.PROGRESS_TEXT_TYPE.get().getTextWithParenthesis(existingXp, neededXp).withStyle(secondaryColor);
        return plusSign.append(skillName).append(space()).append(progressText);
    }

    public static Component rewardClaimer(Skill skill, int oldLevel, int newLevel)
    {
        String insertion = String.format("claimReward,%s,%d,%d", skill.stringKey(), oldLevel, newLevel);
        Style style = Style.EMPTY.withColor(ChatFormatting.BLUE).withInsertion(insertion);

        return Component.translatable("generic.peritia.claim_rewards").withStyle(style);
    }

    public static Component menuOpener(Skill skill)
    {
        String insertion = String.format("openSkillMenu,%s", skill.stringKey());
        Style style = Style.EMPTY.withColor(ChatFormatting.BLUE).withInsertion(insertion);

        return Component.translatable("generic.peritia.open_skill_menu").withStyle(style);
    }

    public static Component progressBarMilestone(SkillInstance instance, int level) { return progressBar(instance.xpForMilestone(level), instance.xpNeededForMilestone(level), 15); }

    public static Component progressBar(SkillInstance instance)  { return progressBar(instance.xp(), instance.xpNeededForLevelUp(), 25); }

    public static Component progressBar(int xp, int neededXp, int length) { return progressBar(xp, neededXp, length, PeritiaClientConfig.PROGRESS_TEXT_TYPE.get()); }

    public static Component progressBar(int xp, int neededXp, int length, ProgressTextType type)
    {
        float actualRatio = (float) xp / neededXp;
        float ratio = Math.min(1, actualRatio);

        int completeLines = Math.round(length * ratio);
        int incompleteLines = length - completeLines;

        MutableComponent bar = Component.literal(" ".repeat(completeLines)).withStyle(ChatFormatting.WHITE, ChatFormatting.STRIKETHROUGH)
                .append(Component.literal(" ".repeat(incompleteLines)).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.STRIKETHROUGH));
        Component progressText = type.getText(xp, neededXp);

        return bar.append(space().withStyle(Style.EMPTY.withStrikethrough(false).withColor(ChatFormatting.DARK_AQUA)).append(progressText));
    }

    public static Component pressR(String string) { return Component.translatable("generic.peritia.press_r", Component.literal(string).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.BLUE); }

    public static Component enchantmentXpInfo(Enchantment enchantment, int xp)
    {
        boolean hasLevels = enchantment.getMaxLevel() > 1;

        MutableComponent prefix = Component.translatable(enchantment.getDescriptionId()).withStyle(ChatFormatting.GRAY).append(": ");
        int multiplier = EnchantmentXpSource.evaluate(enchantment);

        Component xpText = xpTitle(xp * multiplier, ChatFormatting.DARK_AQUA);
        Component suffix = multiplier == 0 ? noXp() : hasLevels ? Component.translatable("generic.peritia.per_level", xpText).withStyle(Style.EMPTY.withColor(0x006F6F)) : xpText;
        return prefix.append(suffix);
    }

    public static Component structure(ResourceLocation structureKey)
    {
        String translationKey = String.format("structure.%s.%s", structureKey.getNamespace(), structureKey.getPath());
        return Component.translatable(translationKey).withStyle(ChatFormatting.GRAY);
    }

    public static Component advancement(ResourceLocation advancementKey) { return ClientHandler.getGameContext().advancementContext().nameOf(advancementKey); }

    public static Component profession(ResourceLocation professionKey) { return Component.translatable("entity.minecraft.villager." + professionKey.getPath()).withStyle(ChatFormatting.GRAY); }

    public static MutableComponent itemStack(ItemStack stack) { return itemStack(stack, ChatFormatting.RESET).withStyle(ExtraTextColors.SKY_BLUE); }

    public static MutableComponent itemStack(ItemStack stack, ChatFormatting... colors) {

        Component name = stack.getHoverName().copy().withStyle(colors);

        if (stack.getItem() instanceof EnchantedBookItem) {

            Map<Enchantment, Integer> enchantmentMap = PeritiaHelper.getEnchantmentMap(stack);

            if (enchantmentMap.size() == 1) {

                Enchantment enchantment = new ArrayList<>(enchantmentMap.keySet()).get(0);
                int level = enchantmentMap.getOrDefault(enchantment, 1);

                name = Component.translatable("generic.peritia.enchanted_book", enchantment.getFullname(level).copy().withStyle(colors)).withStyle(colors);
            }
        }

        return Component.translatable("generic.peritia.item_stack", stack.getCount(), name.getString()).withStyle(colors);
    }

    public static Component minecraftXp(int amount) { return minecraftXp(amount, ChatFormatting.GREEN); }

    public static Component minecraftXp(int amount, ChatFormatting... colors) {

        String string = format(amount) + " ";
        return Component.literal(string).withStyle(colors).append(Component.translatable("generic.peritia.experience").withStyle(colors));
    }

    public static MutableComponent attribute(SkillAttributeInstance attributeInstance) { return attribute(attributeInstance.attribute(), attributeInstance.value(), ChatFormatting.BLUE); }

    public static MutableComponent attribute(@Nullable Attribute attribute, double value, ChatFormatting... formatting)
    {
        if (attribute == null) return empty();

        Set<Attribute> percentageAttributes = Set.of(PeritiaAttributes.CRIT_DAMAGE.get(), PeritiaAttributes.EXTRA_CRIT_CHANCE.get(), PeritiaAttributes.BLOCK_BREAK_SPEED.get());
        double multiplied = attribute == PeritiaAttributes.BLOCK_BREAK_SPEED.get() ? 100 * value : value;

        String sign = multiplied >= 0 ? "+" : "";
        String string = percentageAttributes.contains(attribute) ? sign + makePercentage(multiplied).getString() : sign + format(multiplied);

        Component suffix = Component.translatable(attribute.getDescriptionId());
        return Component.literal(string).withStyle(formatting).append(space()).append(suffix);
    }

    public static List<Component> $levelUp(LevelUpContext context, boolean manually)
    {
        List<Component> texts = new ArrayList<>();
        context.forEachComponent(component -> texts.addAll($levelUpComponent(component, manually)));
        return texts;
    }

    public static List<Component> $levelUpComponent(LevelUpContext.Component component, boolean manually)
    {
        List<Component> texts = new ArrayList<>();

        Skill skill = component.skill();
        int oldLevel = component.oldLevel();
        int newLevel = component.newLevel();

        boolean hasSplash = PeritiaClientConfig.SPLASH_TEXT.get() && newLevel == skill.maxLevel() && manually;
        boolean hasRewards = false;

        MutableComponent popup = levelUpPopup().copy();
        Component indicator = levelUpIndicator(skill, oldLevel, newLevel, ChatFormatting.GRAY);

        for (int lvl = oldLevel + 1; lvl <= newLevel; lvl++)
        {
            if (!skill.rewardsAt(lvl).isEmpty())
            {
                hasRewards = true;
                break;
            }
        }

        Component text = popup.append(space()).append(indicator);
        texts.add(text);
        if (hasRewards) texts.add(rewardClaimer(skill, oldLevel, newLevel));
        if (hasSplash) texts.add(splash());

        return texts;
    }

    public static List<Component> $levelUpReady(Skill skill)
    {
        List<Component> texts = new ArrayList<>();

        Component text = levelUpReadyPopup().copy().append(space()).append(skill(skill, ChatFormatting.GRAY));
        texts.add(text);
        texts.add(menuOpener(skill));

        return texts;
    }

    public static List<Component> $requisitesForRecipe(SkillRequisites requisites, SkillData skillData) { return $requisitesForRecipe(requisites, skillData, ChatFormatting.RED); }

    public static List<Component> $requisitesForRecipe(SkillRequisites requisites, SkillData skillData, ChatFormatting... formatting)
    {
        List<Component> texts = new ArrayList<>();
        for (SkillRequisite requisite : requisites)
        {
            if (!requisite.testForRecipe(skillData)) texts.add(requisite(requisite, formatting));
        }
        return texts;
    }

    public static List<Component> $requisitesForRecipeJei(SkillRequisites requisites, SkillData skillData)
    {
        List<Component> texts = new ArrayList<>();
        for (SkillRequisite requisite : requisites)
        {
            ChatFormatting color = requisite.testForRecipe(skillData) ? ChatFormatting.GREEN : ChatFormatting.RED;
            texts.add(requisite(requisite, color));
        }
        return texts;
    }

    public static List<Component> $requisitesForLockedItem(SkillRequisites requisites, SkillData skillData, boolean warning) { return $requisitesForLockedItem(requisites, skillData, warning, ChatFormatting.RED); }

    public static List<Component> $requisitesForLockedItem(SkillRequisites requisites, SkillData skillData, boolean warning, ChatFormatting... formatting)
    {
        List<Component> texts = new ArrayList<>();
        for (SkillRequisite requisite : requisites)
        {
            if (!requisite.testForLockedItem(skillData))
            {
                Component text = warning ? itemRequisite(requisite, formatting) : requisite(requisite, formatting);
                texts.add(text);
            }
        }
        return texts;
    }

    public static List<Component> $generalStats(SkillData skillData)
    {
        List<Component> texts = new ArrayList<>();

        Skill skill = skillData.highestSkill();
        texts.add(highestSkill().append(skill(skill, ChatFormatting.DARK_AQUA)));

        float avgLevel = skillData.avgSkillLevel();
        Component text = avgSkillLevel().append(makeText(avgLevel, ChatFormatting.DARK_AQUA));
        texts.add(text);

        int totalXp = skillData.totalXp();
        Component text2 = totalXp().append(makeText(totalXp, ChatFormatting.DARK_AQUA));
        texts.add(text2);

        return texts;
    }

    public static List<Component> $attributes(Map<Attribute, Double> valueMap)
    {
        List<Component> texts = new ArrayList<>();
        int[] colors = new int[]{0x5555FF, 0x55AAFF, 0x55FFFF};
        int index = 0;

        for (Map.Entry<Attribute, Double> entry : valueMap.entrySet())
        {
            double value = entry.getValue();
            if (value == 0) continue;

            int color = colors[index % 3];
            index++;

            Component text = attribute(entry.getKey(), entry.getValue()).withStyle(style -> style.withColor(color));
            texts.add(space().append(text));
        }
        return texts;
    }

    public static List<Component> $ascension(List<RewardLike<?>> rewardLikes)
    {
        boolean hasSplash = PeritiaClientConfig.SPLASH_TEXT.get();
        List<Component> texts = new ArrayList<>();

        if (!rewardLikes.isEmpty())
        {
            texts.add(rewards());
            for (RewardLike<?> rewardLike : rewardLikes)
            {
                if (rewardLike.hasText()) texts.add(space().append(rewardLike.tryGetText()));
            }
            texts.add(space());
        }
        if (hasSplash) texts.add(splash());
        return texts;
    }

    public static List<Component> $blockInfo(BlockXpSource xpSource, int debt, boolean correctTool)
    {
        XpGainContext context = PeritiaJadeHelper.makeContext(xpSource, correctTool);
        ChatFormatting color = context.blocked() ? ChatFormatting.RED : ChatFormatting.AQUA;

        List<Component> texts = new ArrayList<>();

        Component text = Component.translatable("label.peritia.break").withStyle(ChatFormatting.GRAY).append(xpGain(context));
        texts.add(text);

        if (debt > 0)
        {
            Component debtText = Component.translatable("label.peritia.debt").withStyle(ChatFormatting.GRAY).append(makeText(debt, color));
            texts.add(debtText);
        }
        if (!correctTool) texts.add(incorrectTool());
        return texts;
    }

    public static List<Component> $entityInfo(EntityXpSource xpSource, Entity entity)
    {
        XpGainContext context = PeritiaJadeHelper.makeContext(xpSource, entity);
        Component text = Component.translatable("label.peritia.death").withStyle(ChatFormatting.GRAY).append(xpGain(context));
        return List.of(text);
    }

    public static List<Component> $chestInfo(ChestXpSource xpSource, boolean breakable)
    {
        XpGainContext context = PeritiaJadeHelper.makeContext(xpSource);
        String translationKey = breakable ? "label.peritia.loot_or_break" : "label.peritia.loot";
        Component text = Component.translatable(translationKey).withStyle(ChatFormatting.GRAY).append(xpGain(context));

        return List.of(text);
    }

    public static List<Component> $detailedInstructions(String string, boolean allowCheats)
    {
        int maxIndex = allowCheats ? 16 : 9;
        List<Component> texts = new ArrayList<>();

        for (int i = 0; i < maxIndex; i++)
        {
            if (i == 1 || i == 2)
            {
                texts.add(Component.translatable("command.peritia.help." + i, string));
            }
            else texts.add(Component.translatable("command.peritia.help." + i));
        }
        return texts;
    }

}
