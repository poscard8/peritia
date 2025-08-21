package github.poscard8.peritia.skill.data;

import com.google.gson.JsonObject;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.ascension.Legacy;
import github.poscard8.peritia.config.PeritiaServerConfig;
import github.poscard8.peritia.skill.HighScoreMap;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.SkillInstance;
import github.poscard8.peritia.skill.SkillMap;
import github.poscard8.peritia.util.minecraft.ResourceSet;
import github.poscard8.peritia.util.serialization.SerializableDate;
import github.poscard8.peritia.util.skill.ComparableWeightHolder;
import github.poscard8.peritia.xpsource.XpSource;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;

public abstract sealed class SkillData implements ComparableWeightHolder permits ServerSkillData, ClientSkillData
{
    protected SkillMap skillMap = SkillMap.empty();
    protected HighScoreMap highScoreMap = HighScoreMap.empty();
    protected Legacy legacy = Legacy.empty();
    protected ResourceSet discoveredXpSources = ResourceSet.empty();
    protected JsonObject additional = new JsonObject();
    protected int timePlayed = 0; // in seconds
    protected SerializableDate lastUpdate = new SerializableDate();
    protected SerializableDate lastAscended = new SerializableDate().add(Calendar.MINUTE, -1);

    protected SkillData() {}

    public SkillMap skillMap() { return skillMap; }

    public SkillInstance getSkill(Skill skill) { return skillMap().getSkill(skill); }

    public int getLevel(Skill skill) { return skillMap().getLevel(skill); }

    public List<SkillInstance> getSortedSkills() { return skillMap().values().stream().sorted(SkillInstance::compareTo).toList(); }

    @Override
    public double weight()
    {
        double weight = 0;
        for (SkillInstance instance : skillMap().values()) weight += instance.weight();
        weight += legacy().weight();

        return weight;
    }

    @NotNull
    public Skill highestSkill()
    {
        List<SkillInstance> sorted = getSortedSkills();
        int size = sorted.size();
        return size < 2 ? Peritia.skillHandler().getFirstSkill() : sorted.get(size - 1).skill();
    }

    @NotNull
    public Skill lowestSkill()
    {
        List<SkillInstance> sorted = getSortedSkills();
        return sorted.isEmpty() ? Peritia.skillHandler().getFirstSkill() : sorted.get(0).skill();
    }

    @NotNull
    public Skill medianSkill()
    {
        List<SkillInstance> sorted = getSortedSkills();
        if (sorted.size() < 2) return Peritia.skillHandler().getFirstSkill();

        int index = (sorted.size() - 1) / 2;
        return sorted.get(index).skill();
    }

    public float avgSkillLevel()
    {
        float levels = 0;
        int skillCount = 0;

        for (Skill skill : Peritia.skills())
        {
            int level = getLevel(skill);
            levels += level;
            skillCount++;
        }
        return levels / skillCount;
    }

    public int totalXp()
    {
        int totalXp = 0;
        for (Skill skill : Peritia.skills())
        {
            totalXp += getSkill(skill).totalXp();
        }
        return totalXp;
    }

    public HighScoreMap highScoreMap() { return highScoreMap; }

    public int getHighScore(Skill skill) { return highScoreMap().getHighScore(skill); }

    public Legacy legacy() { return legacy; }

    public int allTimeXp() { return legacy().pastXp() + totalXp(); }

    public int legacyScore() { return legacy().pastScore(); }

    public int pendingLegacyScore() { return legacy().getPendingScore(allTimeXp()); }

    public int extraLegacyScore() { return legacy().getExtraScore(allTimeXp()); }

    public boolean hasExtraLegacyScore() { return extraLegacyScore() > 0; }

    public int ascensionCount() { return legacy().ascensionCount(); }

    public ResourceSet discoveredXpSources() { return discoveredXpSources; }

    public <T extends XpSource> boolean hasDiscovered(T xpSource) { return discoveredXpSources().contains(xpSource); }

    public boolean hasCompletedEncyclopediaFor(Player player, Skill skill)
    {
        for (XpSource xpSource : skill.getXpSourcesForSelf(this))
        {
            if (xpSource.shouldHide(player, this)) return false;
        }
        return discoveredXpSources().containsAll(skill.getXpSourcesForSelf(this));
    }

    public JsonObject additionalData() { return additional; }

    public double xpMultiplier() { return PeritiaServerConfig.UNIVERSAL_XP_MULTIPLIER.get(); }

    public boolean canAscend() { return PeritiaServerConfig.ENABLE_ASCENSIONS.get(); }

    public int timePlayed() { return timePlayed; }

    public SerializableDate lastUpdate() { return lastUpdate; }

    public SerializableDate lastAscended() { return lastAscended; }

    public boolean isAscensionCooldownFinished()
    {
        SerializableDate nextDate = lastAscended().add(Calendar.MINUTE, 1);
        return nextDate.offsetFromNow() <= 0;
    }

    public boolean hasProgress()
    {
        if (ascensionCount() > 0) return true;

        for (SkillInstance instance : skillMap().values())
        {
            if (instance.hasProgress()) return true;
        }
        return false;
    }

    public boolean isNew() { return !hasProgress(); }

}
