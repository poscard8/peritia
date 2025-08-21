package github.poscard8.peritia.util.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import github.poscard8.peritia.client.screen.XpSourceScreen;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.SkillAssignable;
import github.poscard8.peritia.util.gui.PeritiaUIElement;
import github.poscard8.peritia.util.text.PeritiaTexts;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.XpSourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public abstract class XpSourceWidget<T extends XpSource> extends AbstractWidget implements PeritiaUIElement, SkillAssignable
{
    public static final int WIDTH = 90;
    public static final int HEIGHT = 46;

    protected static final Map<XpSourceType<?>, Placer<?>> WIDGET_MAP = new HashMap<>();

    protected final T xpSource;
    protected Skill skill = null;
    protected Component skillTitle;
    protected Component xpTitle;

    public XpSourceWidget(T xpSource, int x, int y)
    {
        super(x, y, WIDTH, HEIGHT, Component.empty());
        this.xpSource = xpSource;
        this.skillTitle = PeritiaTexts.skillTitle(xpSource, null);
        this.xpTitle = PeritiaTexts.xpTitle(xpSource, (float) skillData().xpMultiplier());
    }

    public static <I extends XpSource> void registerForType(XpSourceType<I> type, Placer<I> placer) { WIDGET_MAP.put(type, placer); }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <I extends XpSource> XpSourceWidget<I> placeNew(I xpSource, int x, int y)
    {
        if (xpSource.isEmpty()) return null;
        
        Placer<?> placer = WIDGET_MAP.get(xpSource.type());
        if (placer == null) return null;

        Placer<I> casted = (Placer<I>) placer;
        return casted.place(xpSource, x, y);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta)
    {
        renderBg(guiGraphics);
        renderTexts(guiGraphics);
    }

    protected void renderBg(GuiGraphics guiGraphics)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        handleShaderColor();

        if (isHovered())
        {
            guiGraphics.blit(XpSourceScreen.textureLocation(), getX(), getY(), 90, 210, 90, 46, 512, 256);
        }
        else guiGraphics.blit(XpSourceScreen.textureLocation(), getX(), getY(), 0, 210, 90, 46, 512, 256);

    }

    protected void renderTexts(GuiGraphics guiGraphics)
    {
        int maxWidth = WIDTH - 8;
        int width = font().width(skillTitle()) + font().width(xpTitle()) + 2;

        if (width > maxWidth)
        {
            guiGraphics.drawString(font(), xpTitle(), getX() + 87 - font().width(xpTitle()), getY() + 4, secondaryTextColor(), false);

            int leftWidth = maxWidth - font().width(xpTitle()) - 2;
            MutableComponent spaced = skillTitle().copy().append(" ");
            int newWidth = font().width(spaced);

            Component doubleText = spaced.append(skillTitle().copy().append(" "));
            int xOffset = (screenTicks() % newWidth) - newWidth;

            guiGraphics.enableScissor(getX() + 4, getY() + 4, getX() + 4 + leftWidth, getY() + 13);
            guiGraphics.drawString(font(), doubleText, getX() + 4 + xOffset, getY() + 4, secondaryTextColor(), false);
            guiGraphics.disableScissor();
        }
        else
        {
            guiGraphics.drawString(font(), skillTitle(), getX() + 4, getY() + 4, secondaryTextColor(), false);
            guiGraphics.drawString(font(), xpTitle(), getX() + 87 - font().width(xpTitle()), getY() + 4, secondaryTextColor(), false);
        }
    }

    public void addSelf(XpSourceScreen screen) { screen.$addWidget(this); }

    public void removeSelf(XpSourceScreen screen) { screen.$removeWidget(this); }

    public void setPos(int x, int y)
    {
        setX(x);
        setY(y);
    }

    public T xpSource() { return xpSource; }

    public boolean shouldShow() { return xpSource().shouldShow(player(), skillData()); }

    public boolean shouldHide() { return xpSource().shouldHide(player(), skillData()); }

    public Skill skill() { return skill; }

    @Override
    public void assignSkill(Skill skill)
    {
        this.skill = skill;
        this.skillTitle = PeritiaTexts.skillTitle(xpSource(), skill());
    }

    public Component skillTitle() { return skillTitle; }

    public Component xpTitle() { return xpTitle; }

    public int primaryTextColor() { return isHovered() ? textureStyle().hoveredTextColor() : textureStyle().primaryTextColor(); }

    public int secondaryTextColor() { return isHovered() ? textureStyle().hoveredTextColor() : textureStyle().secondaryTextColor(); }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}


    public interface Placer<T extends XpSource>
    {
        XpSourceWidget<T> place(T xpSource, int x, int y);
    }

}
