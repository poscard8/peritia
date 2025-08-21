package github.poscard8.peritia.xpsource.type;

import com.google.gson.JsonObject;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.xpsource.ResourceInputs;
import github.poscard8.peritia.xpsource.GuiXpSource;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.XpSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class AdvancementXpSource extends GuiXpSource
{
    protected ResourceInputs inputs = ResourceInputs.empty();

    public AdvancementXpSource(ResourceLocation key) { super(key); }

    public static AdvancementXpSource empty() { return new AdvancementXpSource(EMPTY_KEY); }

    @Nullable
    public static AdvancementXpSource tryLoad(JsonObject data)
    {
        @Nullable XpSource xpSource = empty().loadWithFallback(data);
        return xpSource != null ? (AdvancementXpSource) xpSource : null;
    }

    @Override
    public XpSourceType<?> type() { return PeritiaXpSourceTypes.ADVANCEMENT.get(); }

    public ResourceInputs inputs() { return inputs; }

    @Override
    public boolean isInvalid() { return super.isInvalid() || inputs().isEmpty(); }

    public void handleEarn(ServerPlayer player, ResourceLocation key)
    {
        if (inputs().test(key) && canPlayerGainXp(player))
        {
            addWaitingXp(player);

            if (!player.containerMenu.stillValid(player) || !player.hasContainerOpen())
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
    }

    @Override
    public void saveAdditional(JsonObject data)
    {
        JsonHelper.write(data, "inputs", inputs);
    }

}
