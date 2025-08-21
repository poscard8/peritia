package github.poscard8.peritia.compat.jade;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.network.ClientHandler;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.skill.data.ClientSkillData;
import github.poscard8.peritia.util.text.PeritiaTexts;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.type.ChestXpSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class PeritiaChestTextProvider implements IBlockComponentProvider
{
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig config)
    {
        if (!config.get(PeritiaJadePlugin.CHEST_XP_SOURCES)) return;

        Player player = blockAccessor.getPlayer();
        ClientSkillData skillData = ClientHandler.getSkillData();
        if (player == null || !XpSource.canPlayerGainXp(player)) return;

        CompoundTag nbt = blockAccessor.getServerData();
        String lootTableString = nbt.getString(ChestDataProvider.NBT_KEY);
        if (lootTableString.isEmpty()) return;

        ResourceLocation lootTableKey = ResourceLocation.tryParse(lootTableString);
        if (lootTableKey == null) return;

        for (ChestXpSource xpSource : Peritia.xpSourceHandler().byType(PeritiaXpSourceTypes.CHEST))
        {
            if (xpSource.inputs().test(lootTableKey))
            {
                if (xpSource.shouldShow(player, skillData))
                {
                    tooltip.addAll(PeritiaTexts.$chestInfo(xpSource, true));
                }
                else tooltip.add(PeritiaTexts.mayGiveXp());
                break;
            }
        }
    }

    @Override
    public ResourceLocation getUid() { return PeritiaJadePlugin.CHEST_XP_SOURCES; }

    @Override
    public int getDefaultPriority() { return 5000; }


}
