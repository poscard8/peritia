package github.poscard8.peritia.util.serialization;

import github.poscard8.peritia.Peritia;

public interface Serializable<I, T extends Serializable<I, T>>
{
    T fallback();

    T load(I data);

    I save();

    default T loadWithFallback(I data) { return loadWithFallback(data, fallback()); }

    default T loadWithFallback(I data, T fallback)
    {
        try
        {
            return load(data);
        }
        catch (Exception exception)
        {
            Peritia.LOGGER.error("Error loading object {}, using a fallback instead. If the object is related to an unloaded 3rd mod, ignore this message", this);
            Peritia.LOGGER.error("Cause: {}", exception.getMessage());
            return fallback;
        }
    }

    default I saveForClient() { return save(); }

}
