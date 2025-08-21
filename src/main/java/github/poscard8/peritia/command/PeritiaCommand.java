package github.poscard8.peritia.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.client.menu.PeritiaMainMenu;
import github.poscard8.peritia.config.PeritiaServerConfig;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.clientbound.InstructionsPacket;
import github.poscard8.peritia.network.packet.clientbound.LookContextPacket;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.SkillInstance;
import github.poscard8.peritia.skill.data.ServerSkillData;
import github.poscard8.peritia.util.skill.SkillFunction;
import github.poscard8.peritia.util.text.PeritiaTexts;
import github.poscard8.peritia.util.text.ProgressTextType;
import github.poscard8.peritia.util.xpsource.DataXpSourceFunction;
import github.poscard8.peritia.util.xpsource.XpSourceFunction;
import github.poscard8.peritia.xpsource.data.ServerXpSourceData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class PeritiaCommand
{
    static final SimpleCommandExceptionType VIEW_INVALID_SOURCE = new SimpleCommandExceptionType(Component.translatable("command.peritia.view_invalid_source"));
    static final SimpleCommandExceptionType NO_PLAYER = new SimpleCommandExceptionType(Component.translatable("command.peritia.no_player"));
    static final SimpleCommandExceptionType NO_SKILLS = new SimpleCommandExceptionType(Component.translatable("command.peritia.no_skill"));
    static final SimpleCommandExceptionType NO_XP_SOURCES = new SimpleCommandExceptionType(Component.translatable("command.peritia.no_xp_source"));

    static final Predicate<CommandSourceStack> CHEAT_PREDICATE = sourceStack -> sourceStack.hasPermission(2);
    static final Predicate<CommandSourceStack> HELP_PREDICATE = CHEAT_PREDICATE.or(sourceStack -> PeritiaServerConfig.HELP_COMMAND.get().test());
    static final Predicate<CommandSourceStack> VIEW_PREDICATE = CHEAT_PREDICATE.or(sourceStack -> PeritiaServerConfig.VIEW_COMMAND.get().test());
    static final Predicate<CommandSourceStack> COMMAND_PREDICATE = CHEAT_PREDICATE.or(sourceStack -> PeritiaServerConfig.HELP_COMMAND.get().test() || PeritiaServerConfig.VIEW_COMMAND.get().test());

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> argumentBuilder = Commands.literal(Peritia.ID).requires(COMMAND_PREDICATE)
                .then(Commands.literal("help").requires(HELP_PREDICATE)
                        .executes(ctx -> help(ctx.getSource(), ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("view").requires(VIEW_PREDICATE)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> viewPlayer(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("skill").requires(CHEAT_PREDICATE)
                        .then(Commands.literal("add")
                                .then(Commands.argument("player", EntityArgument.players())
                                        .then(Commands.argument("skill", SkillArgument.of())
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                        .then(Commands.literal("xp")
                                                                .then(Commands.argument("ignoreRestrictions", BoolArgumentType.bool())
                                                                        .executes(ctx -> addXP(ctx.getSource(), EntityArgument.getPlayers(ctx, "player"), SkillArgument.getSkills(ctx), IntegerArgumentType.getInteger(ctx, "amount"), BoolArgumentType.getBool(ctx, "ignoreRestrictions"))))
                                                                .executes(ctx -> addXP(ctx.getSource(), EntityArgument.getPlayers(ctx, "player"), SkillArgument.getSkills(ctx), IntegerArgumentType.getInteger(ctx, "amount"), true)))
                                                        .then(Commands.literal("level")
                                                                .executes(ctx -> addLevel(ctx.getSource(), EntityArgument.getPlayers(ctx, "player"), SkillArgument.getSkills(ctx), IntegerArgumentType.getInteger(ctx, "amount"))))
                                                        .executes(ctx -> addXP(ctx.getSource(), EntityArgument.getPlayers(ctx, "player"), SkillArgument.getSkills(ctx), IntegerArgumentType.getInteger(ctx, "amount"), true))))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.players())
                                        .then(Commands.argument("skill", SkillArgument.of())
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                        .then(Commands.literal("xp")
                                                                .then(Commands.argument("ignoreRestrictions", BoolArgumentType.bool())
                                                                        .executes(ctx -> setXP(ctx.getSource(), EntityArgument.getPlayers(ctx, "player"), SkillArgument.getSkills(ctx), IntegerArgumentType.getInteger(ctx, "amount"), BoolArgumentType.getBool(ctx, "ignoreRestrictions"))))
                                                                .executes(ctx -> setXP(ctx.getSource(), EntityArgument.getPlayers(ctx, "player"), SkillArgument.getSkills(ctx), IntegerArgumentType.getInteger(ctx, "amount"), true)))
                                                        .then(Commands.literal("level")
                                                                .executes(ctx -> setLevel(ctx.getSource(), EntityArgument.getPlayers(ctx, "player"), SkillArgument.getSkills(ctx), IntegerArgumentType.getInteger(ctx, "amount"))))
                                                        .executes(ctx -> setXP(ctx.getSource(), EntityArgument.getPlayers(ctx, "player"), SkillArgument.getSkills(ctx), IntegerArgumentType.getInteger(ctx, "amount"), true))))))
                        .then(Commands.literal("reset")
                                .then(Commands.argument("player", EntityArgument.players())
                                        .then(Commands.argument("skill", SkillArgument.of())
                                                .executes(ctx -> resetSkill(ctx.getSource(), EntityArgument.getPlayers(ctx, "player"), SkillArgument.getSkills(ctx))))))
                        .then(Commands.literal("get")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("skill", SingleSkillArgument.of())
                                                .then(Commands.literal("xp")
                                                        .executes(ctx -> getInfo(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), SingleSkillArgument.getSkill(ctx), SkillInfo.XP)))
                                                .then(Commands.literal("total_xp")
                                                        .executes(ctx -> getInfo(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), SingleSkillArgument.getSkill(ctx), SkillInfo.TOTAL_XP)))
                                                .then(Commands.literal("level")
                                                        .executes(ctx -> getInfo(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), SingleSkillArgument.getSkill(ctx), SkillInfo.LEVEL)))
                                                .then(Commands.literal("progress")
                                                        .executes(ctx -> getInfo(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), SingleSkillArgument.getSkill(ctx), SkillInfo.PROGRESS)))))))
                .then(Commands.literal("xp_source").requires(CHEAT_PREDICATE)
                        .then(Commands.literal("cover")
                                .then(Commands.argument("player", EntityArgument.players())
                                        .then(Commands.argument("xp_source", XpSourceArgument.of())
                                                .executes(ctx -> coverXpSources(ctx.getSource(), EntityArgument.getPlayers(ctx, "player"), XpSourceArgument.getXpSources(ctx))))))
                        .then(Commands.literal("discover")
                                .then(Commands.argument("player", EntityArgument.players())
                                        .then(Commands.argument("xp_source", XpSourceArgument.of())
                                                .executes(ctx -> discoverXpSources(ctx.getSource(), EntityArgument.getPlayers(ctx, "player"), XpSourceArgument.getXpSources(ctx))))))
                        .then(Commands.literal("reset")
                                .then(Commands.argument("player", EntityArgument.players())
                                        .then(Commands.argument("xp_source", DataXpSourceArgument.of())
                                                .executes(ctx -> resetXpSources(ctx.getSource(), EntityArgument.getPlayers(ctx, "player"), DataXpSourceArgument.getXpSources(ctx)))))))
                .then(Commands.literal("ascend").requires(CHEAT_PREDICATE)
                        .then(Commands.argument("player", EntityArgument.players())
                                .executes(ctx -> ascend(ctx.getSource(), EntityArgument.getPlayers(ctx, "player"))))
                        .executes(ctx -> ascend(ctx.getSource(), getPlayer(ctx))))
                .then(Commands.literal("max_out").requires(CHEAT_PREDICATE)
                        .then(Commands.argument("player", EntityArgument.players())
                                .executes(ctx -> maxOut(ctx.getSource(), EntityArgument.getPlayers(ctx, "player"))))
                        .executes(ctx -> maxOut(ctx.getSource(), getPlayer(ctx))))
                .then(Commands.literal("reset").requires(CHEAT_PREDICATE)
                        .then(Commands.argument("player", EntityArgument.players())
                                .executes(ctx -> reset(ctx.getSource(), EntityArgument.getPlayers(ctx, "player"))))
                        .executes(ctx -> reset(ctx.getSource(), getPlayer(ctx))));

        dispatcher.register(argumentBuilder);
    }

    static int help(CommandSourceStack sourceStack, ServerPlayer player)
    {
        boolean allowCheats = sourceStack.hasPermission(2);
        PeritiaNetworkHandler.sendToClient(InstructionsPacket.detailed(allowCheats), player);
        return 0;
    }

    static int viewPlayer(CommandSourceStack sourceStack, ServerPlayer player) throws CommandSyntaxException
    {
        if (player == null) throw NO_PLAYER.create();

        try
        {
            ServerPlayer self = sourceStack.getPlayerOrException();

            boolean viewingOther = !self.getStringUUID().equals(player.getStringUUID());
            if (viewingOther) PeritiaNetworkHandler.sendToClient(new LookContextPacket(player), self);

            self.openMenu(PeritiaMainMenu.provider((byte) 0));

            sourceStack.sendSuccess(() -> Component.translatable("command.peritia.view_success", player.getName().getString()), true);
            return 0;
        }
        catch (CommandSyntaxException e) { throw VIEW_INVALID_SOURCE.create(); }
    }

    static int addXP(CommandSourceStack sourceStack, Collection<ServerPlayer> players, SkillFunction skillFunction, int xp, boolean ignoreRestrictions)
    {
        for (ServerPlayer player : players) ServerSkillData.of(player).addXpToSkills(skillFunction, null, xp, false, ignoreRestrictions);

        sourceStack.sendSuccess(() -> Component.translatable("command.peritia.skill_add_xp_success", xp, skillFunction.count(), players.size()), true);
        return 0;
    }

    static int addLevel(CommandSourceStack sourceStack, Collection<ServerPlayer> players, SkillFunction skillFunction, int levels) {

        for (ServerPlayer player : players) ServerSkillData.of(player).addLevelsToSkills(skillFunction, levels);

        sourceStack.sendSuccess(() -> Component.translatable("command.peritia.skill_add_level_success", levels, skillFunction.count(), players.size()), true);
        return 0;
    }

    static int setXP(CommandSourceStack sourceStack, Collection<ServerPlayer> players, SkillFunction skillFunction, int xp, boolean ignoreRestrictions) {

        for (ServerPlayer player : players) ServerSkillData.of(player).setXpOfSkills(skillFunction, xp, ignoreRestrictions);

        sourceStack.sendSuccess(() -> Component.translatable("command.peritia.skill_set_xp_success", skillFunction.count(), players.size(), xp), true);
        return 0;
    }

    static int setLevel(CommandSourceStack sourceStack, Collection<ServerPlayer> players, SkillFunction skillFunction, int level)
    {
        for (ServerPlayer player : players) ServerSkillData.of(player).setLevelsOfSkills(skillFunction, level);

        sourceStack.sendSuccess(() -> Component.translatable("command.peritia.skill_set_level_success", skillFunction.count(), players.size(), level), true);
        return 0;
    }

    static int resetSkill(CommandSourceStack sourceStack, Collection<ServerPlayer> players, SkillFunction skillFunction)
    {
        for (ServerPlayer player : players) ServerSkillData.of(player).resetSkills(skillFunction);

        sourceStack.sendSuccess(() -> Component.translatable("command.peritia.skill_reset_success", skillFunction.count(), players.size()), true);
        return 0;
    }

    static int getInfo(CommandSourceStack sourceStack, ServerPlayer player, Skill skill, SkillInfo info) throws CommandSyntaxException
    {
        SkillInstance instance = ServerSkillData.of(player).getSkill(skill);

        switch (info)
        {
            case LEVEL -> sourceStack.sendSuccess(() -> Component.translatable("command.peritia.skill_get_level_success", PeritiaTexts.skill(skill), player.getName(), instance.level()), true);
            case XP -> sourceStack.sendSuccess(() -> Component.translatable("command.peritia.skill_get_xp_success", PeritiaTexts.skill(skill), player.getName(), instance.xp()), true);
            case TOTAL_XP -> sourceStack.sendSuccess(() -> Component.translatable("command.peritia.skill_get_total_xp_success", PeritiaTexts.skill(skill), player.getName(), instance.totalXp()), true);
            default ->
            {
                sourceStack.sendSuccess(() -> Component.translatable("command.peritia.skill_get_progress_success", PeritiaTexts.skill(skill), player.getName()), true);

                ServerPlayer self = sourceStack.getPlayerOrException();

                Component text = PeritiaTexts.space()
                        .append(PeritiaTexts.level().copy().withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                        .append(PeritiaTexts.progressBar(instance.level(), instance.maxLevel(), 30, ProgressTextType.NUMERIC));

                int totalXp = instance.totalXp();
                int neededXp = skill.getNeededTotalXp();
                double percentage = 100.0F * totalXp / neededXp;
                Component percentageText = Component.translatable("(%s)", PeritiaTexts.makePercentage(percentage)).withStyle(ChatFormatting.DARK_GREEN);

                Component text2 = PeritiaTexts.space()
                        .append(PeritiaTexts.totalXp())
                        .append(PeritiaTexts.progressBar(totalXp, neededXp, 30, ProgressTextType.NUMERIC))
                        .append(PeritiaTexts.space())
                        .append(percentageText);

                self.displayClientMessage(text, false);
                self.displayClientMessage(text2, false);
            }
        }
        return 0;
    }

    static int ascend(CommandSourceStack sourceStack, Collection<ServerPlayer> players)
    {
        for (ServerPlayer player : players) ServerSkillData.of(player).ascend(false);
        sourceStack.sendSuccess(() -> Component.translatable("command.peritia.ascension_success", players.size()), true);
        return 0;
    }

    static int coverXpSources(CommandSourceStack sourceStack, Collection<ServerPlayer> players, XpSourceFunction xpSourceFunction)
    {
        for (ServerPlayer player : players) ServerSkillData.of(player).coverXpSources(xpSourceFunction);
        sourceStack.sendSuccess(() -> Component.translatable("command.peritia.xp_source_cover_success", xpSourceFunction.getXpSources().size(), players.size()), true);
        return 0;
    }

    static int discoverXpSources(CommandSourceStack sourceStack, Collection<ServerPlayer> players, XpSourceFunction xpSourceFunction)
    {
        for (ServerPlayer player : players) ServerSkillData.of(player).discoverXpSources(xpSourceFunction);
        sourceStack.sendSuccess(() -> Component.translatable("command.peritia.xp_source_discover_success", xpSourceFunction.getXpSources().size(), players.size()), true);
        return 0;
    }

    static int resetXpSources(CommandSourceStack sourceStack, Collection<ServerPlayer> players, DataXpSourceFunction xpSourceFunction)
    {
        ServerXpSourceData xpSourceData = ServerXpSourceData.of(sourceStack.getServer());
        xpSourceData.reset(players, xpSourceFunction);

        sourceStack.sendSuccess(() -> Component.translatable("command.peritia.xp_source_reset_success", xpSourceFunction.getXpSources().size(), players.size()), true);
        return 0;
    }

    static int maxOut(CommandSourceStack sourceStack, Collection<ServerPlayer> players) {

        for (ServerPlayer player : players) ServerSkillData.of(player).maxOut();
        sourceStack.sendSuccess(() -> Component.translatable("command.peritia.max_out_success", players.size()), true);
        return 0;
    }

    static int reset(CommandSourceStack sourceStack, Collection<ServerPlayer> players)
    {
        for (ServerPlayer player : players) ServerSkillData.of(player).reset();
        sourceStack.sendSuccess(() -> Component.translatable("command.peritia.reset_success", players.size()), true);
        return 0;
    }

    static List<ServerPlayer> getPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException
    {
        return List.of(ctx.getSource().getPlayerOrException());
    }

}
