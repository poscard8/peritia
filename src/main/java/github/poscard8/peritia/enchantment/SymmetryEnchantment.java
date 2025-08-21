package github.poscard8.peritia.enchantment;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static github.poscard8.peritia.enchantment.AttributeEnchantment.CATEGORY;
import static github.poscard8.peritia.enchantment.AttributeEnchantment.SLOTS;

public class SymmetryEnchantment extends Enchantment
{
    public final double transferRatio;
    public final int maxLevel;
    public final boolean treasure;
    public final boolean attributeIncompatible;

    public SymmetryEnchantment(Rarity rarity, double transferRatio, int maxLevel) { this(rarity, transferRatio, maxLevel, true, true); }

    public SymmetryEnchantment(Rarity rarity, double transferRatio, int maxLevel, boolean treasure, boolean attributeIncompatible)
    {
        super(rarity, CATEGORY, SLOTS);
        this.transferRatio = transferRatio;
        this.maxLevel = maxLevel;
        this.treasure = treasure;
        this.attributeIncompatible = attributeIncompatible;
    }

    public static int getExperienceGain(ServerPlayer player, int skillXpGain) { return getExperienceGain(player.getMainHandItem(), skillXpGain); }

    public static int getExperienceGain(ItemStack stack, int skillXpGain)
    {
        double transferRatio = 0;
        if (stack.getItem() instanceof EnchantedBookItem) return 0;
        Map<Enchantment, Integer> levelMap = EnchantmentHelper.getEnchantments(stack);

        for (Enchantment enchantment : levelMap.keySet())
        {
            if (enchantment instanceof SymmetryEnchantment symmetryEnchantment)
            {
                int level = levelMap.get(enchantment);
                transferRatio += (level * symmetryEnchantment.transferRatio);
            }
        }
        return (int) Math.round(skillXpGain * transferRatio);
    }

    @Override
    public int getMaxLevel() { return maxLevel; }

    @Override
    public boolean isTreasureOnly() { return treasure; }

    @Override
    public boolean isTradeable() { return !treasure; }

    @Override
    public boolean isDiscoverable() { return !treasure; }

    @Override
    protected boolean checkCompatibility(@NotNull Enchantment other) { return !(attributeIncompatible && other instanceof AttributeEnchantment); }

}
