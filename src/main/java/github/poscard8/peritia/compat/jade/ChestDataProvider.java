package github.poscard8.peritia.compat.jade;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.mixin.accessor.RandomizableContainerBlockEntityAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public class ChestDataProvider implements IServerDataProvider<BlockAccessor>
{
    public static final ResourceLocation ID = Peritia.asResource("chest_data_provider");
    protected static final String NBT_KEY = "peritia_loot_table";

    @Override
    public void appendServerData(CompoundTag nbt, BlockAccessor blockAccessor)
    {
        if (blockAccessor.getLevel().getBlockEntity(blockAccessor.getPosition()) instanceof RandomizableContainerBlockEntity blockEntity)
        {
            @Nullable ResourceLocation lootTableKey = ((RandomizableContainerBlockEntityAccessor) blockEntity).getLootTable();
            if (lootTableKey != null) nbt.putString(NBT_KEY, lootTableKey.toString());
        }
    }

    @Override
    public ResourceLocation getUid() { return ID; }

}
