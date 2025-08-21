package github.poscard8.peritia.xpsource.type;

import com.google.gson.JsonObject;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.xpsource.ItemInputs;
import github.poscard8.peritia.xpsource.GuiXpSource;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.XpSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class GrindstoneXpSource extends GuiXpSource
{
    protected ItemInputs inputs = ItemInputs.empty();

    public GrindstoneXpSource(ResourceLocation key) { super(key); }

    public static GrindstoneXpSource empty() { return new GrindstoneXpSource(EMPTY_KEY); }

    @Nullable
    public static GrindstoneXpSource tryLoad(JsonObject data)
    {
        @Nullable XpSource xpSource = empty().loadWithFallback(data);
        return xpSource != null ? (GrindstoneXpSource) xpSource : null;
    }

    @Override
    public XpSourceType<?> type() { return PeritiaXpSourceTypes.GRINDSTONE.get(); }

    public ItemInputs inputs() { return inputs; }

    @Override
    public boolean isInvalid() { return super.isInvalid() || inputs().isEmpty(); }

    public int evaluateItems(ItemStack top, ItemStack bottom)
    {
        int value = 0;
        if (inputs().test(top)) value++;
        if (inputs().test(bottom)) value++;

        return value;
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
