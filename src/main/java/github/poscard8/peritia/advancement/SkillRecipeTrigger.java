package github.poscard8.peritia.advancement;

import com.google.gson.JsonObject;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.skill.recipe.SkillRecipe;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.skill.SkillRecipePredicate;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SkillRecipeTrigger extends SimpleCriterionTrigger<SkillRecipeTrigger.TriggerInstance>
{
    public static final ResourceLocation ID = Peritia.asResource("skill_recipe");

    public void trigger(ServerPlayer player, SkillRecipe recipe)
    {
        trigger(player, triggerInstance -> triggerInstance.recipePredicate.test(recipe));
    }

    @Override
    protected TriggerInstance createInstance(JsonObject jsonObject, ContextAwarePredicate predicate, DeserializationContext context)
    {
        SkillRecipePredicate recipePredicate = JsonHelper.readStringSerializable(jsonObject, "recipe", SkillRecipePredicate::tryLoad, SkillRecipePredicate.empty());
        return new TriggerInstance(predicate, recipePredicate);
    }

    @Override
    public ResourceLocation getId() { return ID; }


    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        public final SkillRecipePredicate recipePredicate;

        public TriggerInstance(ContextAwarePredicate predicate, SkillRecipePredicate recipePredicate)
        {
            super(ID, predicate);
            this.recipePredicate = recipePredicate;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context)
        {
            JsonObject data = super.serializeToJson(context);
            JsonHelper.write(data, "recipe", recipePredicate);

            return data;
        }
    }


}
