package github.poscard8.peritia.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class AttributeEnchantment extends Enchantment
{
    public static final EnchantmentCategory CATEGORY = EnchantmentCategory.create("tool_and_weapon", item ->
    {
        return item instanceof DiggerItem ||
                item instanceof SwordItem ||
                item instanceof TridentItem ||
                item instanceof ProjectileWeaponItem;
    });
    public static final EquipmentSlot[] SLOTS = new EquipmentSlot[]{EquipmentSlot.MAINHAND};


    public static final UUID MODIFIER_UUID = UUID.fromString("B370F1DF-651C-4665-8BA0-738A8B3C8CF6");
    public static final Supplier<String> MODIFIER_NAME = () -> "Peritia: Enchantment modifier";

    public final @NotNull Attribute attribute;
    public final UnaryOperator<Double> valueFunction;
    public final int maxLevel;
    public final boolean treasure;
    public final boolean symmetryIncompatible;

    public AttributeEnchantment(Rarity rarity, @NotNull Attribute attribute, double valuePerLevel, int maxLevel)
    {
        this(rarity, attribute, valuePerLevel, maxLevel, true, true);
    }

    public AttributeEnchantment(Rarity rarity, @NotNull Attribute attribute, double valuePerLevel, int maxLevel, boolean treasure, boolean symmetryIncompatible)
    {
        this(rarity, attribute, level -> level * valuePerLevel, maxLevel, treasure, symmetryIncompatible);
    }

    public AttributeEnchantment(Rarity rarity, @NotNull Attribute attribute, UnaryOperator<Double> valueFunction, int maxLevel, boolean treasure, boolean symmetryIncompatible)
    {
        super(rarity, CATEGORY, SLOTS);
        this.attribute = attribute;
        this.valueFunction = valueFunction;
        this.maxLevel = maxLevel;
        this.treasure = treasure;
        this.symmetryIncompatible = symmetryIncompatible;
    }

    @NotNull
    public Attribute attribute() { return attribute; }

    public double getAttributeValue(int level) { return valueFunction.apply((double) level); }

    public AttributeModifier getAttributeModifier(int level) { return new AttributeModifier(MODIFIER_UUID, MODIFIER_NAME, getAttributeValue(level), AttributeModifier.Operation.ADDITION); }

    @Override
    public int getMaxLevel() { return maxLevel; }

    @Override
    public boolean isTreasureOnly() { return treasure; }

    @Override
    public boolean isTradeable() { return !treasure; }

    @Override
    public boolean isDiscoverable() { return !treasure; }

    @Override
    protected boolean checkCompatibility(@NotNull Enchantment other) { return !(symmetryIncompatible && other instanceof SymmetryEnchantment); }

}
