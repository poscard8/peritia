package github.poscard8.peritia.advancement;

import github.poscard8.peritia.skill.data.ServerSkillData;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public abstract class SkillDataTrigger<T extends AbstractCriterionTriggerInstance> extends SimpleCriterionTrigger<T>
{
    public abstract void trigger(ServerPlayer player, ServerSkillData skillData);

}
