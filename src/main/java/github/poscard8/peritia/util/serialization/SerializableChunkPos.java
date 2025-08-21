package github.poscard8.peritia.util.serialization;

import net.minecraft.world.level.ChunkPos;

public class SerializableChunkPos implements StringSerializable<SerializableChunkPos>
{
    protected int x;
    protected int z;

    public SerializableChunkPos(int x, int z)
    {
        this.x = x;
        this.z = z;
    }

    public static SerializableChunkPos empty() { return new SerializableChunkPos(0, 0); }

    public static SerializableChunkPos tryLoad(String data) { return empty().loadWithFallback(data); }

    public static SerializableChunkPos of(ChunkPos chunkPos) { return new SerializableChunkPos(chunkPos.x, chunkPos.z); }

    public int x() { return x; }

    public int z() { return z; }

    @Override
    public SerializableChunkPos fallback() { return empty(); }

    @Override
    public SerializableChunkPos load(String data)
    {
        String[] split = data.split(",");

        this.x = Integer.parseInt(split[0]);
        this.z = Integer.parseInt(split[1]);

        return this;
    }

    @Override
    public String save() { return String.format("%d,%d", x, z); }

    @Override
    public boolean equals(Object object)
    {
        return object instanceof SerializableChunkPos chunkPos && x() == chunkPos.x() && z() == chunkPos.z();
    }

    @Override
    public String toString() { return String.format("(%s)", save()); }

}
