package github.poscard8.peritia.xpsource.type;

import com.google.gson.JsonObject;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.xpsource.ItemInputs;
import github.poscard8.peritia.xpsource.GuiXpSource;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.XpSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AnvilXpSource extends GuiXpSource
{
    protected ItemInputs leftInputs = ItemInputs.empty();
    protected ItemInputs rightInputs = ItemInputs.empty();

    public AnvilXpSource(ResourceLocation key) { super(key); }

    public static AnvilXpSource empty() { return new AnvilXpSource(EMPTY_KEY); }

    @Nullable
    public static AnvilXpSource tryLoad(JsonObject data)
    {
        @Nullable XpSource xpSource = empty().loadWithFallback(data);
        return xpSource != null ? (AnvilXpSource) xpSource : null;
    }

    @Override
    public XpSourceType<?> type() { return PeritiaXpSourceTypes.ANVIL.get(); }

    public ItemInputs leftInputs() { return leftInputs; }

    public ItemInputs rightInputs() { return rightInputs; }

    @Override
    public boolean isInvalid() { return super.isInvalid() || leftInputs().isEmpty() || rightInputs().isEmpty(); }

    public void handleItems(ServerPlayer player, ItemStack left, ItemStack right)
    {
        if (leftInputs().test(left) && rightInputs().test(right)) addWaitingXp(player);
    }

    @Override
    public void loadAdditional(JsonObject data)
    {
        this.leftInputs = JsonHelper.readElementSerializable(data, "left", ItemInputs::tryLoad, leftInputs);
        this.rightInputs = JsonHelper.readElementSerializable(data, "right", ItemInputs::tryLoad, rightInputs);
    }

    @Override
    public void saveAdditional(JsonObject data)
    {
        JsonHelper.write(data, "left", leftInputs);
        JsonHelper.write(data, "right", rightInputs);
    }

}
