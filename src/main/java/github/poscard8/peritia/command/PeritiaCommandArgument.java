package github.poscard8.peritia.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;

import java.util.List;

public interface PeritiaCommandArgument
{
    default <T> String getNode(CommandContext<T> context) { return getNode(context, ((Object) this).getClass()); }

    default <T> String getNode(CommandContext<T> context, Class<?> clazz)
    {
        List<ParsedCommandNode<T>> nodes = context.getNodes();
        ParsedCommandNode<?> parsedNode;
        String node;
        String input = context.getInput();
        int index = -1;

        for (int i = 1; i < nodes.size(); i++)
        {
            if (nodes.get(i).getNode() instanceof ArgumentCommandNode<?, ?> arg)
            {
                if (arg.getType().getClass().equals(clazz))
                {
                    index = i;
                    break;
                }
            }
        }

        try
        {
            parsedNode = nodes.get(index);
            node = parsedNode.getRange().get(input);
        }
        catch (IndexOutOfBoundsException exception)
        {
            parsedNode = nodes.get(nodes.size() - 1);
            node = input.substring(Math.min(input.length(), parsedNode.getRange().getEnd()));
        }
        return node;
    }
    
}
