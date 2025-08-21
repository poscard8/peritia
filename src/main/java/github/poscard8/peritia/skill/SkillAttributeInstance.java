package github.poscard8.peritia.skill;

import com.google.gson.JsonObject;
import github.poscard8.peritia.reward.RewardLike;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import github.poscard8.peritia.util.skill.AtFunction;
import github.poscard8.peritia.util.skill.AtFunctionHolder;
import github.poscard8.peritia.util.text.PeritiaTexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class SkillAttributeInstance implements JsonSerializable<SkillAttributeInstance>, AtFunctionHolder, RewardLike<SkillAttributeInstance>
{
    public static final ResourceLocation DEFAULT_ATTRIBUTE_KEY = new ResourceLocation("generic.max_health");

    protected ResourceLocation attributeKey;
    protected double value;
    protected AtFunction at;

    public SkillAttributeInstance(ResourceLocation attributeKey, double value, AtFunction at)
    {
        this.attributeKey = attributeKey;
        this.value = value;
        this.at = at;
    }

    public static SkillAttributeInstance empty() { return new SkillAttributeInstance(DEFAULT_ATTRIBUTE_KEY, 0.0D, AtFunction.empty()); }

    public static SkillAttributeInstance tryLoad(JsonObject data) { return empty().loadWithFallback(data); }

    public ResourceLocation attributeKey() { return attributeKey; }

    @Nullable
    public Attribute attribute() { return ForgeRegistries.ATTRIBUTES.getValue(attributeKey); }

    public double value() { return value; }

    @Override
    public AtFunction at() { return at; }

    @Override
    public SkillAttributeInstance multiplyBy(int multiplier) { return new SkillAttributeInstance(attributeKey(), value() * multiplier, at()); }

    @Override
    public boolean shouldDisplayText(Skill skill, int level) { return attribute() != null && value != 0; }

    @Override
    public Component getText(Skill skill, int level) { return PeritiaTexts.attribute(this); }

    @Override
    public int priority() { return 2; }

    @Override
    public SkillAttributeInstance fallback() { return empty(); }

    @Override
    public SkillAttributeInstance load(JsonObject data)
    {
        this.attributeKey = JsonHelper.readResource(data, "attribute", attributeKey);
        this.value = JsonHelper.readDouble(data, "value", value);
        this.at = JsonHelper.readElementSerializable(data, "at", AtFunction::tryLoad, at);

        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();
        JsonHelper.write(data, "attribute", attributeKey);
        JsonHelper.write(data, "value", value);
        JsonHelper.write(data, "at", at);

        return data;
    }

}
