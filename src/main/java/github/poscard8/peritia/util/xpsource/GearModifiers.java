package github.poscard8.peritia.util.xpsource;

import github.poscard8.peritia.skill.Priority;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GearModifiers
{
    static final Map<Priority, List<GearModifier>> MAP = Map.of(Priority.HIGH, new ArrayList<>(), Priority.NORMAL, new ArrayList<>(), Priority.LOW, new ArrayList<>());

    public static List<GearModifier> byPriority(Priority priority) { return MAP.get(priority); }

    public static List<GearModifier> values()
    {
        List<GearModifier> modifiers = new ArrayList<>();
        modifiers.addAll(byPriority(Priority.HIGH));
        modifiers.addAll(byPriority(Priority.NORMAL));
        modifiers.addAll(byPriority(Priority.LOW));

        return modifiers;
    }

    public static GearModifier register(GearModifier modifier)
    {
        List<GearModifier> list = byPriority(modifier.priority);
        list.add(modifier);
        return modifier;
    }

    public static GearModifier register(GearModifier modifier, int index)
    {
        List<GearModifier> list = byPriority(modifier.priority);
        list.add(index, modifier);
        return modifier;
    }

    public static void register()
    {
        register(TRIDENT);
        register(FISHING_ROD);
        register(GOLDEN_ARMOR);
        register(GOLDEN_SWORD);

        register(ALL_SWORDS);
        register(ALL_AXES);
        register(ALL_PICKAXES);
        register(ALL_SHOVELS);
        register(ALL_HOES);
        register(ALL_ARMOR);

        register(ENCHANTED_WEAPON);
    }

    public static final GearModifier TRIDENT = new GearModifier.Builder()
            .item(Items.TRIDENT)
            .mainHandSlot()
            .multiplier(2.0F)
            .build();

    public static final GearModifier FISHING_ROD = new GearModifier.Builder()
            .item(Items.FISHING_ROD)
            .mainHandSlot()
            .multiplier(0.15F)
            .build();

    public static final GearModifier GOLDEN_ARMOR = new GearModifier.Builder()
            .predicate(stack -> stack.getItem() instanceof ArmorItem armor && armor.getMaterial() == ArmorMaterials.GOLD)
            .armorSlots()
            .multiplier(0.5F)
            .build();

    public static final GearModifier GOLDEN_SWORD = new GearModifier.Builder()
            .item(Items.GOLDEN_SWORD)
            .mainHandSlot()
            .multiplier(1.0F)
            .build();

    public static final GearModifier ALL_SWORDS = new GearModifier.Builder()
            .priority(Priority.LOW)
            .tag(ItemTags.SWORDS)
            .mainHandSlot()
            .multiplier(0.3F)
            .build();

    public static final GearModifier ALL_AXES = new GearModifier.Builder()
            .priority(Priority.LOW)
            .tag(ItemTags.AXES)
            .mainHandSlot()
            .multiplier(0.25F)
            .build();

    public static final GearModifier ALL_PICKAXES = new GearModifier.Builder()
            .priority(Priority.LOW)
            .tag(ItemTags.PICKAXES)
            .mainHandSlot()
            .multiplier(0.15F)
            .build();

    public static final GearModifier ALL_SHOVELS = new GearModifier.Builder()
            .priority(Priority.LOW)
            .tag(ItemTags.SHOVELS)
            .mainHandSlot()
            .multiplier(0.15F)
            .build();

    public static final GearModifier ALL_HOES = new GearModifier.Builder()
            .priority(Priority.LOW)
            .tag(ItemTags.HOES)
            .mainHandSlot()
            .multiplier(0.15F)
            .build();

    public static final GearModifier ALL_ARMOR = new GearModifier.Builder()
            .priority(Priority.LOW)
            .predicate(stack -> stack.getItem() instanceof ArmorItem)
            .armorSlots()
            .multiplier(0.1F, 0.2F)
            .build();

    public static final GearModifier ENCHANTED_WEAPON = new GearModifier.Builder()
            .priority(Priority.LOW)
            .predicate(stack -> !stack.isEmpty() && stack.isEnchanted())
            .slots(EquipmentSlot.values())
            .multiplier(0.0F, 0.25F)
            .build();

}
