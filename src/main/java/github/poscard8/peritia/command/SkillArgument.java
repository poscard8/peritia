package github.poscard8.peritia.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.skill.SkillFunction;
import net.minecraft.commands.CommandSourceStack;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class SkillArgument implements ArgumentType<SkillFunction>, PeritiaCommandArgument
{
    SkillArgument() {}

    public static SkillArgument of() { return new SkillArgument(); }

    public static SkillFunction getSkills(CommandContext<CommandSourceStack> context) { return getSkills(context, "skill"); }

    public static SkillFunction getSkills(CommandContext<CommandSourceStack> context, String argumentKey)
    {
        return context.getArgument(argumentKey, SkillFunction.class);
    }

    @Override
    public SkillFunction parse(StringReader reader) throws CommandSyntaxException
    {
        int start = reader.getCursor();

        while(reader.canRead() && !Objects.equals(reader.peek(), ' ')) { reader.skip(); }
        String data = reader.getString().substring(start, reader.getCursor());

        try
        {
            return SkillFunction.loadWithExceptions(data);
        }
        catch (Exception exception)
        {
            reader.setCursor(start);
            throw PeritiaCommand.NO_SKILLS.create();
        }
    }

    @Override
    public <T> CompletableFuture<Suggestions> listSuggestions(CommandContext<T> context, SuggestionsBuilder builder)
    {
        String input = context.getInput();
        String node = getNode(context);
        String substring = node.length() < 2 ? "" : node.substring(1);

        if (Skill.byString(node) != null)
        {
            SuggestionsBuilder builder2 = new SuggestionsBuilder(input, input.length());
            builder2.suggest(",");
            return builder2.buildFuture();
        }
        else if (SkillFunction.Double.isValidPrefix(substring))
        {
            String node2 = node.substring(1);
            SuggestionsBuilder builder2 = new SuggestionsBuilder(input, input.length());

            Peritia.skillHandler().keys().forEach(key ->
            {
                String firstSkillArgument = node2.substring(0, node2.length() - 1);
                if (!key.toString().equals(firstSkillArgument)) builder2.suggest(key.toString());
            });
            return builder2.buildFuture();
        }
        else if (SkillFunction.Double.isValidString(node))
        {
            return builder.buildFuture();
        }
        else
        {
            for (Skill skill : Peritia.skills())
            {
                if (skill.isValidStart(substring)) builder.suggest(skill.stringKey());
            }

            Set<String> special = Set.of("all", "highest", "lowest", "median", "random");
            special.forEach(string ->
            {
                if (string.startsWith(substring)) builder.suggest(string);
            });

            return builder.buildFuture();
        }
    }




}
