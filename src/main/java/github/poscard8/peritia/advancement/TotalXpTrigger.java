package github.poscard8.peritia.advancement;

import com.google.gson.JsonObject;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.skill.data.ServerSkillData;
import github.poscard8.peritia.util.serialization.JsonHelper;
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
public class TotalXpTrigger extends SkillDataTrigger<TotalXpTrigger.TriggerInstance>
{
    public static final ResourceLocation ID = Peritia.asResource("total_xp");

    @Override
    public void trigger(ServerPlayer player, ServerSkillData skillData)
    {
        trigger(player, triggerInstance -> skillData.allTimeXp() >= triggerInstance.xp);
    }

    @Override
    protected TriggerInstance createInstance(JsonObject jsonObject, ContextAwarePredicate predicate, DeserializationContext context)
    {
        int xp = JsonHelper.readInt(jsonObject, "xp", 0);
        return new TriggerInstance(predicate, xp);
    }

    @Override
    public ResourceLocation getId() { return ID; }


    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        public final int xp;

        public TriggerInstance(ContextAwarePredicate predicate, int xp)
        {
            super(ID, predicate);
            this.xp = xp;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context)
        {
            JsonObject data = super.serializeToJson(context);
            JsonHelper.write(data, "xp", xp);

            return data;
        }
    }

}
