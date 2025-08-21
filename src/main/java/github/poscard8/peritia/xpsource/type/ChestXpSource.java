package github.poscard8.peritia.xpsource.type;

import com.google.gson.JsonObject;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.text.Hints;
import github.poscard8.peritia.util.xpsource.ResourceInputs;
import github.poscard8.peritia.xpsource.GuiXpSource;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.XpSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class ChestXpSource extends GuiXpSource
{
    protected ResourceInputs inputs = ResourceInputs.empty();
    protected Hints hints = Hints.empty();
    protected boolean useHints = false;

    public ChestXpSource(ResourceLocation key) { super(key); }

    public static ChestXpSource empty() { return new ChestXpSource(EMPTY_KEY); }

    @Nullable
    public static ChestXpSource tryLoad(JsonObject data)
    {
        @Nullable XpSource xpSource = empty().loadWithFallback(data);
        return xpSource != null ? (ChestXpSource) xpSource : null;
    }

    @Override
    public XpSourceType<?> type() { return PeritiaXpSourceTypes.CHEST.get(); }

    public ResourceInputs inputs() { return inputs; }

    public Hints hints() { return hints; }

    public boolean shouldUseHints() { return useHints; }

    @Override
    public boolean isInvalid() { return super.isInvalid() || inputs().isEmpty(); }

    public void handleChestOpen(ServerPlayer player, ResourceLocation key, boolean awardNow)
    {
        if (inputs().test(key))
        {
            addWaitingXp(player);

            if (awardNow)
            {
                award(player);
                setWaitingXp(player, 0);
            }
        }
    }

    @Override
    public void loadAdditional(JsonObject data)
    {
        this.inputs = JsonHelper.readElementSerializable(data, "inputs", ResourceInputs::tryLoad, inputs);
        this.hints = JsonHelper.readArraySerializable(data, "hints", Hints::tryLoad, hints);
        this.useHints = JsonHelper.readBoolean(data, "useHints", data.has("hints"));
    }

    @Override
    public void saveAdditional(JsonObject data)
    {
        JsonHelper.write(data, "inputs", inputs);
        JsonHelper.write(data, "hints", hints);
        JsonHelper.write(data, "useHints", useHints);
    }

}
