package github.poscard8.peritia.network;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.ascension.ClientAscensionSystem;
import github.poscard8.peritia.client.MenuPreferences;
import github.poscard8.peritia.client.toast.ChestLuckToast;
import github.poscard8.peritia.client.toast.EncyclopediaToast;
import github.poscard8.peritia.client.toast.LevelUpReadyToast;
import github.poscard8.peritia.client.toast.LevelUpToast;
import github.poscard8.peritia.config.PeritiaClientConfig;
import github.poscard8.peritia.mixin.client.accessor.GuiAccessor;
import github.poscard8.peritia.network.packet.clientbound.*;
import github.poscard8.peritia.registry.PeritiaSoundEvents;
import github.poscard8.peritia.skill.data.ClientSkillData;
import github.poscard8.peritia.util.gui.PeritiaUIElement;
import github.poscard8.peritia.util.minecraft.GameContext;
import github.poscard8.peritia.util.minecraft.LookContext;
import github.poscard8.peritia.util.minecraft.SimpleAttributeMap;
import github.poscard8.peritia.util.serialization.PeritiaResources;
import github.poscard8.peritia.util.serialization.SerializableDate;
import github.poscard8.peritia.util.skill.LevelUpContext;
import github.poscard8.peritia.util.skill.XpGainContext;
import github.poscard8.peritia.util.text.PeritiaTexts;
import github.poscard8.peritia.xpsource.data.ClientXpSourceData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class PeritiaClientHandler implements ClientHandler
{
    static PeritiaClientHandler INSTANCE;
    static final int XP_CONTEXT_DURATION = 60;
    static final int LEVEL_CONTEXT_DURATION = 1;

    @NotNull Minecraft minecraft;

    ClientSkillData skillData;
    ClientXpSourceData xpSourceData;
    ClientAscensionSystem ascensionSystem;
    GameContext gameContext;
    SimpleAttributeMap attributeMap;
    @Nullable LookContext lookContext;
    @Nullable XpGainContext xpGainContext;
    @Nullable LevelUpContext levelUpContext;

    int xpAtBeginning = -1;
    int xpContextTicks = 0;
    int levelContextTicks = 0;
    int subtitleTicks = 0;
    boolean leveledUpManually = false;

    int screenTicks = 0;
    boolean fade = true;

    PeritiaClientHandler()
    {
        INSTANCE = this;
        this.minecraft = Minecraft.getInstance();
        MenuPreferences.load();
    }

    @NotNull
    public static PeritiaClientHandler getInstance() { return INSTANCE != null ? INSTANCE : new PeritiaClientHandler(); }

    public static void reload() { INSTANCE = new PeritiaClientHandler(); }

    public void tick()
    {
        if (xpGainContext() != null && xpContextTicks > 0)
        {
            xpContextTicks--;
            if (xpContextTicks == 0) resetXpGainContext();
        }

        if (levelUpContext() != null && levelContextTicks > 0)
        {
            levelContextTicks--;
            if (levelContextTicks == 0)
            {
                levelUp();
                resetLevelUpContext();
            }
        }

        GuiAccessor guiAccessor = (GuiAccessor) minecraft().gui;

        if (subtitleTicks > 1)
        {
            guiAccessor.setToolHighlightTimer((subtitleTicks - 1) / 2);
            guiAccessor.setLastToolHighLight(EncyclopediaUpdatePacket.ARBITRARY_ITEM_STACK);

            subtitleTicks--;
        }
        else if (subtitleTicks == 1)
        {
            guiAccessor.setLastToolHighLight(Objects.requireNonNull(player()).getInventory().getSelected());
            guiAccessor.setToolHighlightTimer(0);

            subtitleTicks--;
        }
    }

    public void playLocalSound(Supplier<SoundEvent> soundGetter, float volume, float pitch) { playLocalSound(soundGetter.get(), volume, pitch); }

    public void playLocalSound(SoundEvent sound, float volume, float pitch) { minecraft().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume)); }

    public void addParticlesAroundPlayer(ParticleOptions particleOptions, int count)
    {
        Player player = player();

        if (player == null) return;
        Random random = new Random();

        for(int i = 0; i < count; ++i)
        {
            double xd = random.nextGaussian() * 0.04D;
            double yd = random.nextGaussian() * 0.04D;
            double zd = random.nextGaussian() * 0.04D;

            player.level().addParticle(particleOptions, player.getRandomX(1.0D), player.getRandomY() + 0.25D, player.getRandomZ(1.0D), xd, yd, zd);
        }
    }

    public String getKeyMappingString() { return Component.keybind("key.peritia.main_menu").getString(); }

    @NotNull
    public Minecraft minecraft() { return minecraft; }

    @Override
    @Nullable
    public LocalPlayer player() { return minecraft().player; }

    @Override
    @NotNull
    public String languageCode() { return minecraft().getLanguageManager().getSelected(); }

    public void refreshScreen()
    {
        if (minecraft().screen instanceof PeritiaUIElement peritiaScreen) peritiaScreen.refresh();
    }

    public int screenTicks() { return screenTicks; }

    public void setScreenTicks(int screenTicks) { this.screenTicks = screenTicks; }

    public void addScreenTick() { setScreenTicks(screenTicks() + 1); }

    public boolean fade() { return fade; }

    public void setFade(boolean fade) { this.fade = fade; }

    public float getRGB()
    {
        return PeritiaClientConfig.UI_FADE_ANIMATION.get() && fade() ? Mth.clamp(screenTicks() / 8.0F, 0, 1) : 1;
    }

    public void tryExecute(Consumer<LocalPlayer> consumer) { if (player() != null) consumer.accept(Objects.requireNonNull(player())); }

    @Override
    @NotNull
    public ClientSkillData skillData() { return skillData; }

    public void setSkillData(@NotNull ClientSkillData skillData) { this.skillData = skillData; }

    @Override
    @NotNull
    public ClientXpSourceData xpSourceData() { return xpSourceData; }

    public void setXpSourceData(@NotNull ClientXpSourceData xpSourceData) { this.xpSourceData = xpSourceData; }

    @Override
    @NotNull
    public ClientAscensionSystem ascensionSystem() { return ascensionSystem; }

    public void setAscensionSystem(@NotNull ClientAscensionSystem ascensionSystem) { this.ascensionSystem = ascensionSystem; }

    @Override
    @NotNull
    public GameContext gameContext() { return gameContext; }

    public void setGameContext(GameContext gameContext) { this.gameContext = gameContext; }

    public SerializableDate lastLogin() { return gameContext().lastLogin(); }

    public int sessionTimeInSeconds()
    {
        return Math.min(-(int) (lastLogin().offsetFromNow() / 1000), totalTimeInSeconds());
    }

    public int totalTimeInSeconds()
    {
        int duration = viewingSkillData().timePlayed();
        int offset = -(int) (viewingSkillData().lastUpdate().offsetFromNow() / 1000);
        return duration + offset;
    }

    public int totalTimeInHours() { return totalTimeInSeconds() / 3600; }

    public int xpAtBeginning() { return xpAtBeginning; }

    public int xpThisSession() { return skillData().allTimeXp() - xpAtBeginning(); }

    public void setXp(int xp)
    {
        if (xpAtBeginning == -1) this.xpAtBeginning = xp;
    }

    public void resetXp() { this.xpAtBeginning = -1; }

    @NotNull
    public SimpleAttributeMap attributeMap() { return attributeMap; }

    public void setAttributeMap(SimpleAttributeMap attributeMap) { this.attributeMap = attributeMap; }

    @Nullable
    public LookContext lookContext() { return lookContext; }

    public void setLookContext(@Nullable LookContext lookContext) { this.lookContext = lookContext; }

    public void trySetLookContext(@Nullable LookContext lookContext)
    {
        if (minecraft().screen == null) setLookContext(lookContext);
    }

    public boolean hasLookContext() { return lookContext() != null; }

    public boolean isScreenAuthorized() { return !hasLookContext(); }

    @NotNull
    public ClientSkillData viewingSkillData()
    {
        return hasLookContext() ? Objects.requireNonNull(lookContext()).skillDataFromClient() : skillData();
    }

    @NotNull
    public SimpleAttributeMap viewingAttributeMap()
    {
        return hasLookContext() ? Objects.requireNonNull(lookContext()).attributeMap() : attributeMap();
    }

    @Nullable
    public XpGainContext xpGainContext() { return xpGainContext; }

    public void updateXpGainContext(@NotNull XpGainContext context, boolean playSound)
    {
        XpGainContext existing = this.xpGainContext;
        this.xpGainContext = existing == null ? context : existing.append(context);
        this.xpContextTicks = XP_CONTEXT_DURATION;
        assert xpGainContext != null;

        boolean soundConfig = PeritiaClientConfig.XP_GAIN_SOUND.get();
        int threshold = PeritiaClientConfig.PROGRESS_TEXT_THRESHOLD.get();
        if (threshold == -1) return;

        AtomicBoolean showedText = new AtomicBoolean(false);
        XpGainContext copy = xpGainContext.copy();
        copy.filter(threshold);

        tryExecute(player ->
        {
            if (minecraft().screen == null)
            {
                player.displayClientMessage(PeritiaTexts.xpGain(copy), true);
                showedText.set(true);
            }
        });

        if (soundConfig && showedText.get() && playSound)
        {
            Random random = new Random();
            float volume = random.nextFloat(0.5F, 0.75F);
            float pitch = random.nextFloat(0.8F, 1.25F);

            playLocalSound(PeritiaSoundEvents.XP_GAIN, volume, pitch);
        }
    }

    public void resetXpGainContext() { this.xpGainContext = null; }

    public LevelUpContext levelUpContext() { return levelUpContext; }

    public void updateLevelUpContext(@NotNull LevelUpContext context, boolean manually)
    {
        LevelUpContext existing = this.levelUpContext;
        this.levelUpContext = existing == null ? context : existing.append(context);
        this.levelContextTicks = LEVEL_CONTEXT_DURATION;
        this.leveledUpManually = manually;
    }

    public void resetLevelUpContext() { this.levelUpContext = null; }

    public void levelUp()
    {
        LevelUpContext context = levelUpContext();
        if (context == null) return;

        boolean soundConfig = PeritiaClientConfig.LEVEL_UP_SOUND.get();
        boolean toastConfig = PeritiaClientConfig.LEVEL_UP_TOAST.get();
        boolean particleConfig = PeritiaClientConfig.LEVEL_UP_PARTICLE.get();
        boolean textConfig = PeritiaClientConfig.LEVEL_UP_TEXT.get();

        if (soundConfig)
        {
            Random random = new Random();
            float volume = random.nextFloat(0.8F, 1.25F);
            float pitch = random.nextFloat(0.667F, 1);

            playLocalSound(PeritiaSoundEvents.LEVEL_UP, volume, pitch);
        }

        if (toastConfig)
        {
            context.forEachComponent(component ->
            {
                minecraft().getToasts().addToast(new LevelUpToast(component.skill(), component.oldLevel(), component.newLevel()));
            });
        }

        if (particleConfig)
        {
            context.forEachComponent(component -> addParticlesAroundPlayer(component.skill().particleType(), component.skill().particleCount()));
        }

        tryExecute(player ->
        {
            if (textConfig && minecraft().screen == null)
            {
                PeritiaTexts.$levelUp(context, leveledUpManually).forEach(text -> player.displayClientMessage(text, false));
            }
        });
    }

    public void levelUpReady(LevelUpReadyPacket packet)
    {
        boolean soundConfig = PeritiaClientConfig.LEVEL_UP_SOUND.get();
        boolean toastConfig = PeritiaClientConfig.LEVEL_UP_TOAST.get();
        boolean textConfig = PeritiaClientConfig.LEVEL_UP_TEXT.get();

        if (soundConfig && packet.playSound) playLocalSound(PeritiaSoundEvents.NOTIFY, 0.8F, 1);
        if (toastConfig) minecraft().getToasts().addToast(new LevelUpReadyToast(packet.skill));

        tryExecute(player ->
        {
            if (textConfig && minecraft().screen == null)
            {
                PeritiaTexts.$levelUpReady(packet.skill).forEach(text -> player.displayClientMessage(text, false));
            }
        });
    }

    @Override
    @NotNull
    public RecipeManager recipeManager() { return Objects.requireNonNull(minecraft().getConnection()).getRecipeManager(); }

    // ----------------------------------------- PACKET HANDLING -------------------------------------------------------

    @Override
    public void handlePeritiaResourcesPacket(PeritiaResourcesPacket packet)
    {
        PeritiaResources resources = PeritiaResources.tryLoad(packet.data);
        Peritia.resourceLoader().setClientResources(resources);
    }

    @Override
    public void handleGameContextPacket(GameContextPacket packet)
    {
        GameContext gameContext = GameContext.tryLoad(packet.data);
        setGameContext(gameContext);
        resetXp();
    }

    @Override
    public void handleLookContextPacket(LookContextPacket packet)
    {
        LookContext lookContext = packet.isNull ? null : LookContext.tryLoad(packet.data);
        trySetLookContext(lookContext);
    }

    @Override
    public void handleSkillDataPacket(SkillDataPacket packet)
    {
        ClientSkillData skillData = ClientSkillData.tryLoad(packet.data);
        setSkillData(skillData);
        setXp(skillData.allTimeXp());
    }

    @Override
    public void handleXpSourceDataPacket(XpSourceDataPacket packet)
    {
        ClientXpSourceData xpSourceData = ClientXpSourceData.tryLoad(packet.data);
        setXpSourceData(xpSourceData);
    }

    @Override
    public void handleAscensionSystemPacket(AscensionSystemPacket packet)
    {
        ClientAscensionSystem ascensionSystem = ClientAscensionSystem.tryLoad(packet.data);
        setAscensionSystem(ascensionSystem);
    }

    @Override
    public void handleGainXpPacket(XpGainPacket packet)
    {
        updateXpGainContext(packet.context, packet.playSound);
    }

    @Override
    public void handleLevelUpPacket(LevelUpPacket packet)
    {
        updateLevelUpContext(packet.context, packet.manually);
    }

    @Override
    public void handleLevelUpReadyPacket(LevelUpReadyPacket packet)
    {
        levelUpReady(packet);
    }

    @Override
    public void handleEncyclopediaUpdatePacket(EncyclopediaUpdatePacket packet)
    {
        if (PeritiaClientConfig.ENCYCLOPEDIA_TEXT.get())
        {
            subtitleTicks = (int) (70.0F * minecraft().options.notificationDisplayTime().get()) + 1;
        }
    }

    @Override
    public void handleEncyclopediaCompletePacket(EncyclopediaCompletePacket packet)
    {
        if (PeritiaClientConfig.ENCYCLOPEDIA_TOAST.get())
        {
            minecraft().getToasts().addToast(new EncyclopediaToast(packet.skill));
            playLocalSound(PeritiaSoundEvents.NOTIFY, 1, 1);
        }
    }

    @Override
    public void handleChestLuckPacket(ChestLuckPacket packet)
    {
        boolean toastConfig = PeritiaClientConfig.CHEST_LUCK_TOAST.get();
        if (toastConfig)
        {
            Item icon;
            ResourceLocation itemKey = ResourceLocation.tryParse(packet.itemKey);
            if (itemKey == null)
            {
               icon = Items.CHEST;
            }
            else
            {
                icon = ForgeRegistries.ITEMS.getValue(itemKey);
                if (icon == null || icon == Items.AIR) icon = Items.CHEST;
            }

            minecraft().getToasts().addToast(new ChestLuckToast(icon, packet.rolls));
            playLocalSound(PeritiaSoundEvents.NOTIFY, 1, 1);
        }
    }

    @Override
    public void handleRefreshScreenPacket(RefreshScreenPacket packet)
    {
        refreshScreen();
    }

    @Override
    public void handleInstructionsPacket(InstructionsPacket packet)
    {
        List<Component> texts;
        String string = getKeyMappingString();

        switch (packet.id)
        {
            case InstructionsPacket.SIMPLE_ID -> texts = List.of(PeritiaTexts.pressR(string));
            case InstructionsPacket.DETAILED_ID -> texts = PeritiaTexts.$detailedInstructions(string, false);
            case InstructionsPacket.DETAILED_CHEATS_ID -> texts = PeritiaTexts.$detailedInstructions(string, true);
            default -> texts = new ArrayList<>();
        }

        tryExecute(player ->
        {
            for (Component text : texts) player.displayClientMessage(text, false);
        });
    }

    @Override
    public void handleAttributePacket(AttributePacket packet)
    {
        SimpleAttributeMap attributeMap = SimpleAttributeMap.tryLoad(packet.data);
        setAttributeMap(attributeMap);
    }

}
