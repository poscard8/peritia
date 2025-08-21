package github.poscard8.peritia.xpsource.type;

import com.google.gson.JsonObject;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.util.PeritiaHelper;
import github.poscard8.peritia.xpsource.GuiXpSource;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.XpSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class EnchantmentXpSource extends GuiXpSource
{
    public EnchantmentXpSource(ResourceLocation key) { super(key); }

    public static EnchantmentXpSource empty() { return new EnchantmentXpSource(EMPTY_KEY); }

    public static EnchantmentXpSource tryLoad(JsonObject data)
    {
        @Nullable XpSource xpSource = empty().loadWithFallback(data);
        return xpSource != null ? (EnchantmentXpSource) xpSource : null;
    }

    public static int evaluate(Enchantment enchantment)
    {
        if (enchantment.isCurse()) return 0;

        int ordinal = enchantment.getRarity().ordinal();
        int addition = enchantment.isTreasureOnly() ? 2 : 1;
        return ordinal + addition;
    }

    public static int evaluate(Enchantment enchantment, int level) { return evaluate(enchantment) * level; }

    public static int evaluate(ItemStack stack)
    {
        int value = 0;
        Map<Enchantment, Integer> map = PeritiaHelper.getEnchantmentMap(stack);

        for (Map.Entry<Enchantment, Integer> entry : map.entrySet())
        {
            value += evaluate(entry.getKey(), entry.getValue());
        }

        return value;
    }

    @Override
    public XpSourceType<?> type() { return PeritiaXpSourceTypes.ENCHANTMENT.get(); }

    public void handleItem(ServerPlayer player, ItemStack stack) { addWaitingXp(player, evaluate(stack.copy())); }
    
    @Override
    public void loadAdditional(JsonObject data) {}

    @Override
    public void saveAdditional(JsonObject data) {}

}
