package github.poscard8.peritia.xpsource.type;

import com.google.gson.JsonObject;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.XpSourceType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class EmptyXpSource extends XpSource
{
    public EmptyXpSource(ResourceLocation key) { super(key); }

    public static EmptyXpSource empty() { return new EmptyXpSource(EMPTY_KEY); }

    @Nullable
    public static EmptyXpSource tryLoad(JsonObject data)
    {
        @Nullable XpSource xpSource = empty().loadWithFallback(data);
        return xpSource != null ? (EmptyXpSource) xpSource : null;
    }

    @Override
    public XpSourceType<?> type() { return PeritiaXpSourceTypes.EMPTY.get(); }

    @Override
    public boolean isEmpty() { return true; }

    @Override
    public void loadAdditional(JsonObject data) {}

    @Override
    public void saveAdditional(JsonObject data) {}

}
