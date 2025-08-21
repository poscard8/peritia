package github.poscard8.peritia.event;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.client.PeritiaKeyMappings;
import github.poscard8.peritia.network.ClientHandler;
import github.poscard8.peritia.network.PeritiaClientHandler;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.serverbound.BrewPotionPacket;
import github.poscard8.peritia.network.packet.serverbound.OpenMenuPacket;
import github.poscard8.peritia.network.packet.serverbound.UseGrindstonePacket;
import github.poscard8.peritia.registry.PeritiaAttributes;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.skill.ItemLock;
import github.poscard8.peritia.skill.data.SkillData;
import github.poscard8.peritia.util.skill.SkillRequisites;
import github.poscard8.peritia.util.text.PeritiaTexts;
import github.poscard8.peritia.xpsource.type.GrindstoneXpSource;
import github.poscard8.peritia.xpsource.type.PotionXpSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.brewing.PotionBrewEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
@SuppressWarnings("unused")
public class ClientEvents
{
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        Player player = event.getEntity();
        Player localPlayer = Minecraft.getInstance().player;

        if (player != null && localPlayer != null && player.getStringUUID().equals(localPlayer.getStringUUID()))
        {
            Peritia.LOGGER.info("Reloaded client handler for player {}", player.getName().getString());
            PeritiaClientHandler.reload();
        }
    }

    @SubscribeEvent
    static void onClientTick(TickEvent.PlayerTickEvent event)
    {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
        {
            if (event.side.isClient()) PeritiaClientHandler.getInstance().tick();
            while (PeritiaKeyMappings.MAIN_MENU.consumeClick()) PeritiaNetworkHandler.sendToServer(OpenMenuPacket.mainMenu());
        });
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    static void setBreakSpeed(PlayerEvent.BreakSpeed event)
    {
        Player player = event.getEntity();

        if (player.isLocalPlayer())
        {
            double multiplier = ClientHandler.getAttributeMap().valueOf(PeritiaAttributes.BLOCK_BREAK_SPEED.get());
            event.setNewSpeed((float) (event.getNewSpeed() * multiplier));
        }
    }

    @SubscribeEvent
    static void onGrindstoneUse(GrindstoneEvent.OnTakeItem event)
    {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
        {
            ItemStack top = event.getTopItem();
            ItemStack bottom = event.getBottomItem();

            List<GrindstoneXpSource> grindstoneXpSources = Peritia.xpSourceHandler().byType(PeritiaXpSourceTypes.GRINDSTONE);
            for (GrindstoneXpSource xpSource : grindstoneXpSources)
            {
                int multiplier = xpSource.evaluateItems(top, bottom);
                PeritiaNetworkHandler.sendToServer(new UseGrindstonePacket(xpSource, multiplier));
            }
        });
    }

    @SubscribeEvent
    static void onPotionBrew(PotionBrewEvent.Pre event)
    {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
        {
            ItemStack ingredient = event.getItem(3);

            List<PotionXpSource> potionXpSources = Peritia.xpSourceHandler().byType(PeritiaXpSourceTypes.POTION);
            for (PotionXpSource xpSource : potionXpSources)
            {
                if (xpSource.isValidItem(ingredient)) PeritiaNetworkHandler.sendToServer(new BrewPotionPacket(xpSource));
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void addItemTooltips(ItemTooltipEvent event)
    {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
        {
            Player player = event.getEntity();
            if (player != null && ItemLock.lockedForPlayer(player))
            {
                SkillRequisites requisites = ItemLock.getRequisitesFor(event.getItemStack().getItem());
                SkillData skillData = ClientHandler.getSkillData();

                List<Component> texts = event.getToolTip();
                texts.addAll(PeritiaTexts.$requisitesForLockedItem(requisites, skillData, false));
            }
        });
    }

}
