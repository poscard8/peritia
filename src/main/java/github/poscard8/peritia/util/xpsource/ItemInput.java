package github.poscard8.peritia.util.xpsource;

import github.poscard8.peritia.util.serialization.Loadable;
import github.poscard8.peritia.util.serialization.StringSerializable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public abstract class ItemInput implements Input<ItemStack>, StringSerializable<ItemInput>
{
    public static ItemInput empty() { return Single.empty(); }

    public static ItemInput tryLoad(String data)
    {
        if (data.startsWith("#")) return Tag.tryLoad(data);
        return Single.tryLoad(data);
    }

    @Override
    public ItemInput fallback() { return empty(); }


    public static class Single extends ItemInput
    {
        protected Item item;

        public Single(Item item) { this.item = item; }

        public static Single empty() { return new Single(Items.AIR); }

        public static Single tryLoad(String data)
        {
            return empty().loadWithFallback(data) instanceof Single single ? single : empty();
        }

        public Item item() { return item; }

        @Override
        public boolean isValid() { return item() != null || Objects.requireNonNull(item()) != Items.AIR; }

        @Override
        public boolean test(ItemStack stack) { return stack.is(item()); }

        @Override
        public ItemInput load(String data)
        {
            ResourceLocation key = ResourceLocation.tryParse(data);
            if (key == null) throw new RuntimeException(String.format("Invalid key for item: %s", data));

            Item item = ForgeRegistries.ITEMS.getValue(key);
            if (item == null) throw new RuntimeException(String.format("Invalid key for item: %s", data));

            this.item = item;
            return this;
        }

        @Override
        public String save() { return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item())).toString(); }

    }

    public static class Tag extends ItemInput
    {
        protected TagKey<Item> tag;

        public Tag(TagKey<Item> tag) { this.tag = tag; }

        public static Tag empty() { return new Tag(TagKey.create(ForgeRegistries.Keys.ITEMS, Loadable.EMPTY_KEY)); }

        public static Tag tryLoad(String data)
        {
            return empty().loadWithFallback(data) instanceof Tag tag ? tag : empty();
        }

        public TagKey<Item> tag() { return tag; }

        @Override
        public boolean isValid() { return !tag().location().equals(Loadable.EMPTY_KEY); }

        @Override
        public boolean test(ItemStack stack) { return stack.is(tag()); }

        @Override
        public ItemInput load(String data)
        {
            String rawString = data.substring(1);
            ResourceLocation tagKey = ResourceLocation.tryParse(rawString);
            if (tagKey == null) throw new RuntimeException(String.format("Invalid key for item tag: %s", rawString));

            this.tag = TagKey.create(ForgeRegistries.Keys.ITEMS, tagKey);
            return this;
        }

        @Override
        public String save() { return "#" + tag().location(); }

    }

}
