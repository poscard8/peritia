package github.poscard8.peritia.xpsource.type;

import com.google.gson.JsonObject;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.xpsource.ItemInputs;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.XpSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ConsumeXpSource extends XpSource
{
    protected ItemInputs inputs = ItemInputs.empty();

    public ConsumeXpSource(ResourceLocation key) { super(key); }

    public static ConsumeXpSource empty() { return new ConsumeXpSource(EMPTY_KEY); }

    @Nullable
    public static ConsumeXpSource tryLoad(JsonObject data)
    {
        @Nullable XpSource xpSource = empty().loadWithFallback(data);
        return xpSource != null ? (ConsumeXpSource) xpSource : null;
    }

    @Override
    public XpSourceType<?> type() { return PeritiaXpSourceTypes.CONSUME.get(); }

    public ItemInputs inputs() { return inputs; }

    @Override
    public boolean isInvalid() { return super.isInvalid() || inputs().isEmpty(); }

    public void handleConsume(ServerPlayer player, ItemStack stack)
    {
        if (inputs().test(stack)) tryAward(player);
    }

    @Override
    public void loadAdditional(JsonObject data)
    {
        this.inputs = JsonHelper.readElementSerializable(data, "inputs", ItemInputs::tryLoad, inputs);
    }

    @Override
    public void saveAdditional(JsonObject data)
    {
        JsonHelper.write(data, "inputs", inputs);
    }

}
