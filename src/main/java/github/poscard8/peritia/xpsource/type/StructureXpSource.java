package github.poscard8.peritia.xpsource.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.text.Hints;
import github.poscard8.peritia.util.xpsource.StructureInputs;
import github.poscard8.peritia.util.xpsource.VisitedChunks;
import github.poscard8.peritia.xpsource.DataXpSource;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.XpSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StructureXpSource extends DataXpSource
{
    public static final int DEFAULT_CHECK_PERIOD = 20;

    protected final Map<String, VisitedChunks> visitedChunksMap = new HashMap<>();

    protected StructureInputs inputs = StructureInputs.empty();
    protected int checkPeriod = DEFAULT_CHECK_PERIOD;
    protected Hints hints = Hints.empty();
    protected boolean useHints = false;

    public StructureXpSource(ResourceLocation key) { super(key); }

    public static StructureXpSource empty() { return new StructureXpSource(EMPTY_KEY); }

    @Nullable
    public static StructureXpSource tryLoad(JsonObject data)
    {
        @Nullable XpSource xpSource = empty().loadWithFallback(data);
        return xpSource != null ? (StructureXpSource) xpSource : null;
    }

    @Override
    public XpSourceType<?> type() { return PeritiaXpSourceTypes.STRUCTURE.get(); }

    public StructureInputs inputs() { return inputs; }

    public int checkPeriod() { return checkPeriod; }

    public boolean shouldCheck(int playerTicks) { return playerTicks >= checkPeriod() && playerTicks % checkPeriod() == 0; }

    public Hints hints() { return hints; }

    public boolean shouldUseHints() { return useHints; }

    @Override
    public boolean isInvalid() { return super.isInvalid() || inputs().isEmpty() || checkPeriod() <= 0; }

    public VisitedChunks getVisitedChunks(ServerPlayer player)
    {
        tryLoad(player);
        return visitedChunksMap.getOrDefault(player.getStringUUID(), VisitedChunks.empty());
    }

    public boolean hasPlayerVisitedChunk(ServerPlayer player, ChunkPos chunkPos) { return getVisitedChunks(player).hasChunk(chunkPos); }

    public void addVisitedChunk(ServerPlayer player, ChunkPos chunkPos)
    {
        VisitedChunks visitedChunks = getVisitedChunks(player);
        visitedChunks.addChunk(chunkPos);
        visitedChunksMap.put(player.getStringUUID(), visitedChunks);

        update(player);
    }

    public void handlePlayerTick(ServerPlayer player)
    {
        if (shouldCheck(player.tickCount) && canPlayerGainXp(player))
        {
            Set<StructureStart> starts = inputs.getStructureStarts(player);

            for (StructureStart start : starts)
            {
                if (!start.isValid()) continue;

                ChunkPos chunkPos = start.getChunkPos();
                if (hasPlayerVisitedChunk(player, chunkPos)) continue;

                award(player);
                addVisitedChunk(player, chunkPos);
            }
        }
    }

    @Override
    public void loadData(JsonObject data)
    {
        visitedChunksMap.clear();
        for (Map.Entry<String, JsonElement> entry : data.entrySet()) visitedChunksMap.put(entry.getKey(), VisitedChunks.tryLoad(entry.getValue().getAsJsonArray()));
    }

    @Override
    public JsonObject saveData()
    {
        JsonObject data = new JsonObject();

        for (String key : visitedChunksMap.keySet())
        {
            VisitedChunks value = visitedChunksMap.get(key);
            JsonHelper.write(data, key, value);
        }

        return data;
    }

    @Override
    public void loadAdditional(JsonObject data)
    {
        this.inputs = JsonHelper.readElementSerializable(data, "inputs", StructureInputs::tryLoad, inputs);
        this.checkPeriod = JsonHelper.readInt(data, "checkPeriod", checkPeriod);
        this.hints = JsonHelper.readArraySerializable(data, "hints", Hints::tryLoad, hints);
        this.useHints = JsonHelper.readBoolean(data, "useHints", data.has("hints"));
    }

    @Override
    public void saveAdditional(JsonObject data)
    {
        JsonHelper.write(data, "inputs", inputs);
        JsonHelper.write(data, "checkPeriod", checkPeriod);
        JsonHelper.write(data, "hints", hints);
        JsonHelper.write(data, "useHints", useHints);
    }

}
