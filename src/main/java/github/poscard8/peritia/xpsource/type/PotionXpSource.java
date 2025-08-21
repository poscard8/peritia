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
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.jetbrains.annotations.Nullable;

public class PotionXpSource extends GuiXpSource
{
    protected ItemInputs inputs = ItemInputs.empty();

    public PotionXpSource(ResourceLocation key) { super(key); }

    public static PotionXpSource empty() { return new PotionXpSource(EMPTY_KEY); }

    @Nullable
    public static PotionXpSource tryLoad(JsonObject data)
    {
        @Nullable XpSource xpSource = empty().loadWithFallback(data);
        return xpSource != null ? (PotionXpSource) xpSource : null;
    }

    @Override
    public XpSourceType<?> type() { return PeritiaXpSourceTypes.POTION.get(); }

    public ItemInputs inputs() { return inputs; }

    @Override
    public boolean isInvalid() { return super.isInvalid() || inputs().isEmpty(); }

    public boolean isValidItem(ItemStack stack) { return inputs().test(stack) && PotionBrewing.isIngredient(stack); }

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
