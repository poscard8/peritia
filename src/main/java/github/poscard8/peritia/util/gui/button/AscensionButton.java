package github.poscard8.peritia.util.gui.button;

import github.poscard8.peritia.client.toast.AscensionToast;
import github.poscard8.peritia.config.PeritiaClientConfig;
import github.poscard8.peritia.config.PeritiaServerConfig;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.serverbound.AscendPacket;
import github.poscard8.peritia.reward.RewardLike;
import github.poscard8.peritia.util.text.ExtraTextColors;
import github.poscard8.peritia.util.text.PeritiaTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class AscensionButton extends CompactButton
{
    public boolean confirming = false;
    protected int oldScore = 0;
    protected int ascensionTick = -1;

    public AscensionButton(int x, int y)
    {
        super(x, y, press());

        setItem(ascensionSystem().icon());
        setCountText(button ->
        {
            if (!isScreenAuthorized()) return PeritiaTexts.empty();

            ChatFormatting color = skillData().canAscend() ? ChatFormatting.WHITE : ChatFormatting.RED;
            return skillData().hasExtraLegacyScore() ? PeritiaTexts.makePlus(skillData().extraLegacyScore(), color) : PeritiaTexts.makeText(0, ChatFormatting.GRAY);
        });
        setNotifyText(button -> skillData().hasExtraLegacyScore() && skillData().canAscend() ? PeritiaTexts.magentaExclamationMark() : PeritiaTexts.empty());
    }

    protected static OnPress press()
    {
        return button0 ->
        {
            if (!(button0 instanceof AscensionButton button)) return;

            if (button.skillData().canAscend())
            {
                if (button.confirming)
                {
                    button.oldScore = button.skillData().legacyScore();

                    PeritiaNetworkHandler.sendToServer(new AscendPacket());
                    button.scheduleAscension();
                }
                else button.confirming = true;
            }
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int key)
    {
        boolean xCheck = mouseX >= getX() && mouseX < getX() + width;
        boolean yCheck = mouseY >= getY() && mouseY < getY() + height;
        boolean leftClick = key == 0;
        boolean notClicked = !(xCheck && yCheck && leftClick);

        if (notClicked) this.confirming = false;
        return super.mouseClicked(mouseX, mouseY, key);
    }

    @Override
    public List<Component> getTexts()
    {
        if (!isScreenAuthorized()) return getNonAuthorizedTexts();

        List<Component> texts = new ArrayList<>();
        texts.add(PeritiaTexts.legacyTitle());
        texts.add(PeritiaTexts.empty());

        if (confirming)
        {
            String translationSuffix = "generic.peritia.ascension_warning.";
            ChatFormatting color = ChatFormatting.RED;

            texts.add(Component.translatable(translationSuffix + 0).withStyle(color));

            int messageIndex = 0;

            if (PeritiaServerConfig.KEEP_SKILL_RECIPES.get()) messageIndex += 1;
            if (PeritiaServerConfig.KEEP_UNLOCKED_ITEMS.get()) messageIndex += 2;

            if (messageIndex > 0) texts.add(Component.translatable(translationSuffix + messageIndex).withStyle(color));

            List<RewardLike<?>> rewardLikes2 = skillData().legacy().getRewardLikes(skillData().pendingLegacyScore());
            if (rewardLikes2.isEmpty())
            {
                texts.add(Component.translatable(translationSuffix + 4).withStyle(color));
            }
            else
            {
                texts.add(PeritiaTexts.ascendInfo());

                for (RewardLike<?> rewardLike : rewardLikes2)
                {
                    if (rewardLike.hasText()) texts.add(PeritiaTexts.space().append(rewardLike.tryGetText()));
                }
            }
            texts.add(PeritiaTexts.empty());
            texts.add(PeritiaTexts.clickToConfirm());
        }
        else
        {
            int totalXp = skillData().totalXp();
            int allTimeXp = skillData().allTimeXp();
            int sessionXp = clientHandler().xpThisSession();

            texts.add(PeritiaTexts.totalXp().append(PeritiaTexts.makeText(totalXp, ChatFormatting.DARK_AQUA)));

            Component innerText = PeritiaTexts.makeText(allTimeXp).withStyle(ExtraTextColors.DARKER_AQUA);
            Component text = PeritiaTexts.doubleSpace().append(Component.translatable("label.peritia.all_time", innerText).withStyle(ExtraTextColors.DARKER_AQUA));

            Component innerText2 = PeritiaTexts.makeText(sessionXp).withStyle(ExtraTextColors.DARKER_AQUA);
            Component text2 = PeritiaTexts.doubleSpace().append(Component.translatable("label.peritia.this_session", innerText2).withStyle(ExtraTextColors.DARKER_AQUA));

            texts.add(text);
            texts.add(text2);
            texts.add(PeritiaTexts.empty());

            if (skillData().canAscend())
            {
                texts.add(PeritiaTexts.yourLegacyScore().append(PeritiaTexts.makeText(skillData().legacyScore(), ChatFormatting.WHITE)));
                texts.add(PeritiaTexts.ascensionCount(skillData().ascensionCount()));
                texts.add(PeritiaTexts.empty());

                List<RewardLike<?>> rewardLikes = skillData().legacy().getExistingRewardLikes();
                if (!rewardLikes.isEmpty())
                {
                    texts.add(PeritiaTexts.currentRewards());

                    for (RewardLike<?> rewardLike : rewardLikes)
                    {
                        if (rewardLike.hasText()) texts.add(PeritiaTexts.space().append(rewardLike.tryGetText()));
                    }
                    texts.add(PeritiaTexts.empty());
                }

                texts.add(PeritiaTexts.pendingLegacyScore().append(PeritiaTexts.makeText(skillData().extraLegacyScore(), ChatFormatting.LIGHT_PURPLE)));

                Component innerText3 = PeritiaTexts.makeText(skillData().legacy().xpRequiredForNextPoint(skillData().allTimeXp()), ChatFormatting.DARK_PURPLE);
                Component text3 = Component.translatable("generic.peritia.next_legacy_point", innerText3).withStyle(ChatFormatting.GRAY);

                texts.add(text3);
                texts.add(PeritiaTexts.empty());

                List<RewardLike<?>> rewardLikes2 = skillData().legacy().getRewardLikes(skillData().pendingLegacyScore());
                if (!rewardLikes2.isEmpty())
                {
                    texts.add(PeritiaTexts.ascendNowInfo());

                    for (RewardLike<?> rewardLike : rewardLikes2)
                    {
                        if (rewardLike.hasText()) texts.add(PeritiaTexts.space().append(rewardLike.tryGetText()));
                    }
                    texts.add(PeritiaTexts.empty());
                }
                if (skillData().isAscensionCooldownFinished())
                {
                    texts.add(PeritiaTexts.clickToAscend());
                }
                else texts.add(PeritiaTexts.ascensionCooldown(skillData().lastAscended()));
            }
            else texts.add(PeritiaTexts.ascensionsDisabled());
        }
        return texts;
    }

    public List<Component> getNonAuthorizedTexts()
    {
        List<Component> texts = new ArrayList<>();
        texts.add(PeritiaTexts.legacyTitle());
        texts.add(PeritiaTexts.empty());

        int totalXp = viewingSkillData().totalXp();
        int allTimeXp = viewingSkillData().allTimeXp();

        texts.add(PeritiaTexts.totalXp().append(PeritiaTexts.makeText(totalXp, ChatFormatting.DARK_AQUA)));

        Component innerText = PeritiaTexts.makeText(allTimeXp).withStyle(ExtraTextColors.DARKER_AQUA);
        Component text = PeritiaTexts.doubleSpace().append(Component.translatable("label.peritia.all_time", innerText).withStyle(ExtraTextColors.DARKER_AQUA));
        texts.add(text);

        if (viewingSkillData().canAscend())
        {
            texts.add(PeritiaTexts.empty());
            texts.add(PeritiaTexts.legacyScore().append(PeritiaTexts.makeText(skillData().legacyScore(), ChatFormatting.WHITE)));
        }

        List<RewardLike<?>> rewardLikes = viewingSkillData().legacy().getExistingRewardLikes();
        if (!rewardLikes.isEmpty())
        {
            texts.add(PeritiaTexts.currentRewards());

            for (RewardLike<?> rewardLike : rewardLikes)
            {
                if (rewardLike.hasText()) texts.add(PeritiaTexts.space().append(rewardLike.tryGetText()));
            }
        }
        return texts;
    }

    @Override
    public boolean canClick() { return skillData().isAscensionCooldownFinished() && super.canClick(); }

    public void tick()
    {
        tryAscendClientSide();
    }

    public void scheduleAscension() { ascensionTick = screenTicks() + 3; }

    public void tryAscendClientSide()
    {
        if (screenTicks() >= ascensionTick && ascensionTick != -1) ascendClientSide();
    }

    public void ascendClientSide()
    {
        Screen screen = minecraft().screen;
        if (screen != null) screen.onClose();

        boolean soundConfig = PeritiaClientConfig.ASCENSION_SOUND.get();
        boolean toastConfig = PeritiaClientConfig.ASCENSION_TOAST.get();
        boolean particleConfig = PeritiaClientConfig.ASCENSION_PARTICLE.get();
        boolean textConfig = PeritiaClientConfig.ASCENSION_TEXT.get();

        List<RewardLike<?>> rewardLikes = skillData().legacy().getRewardLikes(oldScore, skillData().legacyScore());
        List<Component> texts = PeritiaTexts.$ascension(rewardLikes);

        if (soundConfig) clientHandler().playLocalSound(ascensionSystem().sound(), 1.25F, 1);
        if (toastConfig) minecraft().getToasts().addToast(new AscensionToast());
        if (particleConfig) clientHandler().addParticlesAroundPlayer(ascensionSystem().particleType(), ascensionSystem().particleCount());
        if (textConfig)
        {
            for (Component text : texts) player().displayClientMessage(text, false);
        }
        oldScore = skillData().legacyScore();
        ascensionTick = -1;
    }



}
