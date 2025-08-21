package github.poscard8.peritia.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.PeritiaRegistries;
import github.poscard8.peritia.util.xpsource.DataXpSourceFunction;
import github.poscard8.peritia.xpsource.DataXpSource;
import github.poscard8.peritia.xpsource.XpSourceType;
import net.minecraft.commands.CommandSourceStack;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class DataXpSourceArgument implements ArgumentType<DataXpSourceFunction>, PeritiaCommandArgument
{
    DataXpSourceArgument() {}

    public static DataXpSourceArgument of() { return new DataXpSourceArgument(); }

    public static DataXpSourceFunction getXpSources(CommandContext<CommandSourceStack> context) { return getXpSources(context, "xp_source"); }

    public static DataXpSourceFunction getXpSources(CommandContext<CommandSourceStack> context, String argumentKey)
    {
        return context.getArgument(argumentKey, DataXpSourceFunction.class);
    }

    @Override
    public DataXpSourceFunction parse(StringReader reader) throws CommandSyntaxException
    {
        int start = reader.getCursor();

        while(reader.canRead() && !Objects.equals(reader.peek(), ' ')) { reader.skip(); }
        String data = reader.getString().substring(start, reader.getCursor());

        try
        {
            return DataXpSourceFunction.loadWithExceptions(data);
        }
        catch (Exception exception)
        {
            reader.setCursor(start);
            throw PeritiaCommand.NO_XP_SOURCES.create();
        }
    }

    @Override
    public <T> CompletableFuture<Suggestions> listSuggestions(CommandContext<T> context, SuggestionsBuilder builder)
    {
        String node = getNode(context);
        String substring = node.length() < 2 ? "" : node.substring(1);

        if (substring.startsWith("$"))
        {
            for (XpSourceType<?> type : PeritiaRegistries.xpSourceTypes().getValues())
            {
                if (type.isDataType()) builder.suggest("$" + type.key().toString());
            }
        }
        else
        {
            String substring2 = substring.length() < 2 ? "" : substring.substring(1);

            for (Skill skill : Peritia.skills())
            {
                if ((substring.startsWith("#") && skill.isValidStart(substring2)) || substring.isEmpty()) builder.suggest("#" + skill.stringKey());
            }
            for (DataXpSource xpSource : Peritia.xpSourceHandler().dataXpSources())
            {
                if (xpSource.isValidStart(substring)) builder.suggest(xpSource.stringKey());
            }

            if ("all".startsWith(substring)) builder.suggest("all");
            if (substring.isEmpty()) builder.suggest("$");
        }
        return builder.buildFuture();
    }

}
