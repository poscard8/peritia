package github.poscard8.peritia.util.xpsource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import github.poscard8.peritia.util.serialization.ArraySerializable;
import github.poscard8.peritia.util.serialization.SerializableChunkPos;
import net.minecraft.world.level.ChunkPos;

import java.util.HashSet;
import java.util.Set;

public class VisitedChunks implements ArraySerializable<VisitedChunks>
{
    protected Set<SerializableChunkPos> chunks;

    public VisitedChunks(Set<SerializableChunkPos> chunks) { this.chunks = chunks; }

    public static VisitedChunks empty() { return new VisitedChunks(new HashSet<>()); }

    public static VisitedChunks tryLoad(JsonArray data) { return empty().loadWithFallback(data); }

    public Set<SerializableChunkPos> chunks() { return chunks; }

    public boolean hasChunk(ChunkPos chunkPos) { return hasChunk(SerializableChunkPos.of(chunkPos)); }

    public boolean hasChunk(SerializableChunkPos chunkPos)
    {
        for (SerializableChunkPos chunk : chunks())
        {
            if (chunk.equals(chunkPos)) return true;
        }
        return false;
    }

    public void addChunk(ChunkPos chunkPos) { addChunk(SerializableChunkPos.of(chunkPos)); }

    public void addChunk(SerializableChunkPos chunkPos) { chunks().add(chunkPos); }

    @Override
    public VisitedChunks fallback() { return empty(); }

    @Override
    public VisitedChunks load(JsonArray data)
    {
        for (JsonElement element : data)
        {
            if (element.isJsonPrimitive()) chunks.add(SerializableChunkPos.tryLoad(element.getAsString()));
        }

        return this;
    }

    @Override
    public JsonArray save()
    {
        JsonArray data = new JsonArray();
        for (SerializableChunkPos chunkPos : chunks) data.add(chunkPos.save());

        return data;
    }

}
