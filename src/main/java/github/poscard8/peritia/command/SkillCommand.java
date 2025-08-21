package github.poscard8.peritia.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

import static github.poscard8.peritia.command.PeritiaCommand.*;

public class SkillCommand
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> argumentBuilder = Commands.literal("skill").requires(PeritiaCommand.CHEAT_PREDICATE)
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
                                                .executes(ctx -> getInfo(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), SingleSkillArgument.getSkill(ctx), SkillInfo.PROGRESS))))));

        dispatcher.register(argumentBuilder);
    }

}
