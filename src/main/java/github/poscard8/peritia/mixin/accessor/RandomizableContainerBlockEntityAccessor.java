package github.poscard8.peritia.mixin.accessor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RandomizableContainerBlockEntity.class)
@SuppressWarnings("ALL")
public interface RandomizableContainerBlockEntityAccessor
{
    @Accessor("lootTable")
    @Nullable
    public ResourceLocation getLootTable();

}
