package github.poscard8.peritia.util.xpsource;

import github.poscard8.peritia.util.serialization.StringSerializable;
import net.minecraft.world.entity.Entity;

import java.util.Arrays;
import java.util.function.Function;

public enum GearFunction implements StringSerializable<GearFunction>
{
    IGNORE("ignore", entity -> 1.0F),
    ONLY_ENCHANTED("onlyEnchanted", GearModifier::getEnchantMultiplier),
    EVALUATE("evaluate", GearModifier::getMultiplier);

    private final String name;
    private final Function<Entity, Float> multiplierFunction;

    GearFunction(String name, Function<Entity, Float> multiplierFunction)
    {
        this.name = name;
        this.multiplierFunction = multiplierFunction;
    }

    public static GearFunction empty() { return GearFunction.IGNORE; }

    public static GearFunction tryLoad(String data) { return empty().loadWithFallback(data); }

    public String getName() { return name; }

    public float getMultiplier(Entity entity) { return multiplierFunction.apply(entity); }

    @Override
    public GearFunction fallback() { return empty(); }

    @Override
    public GearFunction load(String data)
    {
        return Arrays.stream(values()).filter(gearFunction -> gearFunction.getName().equals(data)).findFirst().orElse(fallback());
    }

    @Override
    public String save() { return getName(); }

}
