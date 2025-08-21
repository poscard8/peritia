package github.poscard8.peritia.xpsource;

import com.google.gson.JsonObject;
import github.poscard8.peritia.util.serialization.Loadable;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class XpSourceType<T extends XpSource>
{
    protected final Class<T> clazz;
    protected final Function<JsonObject, T> loadFunction;
    protected final boolean dataType;

    protected ResourceLocation key = Loadable.EMPTY_KEY;

    @SuppressWarnings("ALL")
    public XpSourceType(Class<T> clazz, Function<JsonObject, T> loadFunction)
    {
        this.clazz = clazz;
        this.loadFunction = loadFunction;
        this.dataType = DataXpSource.class.isAssignableFrom(clazz);
    }

    public T loadXpSource(JsonObject data) { return loadFunction.apply(data); }

    public ResourceLocation key() { return key; }

    public boolean isDataType() { return dataType; }

    public boolean isEmpty() { return key().equals(Loadable.EMPTY_KEY); }

    public void assignKey(ResourceLocation key) { this.key = key; }


}
