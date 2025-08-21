package github.poscard8.peritia.util.xpsource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import github.poscard8.peritia.util.serialization.ElementSerializable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class StructureInputs implements Iterable<StructureInput>, ElementSerializable<StructureInputs>
{
    protected List<StructureInput> inputs;
    protected boolean acceptAll = false;

    public StructureInputs(List<StructureInput> inputs) { this.inputs = inputs; }

    public List<StructureInput> inputs() { return inputs; }

    public boolean acceptsAll() { return acceptAll; }

    public static StructureInputs empty() { return new StructureInputs(new ArrayList<>()); }

    public static StructureInputs tryLoad(JsonElement data) { return empty().loadWithFallback(data); }

    public static StructureInputs acceptAll()
    {
        StructureInputs inputs = empty();
        inputs.acceptAll = true;
        return inputs;
    }

    public Set<StructureStart> getStructureStarts(ServerPlayer player)
    {
        Set<StructureStart> structureStarts = new HashSet<>();
        for (StructureInput input : this) structureStarts.addAll(input.getStructureStarts(player));

        return structureStarts;
    }

    @Override
    @NotNull
    public Iterator<StructureInput> iterator() { return inputs().iterator(); }

    public boolean isEmpty() { return inputs().isEmpty() && !acceptsAll(); }

    @Override
    public StructureInputs fallback() { return empty(); }

    @Override
    public StructureInputs load(JsonElement data)
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
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())
                {
                    String string = element.getAsString();
                    StructureInput input = StructureInput.tryLoad(string);
                    if (input.isValid()) inputs.add(input);
                }
            }
        }
        return this;
    }

    @Override
    public JsonElement save()
    {
        if (acceptAll) return Input.ALL_NAME_PRIMITIVE;

        JsonArray data = new JsonArray();
        for (StructureInput input : this) data.add(input.save());

        return data;
    }


}
