package github.poscard8.peritia.event;

import com.mojang.brigadier.CommandDispatcher;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.ascension.ServerAscensionSystem;
import github.poscard8.peritia.command.PeritiaCommand;
import github.poscard8.peritia.command.SkillCommand;
import github.poscard8.peritia.enchantment.AttributeEnchantment;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.clientbound.GameContextPacket;
import github.poscard8.peritia.network.packet.clientbound.InstructionsPacket;
import github.poscard8.peritia.network.packet.clientbound.LookContextPacket;
import github.poscard8.peritia.registry.PeritiaAttributes;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.skill.ItemLock;
import github.poscard8.peritia.skill.data.ServerSkillData;
import github.poscard8.peritia.util.PeritiaHelper;
import github.poscard8.peritia.util.forge.PeritiaConfigHelper;
import github.poscard8.peritia.util.skill.SkillRequisites;
import github.poscard8.peritia.util.text.PeritiaTexts;
import github.poscard8.peritia.xpsource.GuiXpSource;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.data.ServerXpSourceData;
import github.poscard8.peritia.xpsource.type.*;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Mod.EventBusSubscriber
@SuppressWarnings("unused")
public class CommonEvents
{
    @SubscribeEvent
    static void addReloadListeners(AddReloadListenerEvent event)
    {
        Peritia.resourceLoader().setServerSide();
        Peritia.resourceLoader().onResourceReload(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    static void registerCommands(RegisterCommandsEvent event)
    {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        PeritiaCommand.register(dispatcher);
        SkillCommand.register(dispatcher);
    }

    @SubscribeEvent
    static void onServerStart(ServerAboutToStartEvent event)
    {
        MinecraftServer server = event.getServer();

        PeritiaConfigHelper.clearMap();
        ModList.get().forEachModContainer((modId, modContainer) -> PeritiaConfigHelper.validate(modContainer));

        PeritiaHelper.setServer(server);
        Peritia.resourceLoader().setServer(server);
        ServerXpSourceData.getOrCreateFile(server);
        ServerAscensionSystem.getOrCreateFile(server);
    }

    @SubscribeEvent
    static void onServerStop(ServerStoppingEvent event)
    {
        PeritiaHelper.setServer(null);
        Peritia.resourceLoader().setServer(null);
    }

    @SubscribeEvent
    static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        PeritiaHelper.executeOnServer(event.getEntity(), player ->
        {
            String playerName = player.getName().getString();
            MinecraftServer server = player.getServer();
            if (server == null) return;

            Peritia.resourceLoader().sendResourcesToClient(player);

            PeritiaNetworkHandler.sendToClient(new GameContextPacket(server), player);
            Peritia.LOGGER.info("Loaded game context for player {}", playerName);

            ServerAscensionSystem.of(player.getServer()).update(player);
            Peritia.LOGGER.info("Loaded ascension system for player {}", playerName);

            ServerSkillData.of(player).update();
            Peritia.LOGGER.info("Loaded skill data of player {}", playerName);

            ServerXpSourceData.of(player.getServer()).update(Set.of(player));
            Peritia.LOGGER.info("Loaded xp source data of player {}", playerName);

            if (ServerSkillData.of(player).isNew() && XpSource.canPlayerGainXp(player))
            {
                PeritiaNetworkHandler.sendToClient(InstructionsPacket.simple(), player);
                Peritia.LOGGER.info("Sent instructions to player {}", playerName);
            }

            List<LoginXpSource> loginXpSources = Peritia.xpSourceHandler().byType(PeritiaXpSourceTypes.LOGIN);
            for (LoginXpSource xpSource : loginXpSources) xpSource.handlePlayerLogin(player);
        });
    }

    @SubscribeEvent
    static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        PeritiaHelper.executeOnServer(event.player, player ->
        {
            List<StructureXpSource> structureXpSources = Peritia.xpSourceHandler().byType(PeritiaXpSourceTypes.STRUCTURE);
            for (StructureXpSource xpSource : structureXpSources) xpSource.handlePlayerTick(player);

            MinecraftServer server = Objects.requireNonNull(player.getServer());
            int ticks = server.getTickCount();

            if (ticks % 3 == 0)
            {
                @Nullable ServerPlayer lookedAt = PeritiaHelper.getLookedAtPlayer(player);
                if (lookedAt != null)
                {
                    PeritiaNetworkHandler.sendToClient(new LookContextPacket(lookedAt), player);
                }
                else PeritiaNetworkHandler.sendToClient(new LookContextPacket(), player);
            }

            if (ticks % 100 == 0 && !player.containerMenu.stillValid(player)) player.inventoryMenu.sendAllDataToRemote();
        });
    }

    @SubscribeEvent
    static void onServerTick(TickEvent.ServerTickEvent event)
    {
        MinecraftServer server = event.getServer();
        int ticks = server.getTickCount();

        if (ticks % 20 == 0)
        {
            List<SocialXpSource> socialXpSources = Peritia.xpSourceHandler().byType(PeritiaXpSourceTypes.SOCIAL);
            for (SocialXpSource xpSource : socialXpSources) xpSource.handleServerTick(server);
        }
    }

    @SubscribeEvent
    static void onPlayerInteract(PlayerInteractEvent event)
    {
        if (!event.isCancelable()) return;

        PeritiaHelper.executeOnServer(event.getEntity(), player ->
        {
            if (!ItemLock.lockedForPlayer(player)) return;

            ServerSkillData skillData = ServerSkillData.of(player);
            Item item = event.getItemStack().getItem();
            SkillRequisites requisites = ItemLock.getRequisitesFor(item);

            if (!requisites.testForLockedItem(skillData))
            {
                List<Component> warningTexts = PeritiaTexts.$requisitesForLockedItem(requisites, skillData, true);
                if (!warningTexts.isEmpty()) player.displayClientMessage(warningTexts.get(0), true);

                event.setCanceled(true);
            }
        });
    }

    @SubscribeEvent
    static void onPlayerAttack(AttackEntityEvent event)
    {
        PeritiaHelper.executeOnServer(event.getEntity(), player ->
        {
            if (!ItemLock.lockedForPlayer(player)) return;

            ServerSkillData skillData = ServerSkillData.of(player);
            Item item = player.getMainHandItem().getItem();
            SkillRequisites requisites = ItemLock.getRequisitesFor(item);

            if (!requisites.testForLockedItem(skillData))
            {
                List<Component> warningTexts = PeritiaTexts.$requisitesForLockedItem(requisites, skillData, true);
                if (!warningTexts.isEmpty()) player.displayClientMessage(warningTexts.get(0), true);

                event.setCanceled(true);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    static void onCriticalHit(CriticalHitEvent event)
    {
        PeritiaHelper.executeOnServer(event.getEntity(), player ->
        {
            double critDamage = player.getAttributeValue(PeritiaAttributes.CRIT_DAMAGE.get());
            double modifier = 1 + critDamage / 100;

            event.setDamageModifier((float) modifier);

            if (!event.isVanillaCritical())
            {
                double extraCritChance = player.getAttributeValue(PeritiaAttributes.EXTRA_CRIT_CHANCE.get()) / 100;
                if (new Random().nextFloat() < extraCritChance) event.setResult(Event.Result.ALLOW);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    static void setBreakSpeed(PlayerEvent.BreakSpeed event)
    {
        PeritiaHelper.executeOnServer(event.getEntity(), player ->
        {
            double multiplier = player.getAttributeValue(PeritiaAttributes.BLOCK_BREAK_SPEED.get());
            event.setNewSpeed((float) (event.getNewSpeed() * multiplier));
        });
    }

    @SubscribeEvent
    static void onEntityDeath(LivingDeathEvent event)
    {
        Entity killer = event.getSource().getEntity();
        Entity entity = event.getEntity();

        PeritiaHelper.executeOnServer(killer, player ->
        {
            List<EntityXpSource> entityXpSources = Peritia.xpSourceHandler().byType(PeritiaXpSourceTypes.ENTITY);
            for (EntityXpSource xpSource : entityXpSources) xpSource.handleEntityKill(player, entity);
        });
    }

    @SubscribeEvent
    static void onBlockBreak(BlockEvent.BreakEvent event)
    {
        PeritiaHelper.executeOnServer(event.getPlayer(), player ->
        {
            BlockState state = event.getState();

            List<BlockXpSource> blockXpSources = Peritia.xpSourceHandler().byType(PeritiaXpSourceTypes.BLOCK);
            for (BlockXpSource xpSource : blockXpSources) xpSource.handleBlockBreak(player, state);

            BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());
            if (blockEntity instanceof RandomizableContainerBlockEntity randomizable)
            {
                CompoundTag nbt = randomizable.getPersistentData();
                nbt.putUUID("brokenBy", player.getUUID());

                randomizable.setChanged();
            }
        });
    }

    @SubscribeEvent
    static void onBlockPlace(BlockEvent.EntityPlaceEvent event)
    {
        PeritiaHelper.executeOnServer(event.getEntity(), player ->
        {
            BlockState state = event.getState();

            List<BlockXpSource> blockXpSources = Peritia.xpSourceHandler().byType(PeritiaXpSourceTypes.BLOCK);
            for (BlockXpSource xpSource : blockXpSources) xpSource.handleBlockPlace(player, state);
        });
    }

    @SubscribeEvent
    static void onContainerClose(PlayerContainerEvent.Close event)
    {
        PeritiaHelper.executeOnServer(event.getEntity(), player ->
        {
            List<GuiXpSource> guiXpSources = Peritia.xpSourceHandler().guiXpSources();
            for (GuiXpSource xpSource : guiXpSources) xpSource.tryAwardAndClear(player);
        });
    }

    @SubscribeEvent
    static void onVillagerTrade(TradeWithVillagerEvent event)
    {
        ServerPlayer player = (ServerPlayer) event.getEntity();

        if (event.getAbstractVillager() instanceof Villager villager)
        {
            VillagerProfession profession = villager.getVillagerData().getProfession();
            ResourceLocation professionKey = ForgeRegistries.VILLAGER_PROFESSIONS.getKey(profession);
            ItemStack result = event.getMerchantOffer().getResult().copy();

            List<TradeXpSource> tradeXpSources = Peritia.xpSourceHandler().byType(PeritiaXpSourceTypes.TRADE);
            for (TradeXpSource xpSource : tradeXpSources) xpSource.handleTrade(player, professionKey, result);
        }
    }

    @SubscribeEvent
    static void onAdvancementEarn(AdvancementEvent.AdvancementEarnEvent event)
    {
        PeritiaHelper.executeOnServer(event.getEntity(), player ->
        {
            ResourceLocation key = event.getAdvancement().getId();

            List<AdvancementXpSource> advancementXpSources = Peritia.xpSourceHandler().byType(PeritiaXpSourceTypes.ADVANCEMENT);
            for (AdvancementXpSource xpSource : advancementXpSources) xpSource.handleEarn(player, key);
        });
    }

    @SubscribeEvent
    static void onAnvilRepair(AnvilRepairEvent event)
    {
        PeritiaHelper.executeOnServer(event.getEntity(), player ->
        {
            ItemStack left = event.getLeft();
            ItemStack right = event.getRight();

            List<EnchantmentXpSource> enchantmentXpSources = Peritia.xpSourceHandler().byType(PeritiaXpSourceTypes.ENCHANTMENT);
            for (EnchantmentXpSource xpSource : enchantmentXpSources) xpSource.handleItem(player, right);

            List<AnvilXpSource> anvilXpSources = Peritia.xpSourceHandler().byType(PeritiaXpSourceTypes.ANVIL);
            for (AnvilXpSource xpSource : anvilXpSources) xpSource.handleItems(player, left, right);

            ItemStack output = event.getOutput().copy();
            if (output.isEnchanted()) CriteriaTriggers.ENCHANTED_ITEM.trigger(player, output, 1);
        });
    }

    @SubscribeEvent
    static void onItemFish(ItemFishedEvent event)
    {
        PeritiaHelper.executeOnServer(event.getEntity(), player ->
        {
            NonNullList<ItemStack> drops = event.getDrops();

            List<FishXpSource> fishXpSources = Peritia.xpSourceHandler().byType(PeritiaXpSourceTypes.FISH);
            for (FishXpSource xpSource : fishXpSources) xpSource.handleFish(player, drops);
        });
    }

    @SubscribeEvent
    static void onItemUseFinish(LivingEntityUseItemEvent.Finish event)
    {
        PeritiaHelper.executeOnServer(event.getEntity(), player ->
        {
            ItemStack stack = event.getItem();

            List<ConsumeXpSource> consumeXpSources = Peritia.xpSourceHandler().byType(PeritiaXpSourceTypes.CONSUME);
            for (ConsumeXpSource xpSource : consumeXpSources) xpSource.handleConsume(player, stack);
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    static void addItemAttributes(ItemAttributeModifierEvent event)
    {
        ItemStack stack = event.getItemStack();
        Map<Enchantment, Integer> levelMap = EnchantmentHelper.getEnchantments(stack);
        if (stack.getItem() instanceof EnchantedBookItem || event.getSlotType() != EquipmentSlot.MAINHAND) return;

        for (Enchantment enchantment : levelMap.keySet())
        {
            if (enchantment instanceof AttributeEnchantment attributeEnchantment)
            {
                int level = levelMap.get(enchantment);
                event.addModifier(attributeEnchantment.attribute(), attributeEnchantment.getAttributeModifier(level));
            }
        }
    }

}
