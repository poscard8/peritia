package github.poscard8.peritia.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.skill.Skill;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class SingleSkillArgument implements ArgumentType<Skill>, PeritiaCommandArgument
{
    SingleSkillArgument() {}

    public static SingleSkillArgument of() { return new SingleSkillArgument(); }

    public static Skill getSkill(CommandContext<CommandSourceStack> context) { return getSkill(context, "skill"); }

    public static Skill getSkill(CommandContext<CommandSourceStack> context, String argumentKey)
    {
        return context.getArgument(argumentKey, Skill.class);
    }

    @Override
    public Skill parse(StringReader reader) throws CommandSyntaxException
    {
        int start = reader.getCursor();

        while(reader.canRead() && !Objects.equals(reader.peek(), ' ')) { reader.skip(); }
        String data = reader.getString().substring(start, reader.getCursor());

        try
        {
            @Nullable Skill skill = Skill.byString(data);
            if (skill == null) throw new RuntimeException();
            return skill;
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
        String node = getNode(context);
        String substring = node.length() < 2 ? "" : node.substring(1);

        if (Skill.byString(node) != null)
        {
            return builder.buildFuture();
        }
        else
        {
            for (Skill skill : Peritia.skills())
            {
                if (skill.isValidStart(substring)) builder.suggest(skill.stringKey());
            }
        }
        return builder.buildFuture();
    }

}
