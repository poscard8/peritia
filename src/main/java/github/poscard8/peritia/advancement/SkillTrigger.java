package github.poscard8.peritia.advancement;

import com.google.gson.JsonObject;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.skill.data.ServerSkillData;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.skill.SkillRequisite;
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
public class SkillTrigger extends SkillDataTrigger<SkillTrigger.TriggerInstance>
{
    public static final ResourceLocation ID = Peritia.asResource("skill");

    @Override
    public void trigger(ServerPlayer player, ServerSkillData skillData)
    {
        trigger(player, triggerInstance -> triggerInstance.requisite.test(skillData, false));
    }

    @Override
    protected TriggerInstance createInstance(JsonObject jsonObject, ContextAwarePredicate predicate, DeserializationContext context)
    {
        SkillRequisite requisite = JsonHelper.readStringSerializable(jsonObject, "requisite", SkillRequisite::tryLoad, SkillRequisite.empty());
        return new TriggerInstance(predicate, requisite);
    }

    @Override
    public ResourceLocation getId() { return ID; }


    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        public final SkillRequisite requisite;

        public TriggerInstance(ContextAwarePredicate predicate, SkillRequisite requisite)
        {
            super(ID, predicate);
            this.requisite = requisite;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context)
        {
            JsonObject data = super.serializeToJson(context);
            JsonHelper.write(data, "requisite", requisite);

            return data;
        }

    }

}
