package github.poscard8.peritia.util.xpsource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import github.poscard8.peritia.util.serialization.ElementSerializable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class BlockInputs implements Iterable<BlockInput>, Predicate<BlockState>, ElementSerializable<BlockInputs>
{
    protected List<BlockInput> inputs;
    protected boolean acceptAll = false;

    public BlockInputs(List<BlockInput> inputs) { this.inputs = inputs; }

    public List<BlockInput> inputs() { return inputs; }

    public boolean acceptsAll() { return acceptAll; }

    public static BlockInputs empty() { return new BlockInputs(new ArrayList<>()); }

    public static BlockInputs tryLoad(JsonElement data) { return empty().loadWithFallback(data); }

    public static BlockInputs acceptAll()
    {
        BlockInputs inputs = empty();
        inputs.acceptAll = true;
        return inputs;
    }

    @Override
    public boolean test(BlockState state)
    {
        if (acceptsAll()) return true;

        for (BlockInput input : this)
        {
            if (input.test(state)) return true;
        }
        return false;
    }

    public boolean testForDisplay(Block block)
    {
        for (BlockInput input : this)
        {
            if (input.testForDisplay(block)) return true;
        }
        return false;
    }

    @Override
    @NotNull
    public Iterator<BlockInput> iterator() { return inputs().iterator(); }

    public boolean isEmpty() { return inputs().isEmpty() && !acceptsAll(); }

    @Override
    public BlockInputs fallback() { return empty(); }

    @Override
    public BlockInputs load(JsonElement data)
    {
        if (data.isJsonPrimitive() && data.getAsJsonPrimitive().isString())
        {
            String string = data.getAsString();
            if (string.equals(Input.ALL_NAME)) this.acceptAll = true;
        }
        else if (data.isJsonArray())
        {
            JsonArray jsonArray = data.getAsJsonArray();

            for (JsonElement element : jsonArray)
            {
                BlockInput input = BlockInput.tryLoad(element);
                if (input.isValid()) inputs.add(input);
            }
        }
        return this;
    }

    @Override
    public JsonElement save()
    {
        if (acceptAll) return Input.ALL_NAME_PRIMITIVE;

        JsonArray data = new JsonArray();
        for (BlockInput input : this) data.add(input.save());

        return data;
    }


}
