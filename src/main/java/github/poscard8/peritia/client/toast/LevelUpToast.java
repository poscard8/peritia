package github.poscard8.peritia.client.toast;

import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.gui.PeritiaToast;
import github.poscard8.peritia.util.gui.button.CompactButton;
import github.poscard8.peritia.util.text.PeritiaTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LevelUpToast implements Toast, PeritiaToast
{
    public static final long DISPLAY_TIME_MILLISECONDS = 3000;

    protected final Skill skill;
    protected final int oldLevel;
    protected final int newLevel;

    public LevelUpToast(Skill skill, int oldLevel, int newLevel)
    {
        this.skill = skill;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public Skill skill() { return skill; }

    public int oldLevel() { return oldLevel; }

    public int newLevel() { return newLevel; }


    @Override
    public Visibility render(GuiGraphics guiGraphics, ToastComponent toasts, long milliseconds)
    {
        renderBg(guiGraphics);
        CompactButton.renderSkillIcon(guiGraphics, 8, 8, skill());

        guiGraphics.drawString(font(), PeritiaTexts.levelUp(), 30, 7, 0xFFAA00);
        guiGraphics.drawString(font(), PeritiaTexts.levelUpIndicator(skill(), oldLevel(), newLevel(), ChatFormatting.WHITE), 30, 18, 0xFFFFFF);

        return milliseconds >= DISPLAY_TIME_MILLISECONDS ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public int getTextureYStart() { return 32; }

}
