package github.poscard8.peritia.client.toast;

import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.gui.PeritiaToast;
import github.poscard8.peritia.util.gui.button.CompactButton;
import github.poscard8.peritia.util.text.PeritiaTexts;
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
public class LevelUpReadyToast implements Toast, PeritiaToast
{
    public static final long DISPLAY_TIME_MILLISECONDS = 3000;

    protected final Skill skill;

    public LevelUpReadyToast(Skill skill) { this.skill = skill; }

    public Skill skill() { return skill; }

    @Override
    public Visibility render(GuiGraphics guiGraphics, ToastComponent toasts, long milliseconds)
    {
        renderBg(guiGraphics);
        CompactButton.renderSkillIcon(guiGraphics, 8, 8, skill());

        guiGraphics.drawString(font(), PeritiaTexts.levelUpReady(), 30, 7, 0xFFAA00);
        guiGraphics.drawString(font(), PeritiaTexts.skill(skill()), 30, 18, 0xFFFFFF);

        return milliseconds >= DISPLAY_TIME_MILLISECONDS ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public int getTextureYStart() { return 0; }

}
