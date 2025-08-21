package github.poscard8.peritia.advancement;

import com.google.gson.JsonObject;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.ascension.Legacy;
import github.poscard8.peritia.skill.data.ServerSkillData;
import github.poscard8.peritia.skill.data.SkillData;
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
public class AscensionTrigger extends SkillDataTrigger<AscensionTrigger.TriggerInstance>
{
    public static final ResourceLocation ID = Peritia.asResource("ascension");

    @Override
    public void trigger(ServerPlayer player, ServerSkillData skillData)
    {
        trigger(player, triggerInstance -> triggerInstance.check(skillData));
    }

    @Override
    protected TriggerInstance createInstance(JsonObject jsonObject, ContextAwarePredicate predicate, DeserializationContext context)
    {
        int score = JsonHelper.readInt(jsonObject, "score", 0);
        int count = JsonHelper.readInt(jsonObject, "count", 0);

        return new TriggerInstance(predicate, score, count);
    }

    @Override
    public ResourceLocation getId() { return ID; }


    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        public final int score;
        public final int count;

        public TriggerInstance(ContextAwarePredicate predicate, int score, int count)
        {
            super(ID, predicate);
            this.score = score;
            this.count = count;
        }

        public boolean check(SkillData skillData)
        {
            Legacy legacy = skillData.legacy();
            return legacy.pastScore() >= score && legacy.ascensionCount() >= count;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context)
        {
            JsonObject data = super.serializeToJson(context);
            JsonHelper.write(data, "score", score);
            JsonHelper.write(data, "count", count);

            return data;
        }
    }

}
