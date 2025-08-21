package github.poscard8.peritia.util.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import github.poscard8.peritia.Peritia;
import org.slf4j.Logger;

public interface PeritiaResourceHandler
{
    Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    Logger MOD_LOGGER = Peritia.LOGGER;

    void revalidate();

}
