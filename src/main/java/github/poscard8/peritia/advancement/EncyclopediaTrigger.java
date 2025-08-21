package github.poscard8.peritia.advancement;

import com.google.gson.JsonObject;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.data.ServerSkillData;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.skill.SkillPredicate;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EncyclopediaTrigger extends SkillDataTrigger<EncyclopediaTrigger.TriggerInstance>
{
    public static final ResourceLocation ID = Peritia.asResource("encyclopedia");

    @Override
    public void trigger(ServerPlayer player, ServerSkillData skillData)
    {
        trigger(player, triggerInstance -> triggerInstance.check(skillData));
    }

    @Override
    protected TriggerInstance createInstance(JsonObject jsonObject, ContextAwarePredicate predicate, DeserializationContext context)
    {
        SkillPredicate skillPredicate = JsonHelper.readStringSerializable(jsonObject, "skill", SkillPredicate::tryLoad, SkillPredicate.empty());
        return new TriggerInstance(predicate, skillPredicate);
    }

    @Override
    public ResourceLocation getId() { return ID; }


    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        public final SkillPredicate skillPredicate;

        public TriggerInstance(ContextAwarePredicate predicate, SkillPredicate skillPredicate)
        {
            super(ID, predicate);
            this.skillPredicate = skillPredicate;
        }

        public boolean check(ServerSkillData skillData)
        {
            for (Skill skill : skillPredicate)
            {
                if (skillData.hasCompletedEncyclopediaFor(skill)) return true;
            }
            return false;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context)
        {
            JsonObject data = super.serializeToJson(context);
            JsonHelper.write(data, "skill", skillPredicate);

            return data;
        }

    }

}
