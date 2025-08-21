package github.poscard8.peritia.util.serialization;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.handler.SkillHandler;
import github.poscard8.peritia.handler.SkillRecipeHandler;
import github.poscard8.peritia.handler.XpSourceHandler;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.clientbound.PeritiaResourcesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Set;

public class PeritiaResourceLoader
{
    static final Logger LOGGER = Peritia.LOGGER;

    public final SkillHandler skillHandler = new SkillHandler();
    public final SkillRecipeHandler skillRecipeHandler = new SkillRecipeHandler();
    public final XpSourceHandler xpSourceHandler = new XpSourceHandler();

    boolean clientSide;
    Stage stage;
    @Nullable MinecraftServer server;

    public PeritiaResourceLoader()
    {
        this.clientSide = true;
        this.stage = Stage.IDLE;
    }

    public boolean isClientSide() { return clientSide; }

    public boolean isServerSide() { return !isClientSide(); }

    public void setClientSide() { this.clientSide = true; }

    public void setServerSide() { this.clientSide = false; }

    public Stage stage() { return stage; }

    public void setStage(Stage stage)
    {
        this.stage = stage;
        if (isServerSide()) sendStageMessage();
    }

    public void sendStageMessage() { LOGGER.info("Peritia resource loader at stage {}", stage().displayName()); }

    @Nullable
    public MinecraftServer server() { return server; }

    public void setServer(@Nullable MinecraftServer server)
    {
        if (isClientSide())
        {
            LOGGER.error("Tried to add a server to a client-side Peritia resource loader");
            return;
        }
        this.server = server;

        if (server != null)
        {
            revalidate();
            setStage(Stage.COMPLETE);
        }
        else setStage(Stage.IDLE);
    }

    public PeritiaResources getResources()
    {
        Stage stage = stage();
        if (stage == Stage.IDLE || stage == Stage.INITIAL_LOAD)
        {
            LOGGER.warn("Attempted to get Peritia resources on stage {}. This may result in mismatch between the server and client", stage.displayName());
        }
        return new PeritiaResources(this);
    }

    public void setClientResources(PeritiaResources resources)
    {
        if (isServerSide())
        {
            LOGGER.warn("Set client resources on a server-side Peritia resource loader. If this is a singleplayer server ignore this message");
        }
        skillHandler.skills = resources.loadSkills();
        skillRecipeHandler.recipes = resources.loadSkillRecipes();
        xpSourceHandler.xpSources = resources.loadXpSources();
        setStage(Stage.COMPLETE);
    }

    public void sendResourcesToClient(ServerPlayer player) { sendResourcesToClients(Set.of(player)); }

    public void sendResourcesToClients()
    {
        Collection<ServerPlayer> players = server == null ? Set.of() : server.getPlayerList().getPlayers();
        sendResourcesToClients(players);
    }

    public void sendResourcesToClients(Collection<ServerPlayer> players)
    {
        for (ServerPlayer player : players)
        {
            PeritiaNetworkHandler.sendToClient(new PeritiaResourcesPacket(getResources()), player);
            LOGGER.info("Sent Peritia resources to player {}", player.getName().getString());
        }
    }

    public void onResourceReload(AddReloadListenerEvent event)
    {
        setStage(Stage.IDLE);

        event.addListener(skillHandler);
        event.addListener(skillRecipeHandler);
        event.addListener(xpSourceHandler);

        if (server != null)
        {
            revalidate();
            setStage(Stage.COMPLETE);
            sendResourcesToClients();
        }
        else setStage(Stage.INITIAL_LOAD);
    }

    public void revalidate()
    {
        skillHandler.revalidate();
        skillRecipeHandler.revalidate();
        xpSourceHandler.revalidate();
    }

    public enum Stage
    {
        IDLE,
        INITIAL_LOAD,
        COMPLETE;

        public String displayName() { return name().replace('_', ' '); }

    }

}
