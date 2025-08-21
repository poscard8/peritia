package github.poscard8.peritia.util.gui.widget;

import github.poscard8.peritia.util.text.PeritiaTexts;
import github.poscard8.peritia.util.text.TextGetter;
import github.poscard8.peritia.util.xpsource.ResourceInput;
import github.poscard8.peritia.util.xpsource.ResourceInputs;
import github.poscard8.peritia.xpsource.type.AdvancementXpSource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class AdvancementXpSourceWidget extends XpSourceWidget<AdvancementXpSource>
{
    protected final TextGetter textGetter;
    protected boolean singular = false;

    @Nullable
    protected ResourceLocation advancementKey = null;

    public AdvancementXpSourceWidget(AdvancementXpSource xpSource, int x, int y)
    {
        super(xpSource, x, y);

        ResourceInputs inputs = xpSource.inputs();
        if (inputs.inputs().size() == 1)
        {
            if (inputs.inputs().get(0) instanceof ResourceInput.Single single)
            {
                singular = true;
                advancementKey = single.key();
            }
        }
        this.textGetter = singular ? TextGetter.empty() : TextGetter.advancement(inputs);
    }

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

        if (singular())
        {
            assert advancementKey != null;

            Component text = Component.translatable("xp_source.peritia.advancement.singular");
            guiGraphics.drawString(font(), text, Math.round((getX() + 4) * inverse), Math.round((getY() + 17) * inverse), primaryTextColor(), false);

            Component nameText = PeritiaTexts.advancement(advancementKey).plainCopy();
            float textWidth = font().width(nameText) * ratio;
            int maxWidth = WIDTH - 8;

            if (textWidth <= maxWidth)
            {
                guiGraphics.drawString(font(), nameText, Math.round((getX() + ((WIDTH - textWidth) / 2)) * inverse), Math.round((getY() + 29) * inverse), primaryTextColor(), false);
            }
            else guiGraphics.drawWordWrap(font(), nameText, Math.round((getX() + 4) * inverse), Math.round((getY() + 29) * inverse), Math.round(maxWidth * inverse), primaryTextColor());
        }
        else
        {
            Component text = Component.translatable("xp_source.peritia.advancement.plural");

            guiGraphics.drawString(font(), text, Math.round((getX() + ((WIDTH - ratio * font().width(text)) / 2)) * inverse), Math.round((getY() + 25.5F) * inverse),
                    primaryTextColor(), false);
        }

        guiGraphics.pose().scale(1, 1, 1);
        guiGraphics.pose().popPose();
    }

    public List<Component> getTexts() { return textGetter.apply(clientHandler()); }

    public boolean singular() { return singular; }

}
