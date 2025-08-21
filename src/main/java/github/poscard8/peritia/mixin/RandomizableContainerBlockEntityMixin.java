package github.poscard8.peritia.mixin;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.clientbound.ChestLuckPacket;
import github.poscard8.peritia.registry.PeritiaAttributes;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.util.PeritiaHelper;
import github.poscard8.peritia.util.PeritiaTags;
import github.poscard8.peritia.xpsource.type.ChestXpSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Trigger for {@link ChestExperienceSource} and enables chest luck.
 * <p>+7 Chest Luck: %7 chance to get double the loot.</p>
 * <p>+111 Chest Luck: %11 chance to get triple the loot, %89 chance to get double the loot.</p>
 */
@Mixin(RandomizableContainerBlockEntity.class)
@SuppressWarnings("ALL")
public abstract class RandomizableContainerBlockEntityMixin
{
    private static final String TARGET = "Lnet/minecraft/world/level/storage/loot/LootTable;fill(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/storage/loot/LootParams;J)V";
    private static final Random RANDOM = new Random();

    RandomizableContainerBlockEntity self = (RandomizableContainerBlockEntity) (Object) this;

    @Inject(method = "unpackLootTable", at = @At(value = "INVOKE", target = TARGET, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    void peritia$unpackLootTable(@Nullable Player player0, CallbackInfo ci, LootTable lootTable, LootParams.Builder lootParams$builder)
    {
        @Nullable ServerPlayer player = null;
        boolean awardNow = false;

        if (player0 instanceof ServerPlayer)
        {
            player = (ServerPlayer) player0;
        }
        else
        {
            MinecraftServer server = PeritiaHelper.getServer();
            if (server != null)
            {
                UUID playerUuid = self.getPersistentData().getUUID("brokenBy");
                player = server.getPlayerList().getPlayer(playerUuid);
                awardNow = true;
            }
        }
        if (player == null) return;

        List<ChestXpSource> chestXpSources = Peritia.xpSourceHandler().byType(PeritiaXpSourceTypes.CHEST);
        for (ChestXpSource xpSource : chestXpSources) xpSource.handleChestOpen(player, lootTable.getLootTableId(), awardNow);

        LootParams lootParams = lootParams$builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player).create(LootContextParamSets.CHEST);

        int chestLuck = (int) player.getAttribute(PeritiaAttributes.CHEST_LUCK.get()).getValue();
        int setRolls = chestLuck / 100;
        int extraRollChance = chestLuck % 100;
        int rolls = setRolls + 1;

        SimpleContainer added = new SimpleContainer(27);

        for (int i = 0; i < setRolls; i++)
        {
            lootTable.fill(added, lootParams, RANDOM.nextLong());
            PeritiaHelper.addContainerToContainer(added, self);
            added.removeAllItems();
        }

        int randomInt = RANDOM.nextInt(100);

        if (randomInt < extraRollChance)
        {
            lootTable.fill(added, lootParams, RANDOM.nextLong());
            PeritiaHelper.addContainerToContainer(added, self);
            added.removeAllItems();
            rolls++;
        }

        @Nullable Item icon = self.getBlockState().getBlock().asItem();
        if (rolls > 1) PeritiaNetworkHandler.sendToClient(new ChestLuckPacket(icon, rolls), player);

        ItemStack stack = player.getMainHandItem();
        if (stack.is(PeritiaTags.KEYS)) stack.shrink(1);
    }

}