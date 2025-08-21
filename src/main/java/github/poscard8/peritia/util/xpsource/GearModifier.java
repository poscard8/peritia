package github.poscard8.peritia.util.xpsource;

import github.poscard8.peritia.skill.Priority;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class GearModifier
{
    public final Priority priority;
    public final Predicate<ItemStack> itemPredicate;
    public final Set<EquipmentSlot> slots;
    public final float normalMultiplier;
    public final float enchantedMultiplier;

    GearModifier(Priority priority, Predicate<ItemStack> itemPredicate, Set<EquipmentSlot> slots, float normalMultiplier, float enchantedMultiplier)
    {
        this.priority = priority;
        this.itemPredicate = itemPredicate;
        this.slots = slots;
        this.normalMultiplier = normalMultiplier;
        this.enchantedMultiplier = enchantedMultiplier;
    }

    public static float getMultiplier(Entity entity)
    {
        float multiplier = 1;
        Set<EquipmentSlot> validatedSlots = new HashSet<>();
        for (GearModifier modifier : GearModifiers.values()) multiplier += modifier.validateEntity(entity, validatedSlots);

        return multiplier;
    }

    public static float getEnchantMultiplier(Entity entity)
    {
        return 1 + GearModifiers.ENCHANTED_WEAPON.validateEntity(entity, new HashSet<>());
    }

    public float validateEntity(Entity entity0, Set<EquipmentSlot> validatedSlots)
    {
        if (!(entity0 instanceof LivingEntity entity)) return 0;
        float total = 0;

        for (EquipmentSlot slot : slots)
        {
            if (validatedSlots.contains(slot)) continue;

            ItemStack stack = entity.getItemBySlot(slot);
            float added = stack.isEnchanted() ? enchantedMultiplier : normalMultiplier;

            if (itemPredicate.test(stack))
            {
                total += added;
                validatedSlots.add(slot);
            }
        }
        return total;
    }


    public static class Builder
    {
        Priority priority = Priority.NORMAL;
        Predicate<ItemStack> itemPredicate = stack -> false;
        Set<EquipmentSlot> slots = new HashSet<>();
        float normalMultiplier = 0.0F;
        float enchantedMultiplier = 0.0F;

        public Builder() {}

        public Builder priority(Priority priority)
        {
            this.priority = priority;
            return this;
        }

        public Builder item(Item item)
        {
            this.itemPredicate = stack -> stack.is(item);
            return this;
        }

        public Builder tag(TagKey<Item> tag)
        {
            this.itemPredicate = stack -> stack.is(tag);
            return this;
        }

        public Builder predicate(Predicate<ItemStack> itemPredicate)
        {
            this.itemPredicate = itemPredicate;
            return this;
        }

        public Builder mainHandSlot() { return slots(EquipmentSlot.MAINHAND); }

        public Builder armorSlots() { return slots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET); }

        public Builder slots(EquipmentSlot... slots)
        {
            this.slots = new HashSet<>(Arrays.asList(slots));
            return this;
        }

        public Builder multiplier(float multiplier) { return multiplier(multiplier, multiplier * 1.5F); }

        public Builder multiplier(float normal, float enchanted)
        {
            this.normalMultiplier = normal;
            this.enchantedMultiplier = enchanted;
            return this;
        }

        public GearModifier build() { return new GearModifier(priority, itemPredicate, slots, normalMultiplier, enchantedMultiplier); }

    }


}
