package github.poscard8.peritia.xpsource.type;

import com.google.gson.JsonObject;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.xpsource.ItemInputs;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.XpSourceType;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class FishXpSource extends XpSource
{
    protected ItemInputs inputs = ItemInputs.empty();

    public FishXpSource(ResourceLocation key) { super(key); }

    public static FishXpSource empty() { return new FishXpSource(EMPTY_KEY); }

    @Nullable
    public static FishXpSource tryLoad(JsonObject data)
    {
        @Nullable XpSource xpSource = empty().loadWithFallback(data);
        return xpSource != null ? (FishXpSource) xpSource : null;
    }

    @Override
    public XpSourceType<?> type() { return PeritiaXpSourceTypes.FISH.get(); }

    public ItemInputs inputs() { return inputs; }

    @Override
    public boolean isInvalid() { return super.isInvalid() || inputs().isEmpty(); }

    public void handleFish(ServerPlayer player, NonNullList<ItemStack> drops)
    {
        int multiplier = 0;

        for (ItemStack stack : drops)
        {
            ItemStack copy = stack.copy();
            if (inputs().test(copy)) multiplier += copy.getCount();
        }
        if (multiplier > 0) tryAward(player, multiplier);
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
