package github.poscard8.peritia.util.gui.widget;

import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.xpsource.XpSource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class CoveredXpSourceWidget extends XpSourceWidget<XpSource>
{
    public CoveredXpSourceWidget(int x, int y)
    {
        super(XpSource.empty(), x, y);
        this.skillTitle = Component.literal("???");
        this.xpTitle = Component.literal("???");
    }

    @Override
    protected void renderTexts(GuiGraphics guiGraphics)
    {
        super.renderTexts(guiGraphics);

        String outer = "?";
        int width = font().width(outer);
        int offset = 9;

        guiGraphics.drawString(font(), outer, getX() + ((WIDTH - width) / 2) + offset, getY() + 26, primaryTextColor(), false);

        offset = -8;
        guiGraphics.drawString(font(), outer, getX() + ((WIDTH - width) / 2) + offset, getY() + 26, primaryTextColor(), false);

        float ratio = 1.5F;
        float inverse = 1 / ratio;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(ratio, ratio, ratio);

        String inner = "?";
        int width2 = font().width(inner);

        guiGraphics.drawString(font(), inner, Math.round((getX() - 0.75F + (WIDTH - width2) / 2.0F) * inverse), Math.round((getY() + 23.5F) * inverse), primaryTextColor(), false);

        guiGraphics.pose().scale(1, 1, 1);
        guiGraphics.pose().popPose();
    }

    @Override
    public void assignSkill(Skill skill) {}

}
