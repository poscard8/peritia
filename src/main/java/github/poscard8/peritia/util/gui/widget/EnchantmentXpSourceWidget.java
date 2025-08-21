package github.poscard8.peritia.util.gui.widget;

import github.poscard8.peritia.util.text.TextGetter;
import github.poscard8.peritia.xpsource.type.EnchantmentXpSource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class EnchantmentXpSourceWidget extends XpSourceWidget<EnchantmentXpSource>
{
    protected final TextGetter textGetter;

    public EnchantmentXpSourceWidget(EnchantmentXpSource xpSource, int x, int y)
    {
        super(xpSource, x, y);
        this.textGetter = TextGetter.enchantments(xpSource.xp());
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

        Component text = Component.translatable("xp_source.peritia.enchantment");

        guiGraphics.drawString(font(), text, Math.round((getX() + ((WIDTH - ratio * font().width(text)) / 2)) * inverse), Math.round((getY() + 25.5F) * inverse),
                primaryTextColor(), false);

        guiGraphics.pose().scale(1, 1, 1);
        guiGraphics.pose().popPose();
    }

    public List<Component> getTexts() { return textGetter.apply(clientHandler()); }

}
