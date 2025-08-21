package github.poscard8.peritia.mixin;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.clientbound.ChestLuckPacket;
import github.poscard8.peritia.registry.PeritiaAttributes;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.util.PeritiaHelper;
import github.poscard8.peritia.util.PeritiaTags;
import github.poscard8.peritia.xpsource.type.ChestXpSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Mixin(AbstractMinecartContainer.class)
@SuppressWarnings("ALL")
public abstract class AbstractMinecartContainerMixin
{
    private static final Map<Integer, ResourceLocation> HASH_MAP = new HashMap<>();
    private static final Random RANDOM = new Random();

    @Shadow
    @Nullable
    public abstract ResourceLocation getLootTable();

    AbstractMinecartContainer self = (AbstractMinecartContainer) (Object) this;

    @Inject(method = "createMenu", at = @At("HEAD"))
    void peritia$createMenu(int id, Inventory inventory, Player player0, CallbackInfoReturnable<AbstractContainerMenu> ci)
    {
        ResourceLocation lootTable = getLootTable();
        if (lootTable != null) HASH_MAP.put(self.hashCode(), lootTable);
    }

    @Inject(method = "createMenu", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    void peritia$createMenu2(int id, Inventory inventory, Player player0, CallbackInfoReturnable<AbstractContainerMenu> ci)
    {
        int hash = self.hashCode();
        ResourceLocation lootTableKey = HASH_MAP.get(hash);
        MinecraftServer server = PeritiaHelper.getServer();

        if (lootTableKey == null || server == null || !(player0 instanceof ServerPlayer player)) return;

        List<ChestXpSource> chestXpSources = Peritia.xpSourceHandler().byType(PeritiaXpSourceTypes.CHEST);
        for (ChestXpSource xpSource : chestXpSources) xpSource.handleChestOpen(player, lootTableKey, false);

        LootTable lootTable = server.getLootData().getLootTable(lootTableKey);
        LootParams lootParams = new LootParams.Builder((ServerLevel) player.level())
                .withParameter(LootContextParams.ORIGIN, self.position())
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.KILLER_ENTITY, self)
                .withLuck(player.getLuck())
                .create(LootContextParamSets.CHEST);

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

        if (rolls > 1) PeritiaNetworkHandler.sendToClient(new ChestLuckPacket(Items.CHEST_MINECART, rolls), player);

        ItemStack stack = player.getMainHandItem();
        if (stack.is(PeritiaTags.KEYS)) stack.shrink(1);

        HASH_MAP.remove(hash);
    }



}
