package github.poscard8.peritia.compat.jade;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.network.ClientHandler;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.skill.data.SkillData;
import github.poscard8.peritia.util.text.PeritiaTexts;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.type.BlockXpSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class PeritiaBlockTextProvider implements IBlockComponentProvider
{
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig config)
    {
        if (!config.get(PeritiaJadePlugin.BLOCK_XP_SOURCES)) return;

        Player player = blockAccessor.getPlayer();
        SkillData skillData = ClientHandler.getSkillData();
        if (player == null || !XpSource.canPlayerGainXp(player)) return;

        ItemStack tool = player.getMainHandItem();
        BlockState state = blockAccessor.getBlockState();

        for (BlockXpSource xpSource : Peritia.xpSourceHandler().byType(PeritiaXpSourceTypes.BLOCK))
        {
            if (xpSource.inputs().test(blockAccessor.getBlockState()))
            {
                if (xpSource.shouldShow(player, skillData))
                {
                    boolean correctTool = xpSource.isCorrectTool(tool, state);
                    tooltip.addAll(PeritiaTexts.$blockInfo(xpSource, xpSource.getDebtClient(), correctTool));
                }
                else tooltip.add(PeritiaTexts.mayGiveXp());
                break;
            }
        }
    }

    @Override
    public ResourceLocation getUid() { return PeritiaJadePlugin.BLOCK_XP_SOURCES; }

    @Override
    public int getDefaultPriority() { return 1000; }


}
