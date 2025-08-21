package github.poscard8.peritia.xpsource.type;

import com.google.gson.JsonObject;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.xpsource.ItemInputs;
import github.poscard8.peritia.util.xpsource.ResourceInputs;
import github.poscard8.peritia.xpsource.GuiXpSource;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.XpSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class TradeXpSource extends GuiXpSource
{
    protected ResourceInputs professionInputs = ResourceInputs.acceptAll();
    protected ItemInputs itemInputs = ItemInputs.empty();

    public TradeXpSource(ResourceLocation key) { super(key); }

    public static TradeXpSource empty() { return new TradeXpSource(EMPTY_KEY); }

    public static TradeXpSource tryLoad(JsonObject data)
    {
        @Nullable XpSource xpSource = empty().loadWithFallback(data);
        return xpSource != null ? (TradeXpSource) xpSource : null;
    }

    @Override
    public XpSourceType<?> type() { return PeritiaXpSourceTypes.TRADE.get(); }

    public ResourceInputs professionInputs() { return professionInputs; }

    public ItemInputs itemInputs() { return itemInputs; }

    @Override
    public boolean isInvalid() { return super.isInvalid() || itemInputs().isEmpty(); }

    public boolean validateTrade(ResourceLocation professionKey, ItemStack result) { return professionInputs().test(professionKey) && itemInputs().test(result); }

    public void handleTrade(ServerPlayer player, ResourceLocation professionKey, ItemStack result)
    {
        if (validateTrade(professionKey, result)) addWaitingXp(player);
    }

    @Override
    public void loadAdditional(JsonObject data)
    {
        this.professionInputs = JsonHelper.readElementSerializable(data, "professions", ResourceInputs::tryLoad, professionInputs);
        this.itemInputs = JsonHelper.readElementSerializable(data, "items", ItemInputs::tryLoad, itemInputs);
    }

    @Override
    public void saveAdditional(JsonObject data)
    {
        JsonHelper.write(data, "professions", professionInputs);
        JsonHelper.write(data, "items", itemInputs);
    }

}
