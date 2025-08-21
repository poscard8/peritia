package github.poscard8.peritia.advancement;

import github.poscard8.peritia.skill.data.ServerSkillData;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class PeritiaAdvancementTriggers
{
    static final List<CriterionTrigger<?>> VALUES = new ArrayList<>();
    static final List<SkillDataTrigger<?>> SKILL_DATA_TRIGGERED = new ArrayList<>();

    public static final SkillTrigger SKILL = register(new SkillTrigger());
    public static final SkillRecipeTrigger SKILL_RECIPE = register(new SkillRecipeTrigger());
    public static final TotalXpTrigger TOTAL_XP = register(new TotalXpTrigger());
    public static final AscensionTrigger ASCENSION = register(new AscensionTrigger());
    public static final EncyclopediaTrigger ENCYCLOPEDIA = register(new EncyclopediaTrigger());

    public static void register() { VALUES.forEach(CriteriaTriggers::register); }

    public static <T extends CriterionTrigger<?>> T register(T trigger)
    {
        VALUES.add(trigger);
        if (trigger instanceof SkillDataTrigger<?> skillDataTrigger) SKILL_DATA_TRIGGERED.add(skillDataTrigger);

        return trigger;
    }

    public static void trigger(ServerPlayer player, ServerSkillData skillData)
    {
        SKILL_DATA_TRIGGERED.forEach(trigger -> trigger.trigger(player, skillData));
    }

}
