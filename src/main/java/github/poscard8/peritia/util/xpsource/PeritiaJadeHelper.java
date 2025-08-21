package github.poscard8.peritia.util.xpsource;

import github.poscard8.peritia.config.PeritiaServerConfig;
import github.poscard8.peritia.network.ClientHandler;
import github.poscard8.peritia.registry.PeritiaAttributes;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.data.ClientSkillData;
import github.poscard8.peritia.util.skill.SkillFunction;
import github.poscard8.peritia.util.skill.XpGainContext;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.type.BlockXpSource;
import github.poscard8.peritia.xpsource.type.ChestXpSource;
import github.poscard8.peritia.xpsource.type.EntityXpSource;
import net.minecraft.world.entity.Entity;

public class PeritiaJadeHelper
{
    static ClientSkillData clientSkillData() { return ClientHandler.getSkillData(); }

    static int getTrueXp(XpSource xpSource) { return getTrueXp(xpSource.xp()); }

    static int getTrueXp(int xp)
    {
        return (int) Math.round(xp * PeritiaServerConfig.UNIVERSAL_XP_MULTIPLIER.get() * ((ClientHandler.getAttributeMap().valueOf(PeritiaAttributes.WISDOM.get()) / 100.0D) + 1));
    }

    public static XpGainContext makeContext(BlockXpSource xpSource, boolean correctTool)
    {
        boolean blocked = xpSource.getDebtClient() > 0 || !correctTool;

        XpGainContext context = new XpGainContext(getTrueXp(xpSource));
        context.setForUI(true);
        context.setRandom(xpSource.skillFunction() == SkillFunction.Special.RANDOM);
        context.setBlocked(blocked);

        for (Skill skill : xpSource.skillFunction().getSkills(clientSkillData())) context.add(skill);
        return context;
    }

    public static XpGainContext makeContext(EntityXpSource xpSource, Entity entity)
    {
        float sourceMultiplier = xpSource.getContextClient().getMultiplier(entity);
        float gearMultiplier = xpSource.gearFunction().getMultiplier(entity);

        int initialXp = xpSource.isFarmingAllowed() ?
                Math.round(xpSource.xp() * gearMultiplier) :
                Math.round(xpSource.xp() * gearMultiplier * sourceMultiplier);

        XpGainContext context = new XpGainContext(getTrueXp(initialXp));
        context.setForUI(true);
        context.setRandom(xpSource.skillFunction() == SkillFunction.Special.RANDOM);

        for (Skill skill : xpSource.skillFunction().getSkills(clientSkillData())) context.add(skill);
        return context;
    }

    public static XpGainContext makeContext(ChestXpSource xpSource)
    {
        XpGainContext context = new XpGainContext(getTrueXp(xpSource));
        context.setForUI(true);
        context.setRandom(xpSource.skillFunction() == SkillFunction.Special.RANDOM);

        for (Skill skill : xpSource.skillFunction().getSkills(clientSkillData())) context.add(skill);
        return context;
    }

}
