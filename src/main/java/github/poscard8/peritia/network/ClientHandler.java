package github.poscard8.peritia.network;

import github.poscard8.peritia.ascension.ClientAscensionSystem;
import github.poscard8.peritia.network.packet.clientbound.*;
import github.poscard8.peritia.skill.data.ClientSkillData;
import github.poscard8.peritia.util.minecraft.GameContext;
import github.poscard8.peritia.util.minecraft.LookContext;
import github.poscard8.peritia.util.minecraft.SimpleAttributeMap;
import github.poscard8.peritia.xpsource.data.ClientXpSourceData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface ClientHandler
{
    static ClientHandler getInstance() { return PeritiaClientHandler.getInstance(); }

    @NotNull
    static String getLanguageCode() { return getInstance().languageCode(); }

    @NotNull
    static String getPlayerUUID() { return getInstance().player() == null ? "" : Objects.requireNonNull(getInstance().player()).getStringUUID(); }

    @NotNull
    static ClientSkillData getSkillData() { return getInstance().skillData(); }

    @NotNull
    static ClientXpSourceData getXpSourceData() { return getInstance().xpSourceData(); }

    @NotNull
    static ClientAscensionSystem getAscensionSystem() { return getInstance().ascensionSystem(); }

    @NotNull
    static GameContext getGameContext() { return getInstance().gameContext(); }

    @Nullable
    static LookContext getLookContext() { return getInstance().lookContext(); }

    @NotNull
    static ClientSkillData getViewingSkillData() { return getInstance().viewingSkillData(); }

    @NotNull
    static SimpleAttributeMap getViewingAttributeMap() { return getInstance().viewingAttributeMap(); }

    @NotNull
    static SimpleAttributeMap getAttributeMap() { return getInstance().attributeMap(); }

    @NotNull
    static RecipeManager getRecipeManager() { return getInstance().recipeManager(); }

    @NotNull
    String languageCode();

    @Nullable
    Player player();

    @NotNull
    ClientSkillData skillData();

    @NotNull
    ClientXpSourceData xpSourceData();

    @NotNull
    ClientAscensionSystem ascensionSystem();

    @NotNull
    GameContext gameContext();

    @Nullable
    LookContext lookContext();

    @NotNull
    ClientSkillData viewingSkillData();

    @NotNull
    SimpleAttributeMap viewingAttributeMap();

    @NotNull
    SimpleAttributeMap attributeMap();

    @NotNull
    RecipeManager recipeManager();

    void handlePeritiaResourcesPacket(PeritiaResourcesPacket packet);

    void handleGameContextPacket(GameContextPacket packet);

    void handleLookContextPacket(LookContextPacket packet);

    void handleInstructionsPacket(InstructionsPacket packet);

    void handleSkillDataPacket(SkillDataPacket packet);

    void handleXpSourceDataPacket(XpSourceDataPacket packet);

    void handleAscensionSystemPacket(AscensionSystemPacket packet);

    void handleGainXpPacket(XpGainPacket packet);

    void handleLevelUpPacket(LevelUpPacket packet);

    void handleLevelUpReadyPacket(LevelUpReadyPacket packet);

    void handleEncyclopediaUpdatePacket(EncyclopediaUpdatePacket packet);

    void handleEncyclopediaCompletePacket(EncyclopediaCompletePacket packet);

    void handleChestLuckPacket(ChestLuckPacket packet);

    void handleRefreshScreenPacket(RefreshScreenPacket packet);

    void handleAttributePacket(AttributePacket packet);

}
