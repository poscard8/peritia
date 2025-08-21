package github.poscard8.peritia.compat.jade;

import github.poscard8.peritia.xpsource.XpSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import java.util.function.Predicate;

import static github.poscard8.peritia.Peritia.asResource;

@WailaPlugin
public class PeritiaJadePlugin implements IWailaPlugin
{
    public static final ResourceLocation BLOCK_XP_SOURCES = asResource("block_xp_sources");
    public static final ResourceLocation ENTITY_XP_SOURCES = asResource("entity_xp_sources");
    public static final ResourceLocation CHEST_XP_SOURCES = asResource("chest_xp_sources");
    public static final ResourceLocation CHEST_XP_SOURCES_MINECART = asResource("chest_xp_sources_minecart");

    @Override
    public void register(IWailaCommonRegistration registration)
    {
        registration.registerBlockDataProvider(new ChestDataProvider(), RandomizableContainerBlockEntity.class);
        registration.registerEntityDataProvider(new MinecartChestDataProvider(), MinecartChest.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration)
    {
        registration.addConfig(BLOCK_XP_SOURCES, true);
        registration.addConfig(ENTITY_XP_SOURCES, true);
        registration.addConfig(CHEST_XP_SOURCES, true);
        registration.addConfig(CHEST_XP_SOURCES_MINECART, true);

        registration.markAsClientFeature(BLOCK_XP_SOURCES);
        registration.markAsClientFeature(ENTITY_XP_SOURCES);
        registration.markAsClientFeature(CHEST_XP_SOURCES);
        registration.markAsClientFeature(CHEST_XP_SOURCES_MINECART);

        registration.registerBlockComponent(new PeritiaBlockTextProvider(), Block.class);
        registration.registerEntityComponent(new PeritiaEntityTextProvider(), LivingEntity.class);
        registration.registerBlockComponent(new PeritiaChestTextProvider(), BaseEntityBlock.class);
        registration.registerEntityComponent(new PeritiaMinecartChestTextProvider(), MinecartChest.class);
    }


    public enum ConfigOption
    {
        ON(player -> false),
        OFF(player -> true),
        SURVIVAL_ONLY(player -> !XpSource.canPlayerGainXp(player));

        private final Predicate<Player> stopPredicate;

        ConfigOption(Predicate<Player> stopPredicate) { this.stopPredicate = stopPredicate; }

        public boolean shouldStopRendering(@Nullable Player player) { return player == null || stopPredicate.test(player); }

    }

}
