package github.poscard8.peritia.util.gui.widget;

import github.poscard8.peritia.util.text.TextGetter;
import github.poscard8.peritia.xpsource.type.SocialXpSource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class SocialXpSourceWidget extends XpSourceWidget<SocialXpSource>
{
    public SocialXpSourceWidget(SocialXpSource xpSource, int x, int y) { super(xpSource, x, y); }


    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta)
    {
        super.renderWidget(guiGraphics, mouseX, mouseY, delta);
        if (isHovered()) guiGraphics.renderTooltip(font(), getTexts(), Optional.empty(), mouseX, mouseY);
    }

    @Override
    protected void renderTexts(GuiGraphics guiGraphics)
    {
        super.renderTexts(guiGraphics);

        float ratio = 0.75F;
        float inverse = 1 / ratio;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(ratio, ratio, ratio);

        Component text = Component.translatable("xp_source.peritia.social");

        guiGraphics.drawString(font(), text, Math.round((getX() + ((WIDTH - ratio * font().width(text)) / 2)) * inverse), Math.round((getY() + 25.5F) * inverse),
                primaryTextColor(), false);

        guiGraphics.pose().scale(1, 1, 1);
        guiGraphics.pose().popPose();
    }

    public List<Component> getTexts()
    {
        try
        {
            TextGetter textGetter = TextGetter.remainingTimeSocial(xpSource(), Objects.requireNonNull(minecraft().getConnection()).getOnlinePlayers().size());
            return textGetter.apply(clientHandler());
        }
        catch (Exception exception) { return List.of(); }
    }

}
