package github.poscard8.peritia.xpsource;

import github.poscard8.peritia.skill.data.ServerSkillData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public abstract class GuiXpSource extends XpSource
{
    protected final Map<ServerPlayer, Integer> waitingXpMap = new HashMap<>();

    public GuiXpSource(ResourceLocation key) { super(key); }

    public int getWaitingXp(ServerPlayer player) { return waitingXpMap.getOrDefault(player, 0); }

    public boolean hasWaitingXp(ServerPlayer player) { return getWaitingXp(player) > 0; }

    public void setWaitingXp(ServerPlayer player, int xp)
    {
        if (canPlayerGainXp(player)) waitingXpMap.put(player, Math.max(0, xp));
    }

    public void addWaitingXp(ServerPlayer player) { addWaitingXp(player, 1); }

    public void addWaitingXp(ServerPlayer player, int multiplier) { setWaitingXp(player, getWaitingXp(player) + (xp() * multiplier)); }

    public void tryAwardAndClear(ServerPlayer player)
    {
        if (hasWaitingXp(player) && canPlayerGainXp(player))
        {
            award(player);
            setWaitingXp(player, 0);
        }
    }

    @Override
    public void award(ServerPlayer player) { ServerSkillData.of(player).addXpToSkills(skillFunction(), this, getWaitingXp(player)); }

    @Override
    public void award(ServerPlayer player, float multiplier) { award(player); }

}
