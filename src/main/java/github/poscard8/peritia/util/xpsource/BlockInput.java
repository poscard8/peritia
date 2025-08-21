package github.poscard8.peritia.util.xpsource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.poscard8.peritia.util.serialization.ElementSerializable;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.Loadable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public abstract class BlockInput implements Input<BlockState>, ElementSerializable<BlockInput>
{
    public static BlockInput empty() { return Single.empty(); }

    public static BlockInput tryLoad(JsonElement data)
    {
        if (data.isJsonPrimitive() && data.getAsJsonPrimitive().isString())
        {
            String string = data.getAsString();
            if (string.startsWith("#")) return Tag.tryLoad(data);
        }
        return Single.tryLoad(data);
    }

    public boolean testForDisplay(Block block) { return test(block.defaultBlockState()); }

    @Override
    public BlockInput fallback() { return empty(); }


    public static class Single extends BlockInput
    {
        protected Block block;
        protected PropertyPredicates properties;

        public Single(Block block, PropertyPredicates properties)
        {
            this.block = block;
            this.properties = properties;
        }

        public static Single empty() { return new Single(Blocks.AIR, PropertyPredicates.empty()); }

        public static Single tryLoad(JsonElement data)
        {
            return empty().loadWithFallback(data) instanceof Single single ? single : empty();
        }

        public Block block() { return block; }

        public PropertyPredicates properties() { return properties; }

        @Override
        public boolean isValid() { return block() != null && Objects.requireNonNull(block()) != Blocks.AIR; }

        @Override
        public boolean test(BlockState state) { return state.getBlock() == block() && properties().test(state); }

        @Override
        public boolean testForDisplay(Block block) { return block == block(); }

        @Override
        public BlockInput load(JsonElement data)
        {
            if (data.isJsonObject())
            {
                JsonObject jsonObject = data.getAsJsonObject();

                this.block = JsonHelper.readRegistrable(jsonObject, "block", ForgeRegistries.BLOCKS, block);
                this.properties = JsonHelper.readArraySerializable(jsonObject, "properties", PropertyPredicates::tryLoad, properties);
            }
            else
            {
                String string = data.getAsString();
                ResourceLocation key = ResourceLocation.tryParse(string);
                if (key == null) throw new RuntimeException(String.format("Invalid key for block: %s", string));

                Block block = ForgeRegistries.BLOCKS.getValue(key);
                if (block == null) throw new RuntimeException(String.format("Invalid key for block: %s", string));

                this.block = block;
            }
            return this;
        }

        @Override
        public JsonElement save()
        {
            if (properties.isEmpty())
            {
                return new JsonPrimitive(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block())).toString());
            }
            else
            {
                JsonObject data = new JsonObject();
                JsonHelper.write(data, "block", block, ForgeRegistries.BLOCKS);
                JsonHelper.write(data, "properties", properties);

                return data;
            }
        }

    }

    public static class Tag extends BlockInput
    {
        protected TagKey<Block> tag;

        public Tag(TagKey<Block> tag) { this.tag = tag; }

        public static Tag empty() { return new Tag(TagKey.create(ForgeRegistries.Keys.BLOCKS, Loadable.EMPTY_KEY)); }

        public static Tag tryLoad(JsonElement data)
        {
            return empty().loadWithFallback(data) instanceof Tag tag ? tag : empty();
        }

        public TagKey<Block> tag() { return tag; }

        @Override
        public boolean isValid() { return !tag().location().equals(Loadable.EMPTY_KEY); }

        @Override
        public boolean test(BlockState state) { return state.is(tag()); }

        @Override
        public BlockInput load(JsonElement data)
        {
            String string = data.getAsString();
            String rawString = string.substring(1);
            ResourceLocation tagKey = ResourceLocation.tryParse(rawString);
            if (tagKey == null) throw new RuntimeException(String.format("Invalid key for block tag: %s", rawString));

            this.tag = TagKey.create(ForgeRegistries.Keys.BLOCKS, tagKey);
            return this;
        }

        @Override
        public JsonElement save() { return new JsonPrimitive("#" + tag.location()); }

    }

}
