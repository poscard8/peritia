package github.poscard8.peritia.client.toast;

import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.gui.PeritiaToast;
import github.poscard8.peritia.util.text.PeritiaTexts;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EncyclopediaToast implements Toast, PeritiaToast
{
    public static final long DISPLAY_TIME_MILLISECONDS = 2000;

    protected final Skill skill;

    public EncyclopediaToast(Skill skill) { this.skill = skill; }

    public Skill skill() { return skill; }

    @Override
    public Visibility render(GuiGraphics guiGraphics, ToastComponent toasts, long milliseconds)
    {
        renderBg(guiGraphics);
        guiGraphics.renderItem(Items.WRITABLE_BOOK.getDefaultInstance(), 8, 8);

        guiGraphics.drawString(font(), PeritiaTexts.encyclopediaComplete(), 30, 7, 0xFFAA00);
        guiGraphics.drawString(font(), PeritiaTexts.skill(skill()), 30, 18, 0xFFFFFF);

        return milliseconds >= DISPLAY_TIME_MILLISECONDS ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public int getTextureYStart() { return 128; }

}
