package github.poscard8.peritia.util;

import github.poscard8.peritia.Peritia;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class PeritiaTags
{
    public static final TagKey<Item> KEYS = item("keys");

    static TagKey<Item> item(String name) { return TagKey.create(ForgeRegistries.Keys.ITEMS, Peritia.asResource(name)); }

}
