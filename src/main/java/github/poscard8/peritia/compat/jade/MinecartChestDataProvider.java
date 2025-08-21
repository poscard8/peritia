package github.poscard8.peritia.compat.jade;

import github.poscard8.peritia.Peritia;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.MinecartChest;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

public class MinecartChestDataProvider implements IServerDataProvider<EntityAccessor>
{
    public static final ResourceLocation ID = Peritia.asResource("minecart_chest_data_provider");
    protected static final String NBT_KEY = "peritia_loot_table";

    @Override
    public void appendServerData(CompoundTag nbt, EntityAccessor entityAccessor)
    {
        if (entityAccessor.getEntity() instanceof MinecartChest minecartChest)
        {
            @Nullable ResourceLocation lootTableKey = minecartChest.getLootTable();
            if (lootTableKey != null) nbt.putString(NBT_KEY, lootTableKey.toString());
        }
    }

    @Override
    public ResourceLocation getUid() { return ID; }

}
