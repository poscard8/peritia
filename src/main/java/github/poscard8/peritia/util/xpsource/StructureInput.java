package github.poscard8.peritia.util.xpsource;

import github.poscard8.peritia.util.PeritiaHelper;
import github.poscard8.peritia.util.serialization.Loadable;
import github.poscard8.peritia.util.serialization.StringSerializable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.Set;

public abstract class StructureInput implements StringSerializable<StructureInput>
{
    public static StructureInput empty() { return Single.empty(); }

    @Override
    public StructureInput fallback() { return empty();}

    public static StructureInput tryLoad(String data)
    {
        if (data.startsWith("#")) return Tag.tryLoad(data);
        return Single.tryLoad(data);
    }

    public abstract Set<StructureStart> getStructureStarts(ServerPlayer player);

    public abstract boolean isValid();


    public static class Single extends StructureInput
    {
        protected ResourceLocation structureKey;

        public Single(ResourceLocation structureKey) { this.structureKey = structureKey; }

        public static Single empty() { return new Single(Loadable.EMPTY_KEY); }

        public static Single tryLoad(String data)
        {
            return empty().loadWithFallback(data) instanceof Single single ? single : empty();
        }

        public ResourceLocation structureKey() { return structureKey; }

        public ResourceKey<Structure> getResourceKey() { return ResourceKey.create(Registries.STRUCTURE, structureKey()); }

        @Override
        public Set<StructureStart> getStructureStarts(ServerPlayer player)
        {
            StructureManager structureManager = PeritiaHelper.getStructureManager(player);
            StructureStart structureStart = structureManager.getStructureWithPieceAt(player.blockPosition().below(), getResourceKey());

            return Set.of(structureStart);
        }

        public boolean isValid() { return !structureKey().equals(Loadable.EMPTY_KEY); }

        @Override
        public StructureInput load(String data)
        {
            this.structureKey = ResourceLocation.tryParse(data);
            if (structureKey == null) throw new RuntimeException(String.format("Invalid key for structure: %s", data));
            return this;
        }

        @Override
        public String save() { return structureKey.toString(); }

    }

    public static class Tag extends StructureInput
    {
        protected TagKey<Structure> tag;

        public Tag(TagKey<Structure> tag) { this.tag = tag; }

        public static Tag empty() { return new Tag(TagKey.create(Registries.STRUCTURE, Loadable.EMPTY_KEY)); }

        public static Tag tryLoad(String data)
        {
            return empty().loadWithFallback(data) instanceof Tag tag ? tag : empty();
        }

        public TagKey<Structure> tag() { return tag; }

        @Override
        public Set<StructureStart> getStructureStarts(ServerPlayer player)
        {
            StructureManager structureManager = PeritiaHelper.getStructureManager(player);
            StructureStart structureStart = structureManager.getStructureWithPieceAt(player.blockPosition().below(), tag());

            return Set.of(structureStart);
        }

        public boolean isValid() { return !tag().location().equals(Loadable.EMPTY_KEY); }

        @Override
        public StructureInput load(String data)
        {
            String rawString = data.substring(1);
            ResourceLocation tagKey = ResourceLocation.tryParse(rawString);
            if (tagKey == null) throw new RuntimeException(String.format("Invalid key for structure tag: %s", rawString));

            this.tag = TagKey.create(Registries.STRUCTURE, tagKey);
            return this;
        }

        @Override
        public String save() { return "#" + tag().location(); }

    }

}
