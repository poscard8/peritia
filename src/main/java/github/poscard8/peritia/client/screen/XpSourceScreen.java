package github.poscard8.peritia.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.config.PeritiaClientConfig;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.serverbound.OpenMenuPacket;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.gui.PeritiaUIElement;
import github.poscard8.peritia.util.gui.TextureStyle;
import github.poscard8.peritia.util.gui.button.*;
import github.poscard8.peritia.util.gui.widget.CoveredXpSourceWidget;
import github.poscard8.peritia.util.gui.widget.XpSourceWidget;
import github.poscard8.peritia.util.text.PeritiaTexts;
import github.poscard8.peritia.util.xpsource.XpSourceSortFunction;
import github.poscard8.peritia.xpsource.XpSource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public final class XpSourceScreen extends Screen implements PeritiaUIElement
{
    public static final int WIDGETS_PER_PAGE = 8;

    Skill skill;
    int leftPos;
    int topPos;

    List<XpSourceWidget<?>> widgets;
    SortButton sortButton;
    PreviousSkillButton previousSkillButton;
    NextSkillButton nextSkillButton;
    CompactButton previousButton;
    CompactButton nextButton;

    boolean hasHidden;

    int page;
    int pageCount;
    int firstPage;
    int lastPage;

    public XpSourceScreen(Skill skill)
    {
        super(PeritiaTexts.xpSourceTitle(skill));
        this.skill = skill;
        this.width = 196;
        this.height = 210;
    }

    public static ResourceLocation textureLocation()
    {
        TextureStyle textureStyle = PeritiaClientConfig.UI_TEXTURE_STYLE.get();
        return Peritia.asResource(String.format("textures/gui/xp_source%s.png", textureStyle.suffix()));
    }

    @Override
    protected void init()
    {
        clientHandler().setScreenTicks(0);
        clientHandler().setFade(false);

        this.leftPos = 142;
        this.topPos = 32;

        addRenderableWidget(UtilityButtons.back(leftPos - 13, topPos + 4, button -> PeritiaNetworkHandler.sendToServer(OpenMenuPacket.skillMenu(skill))));

        List<XpSource> xpSources = skill.getXpSourcesForSelf(skillData());
        widgets = new ArrayList<>();
        hasHidden = false;

        for (XpSource xpSource : xpSources)
        {
            @Nullable XpSourceWidget<?> widget = XpSourceWidget.placeNew(xpSource, 0, 0);
            if (widget != null)
            {
                if (widget.shouldShow()) widgets.add(widget);
                if (widget.shouldHide() && !hasHidden) hasHidden = true;
            }
        }

        widgets.forEach(widget -> widget.assignSkill(skill));
        int size = widgets.size();

        pageCount = Math.max(1, (size + WIDGETS_PER_PAGE - 1) / WIDGETS_PER_PAGE);
        firstPage = 0;
        lastPage = pageCount - 1;

        sortButton = new SortButton(leftPos + 108, topPos - 13, button0 ->
        {
            if (button0 instanceof SortButton button)
            {
                XpSourceSortFunction next = button.nextSortFunction();

                button.sortFunction = next;
                preferences().setSortFunction(skill, next);
                sortWidgets(next);

                openPage(page);
            }
        });
        previousButton = UtilityButtons.previous(leftPos - 21, topPos + 90, button -> openPreviousPage());
        nextButton = UtilityButtons.next(leftPos + 203, topPos + 90, button -> openNextPage());

        sortButton.sortFunction = preferences().getSortFunction(skill);
        addRenderableWidget(sortButton);

        previousSkillButton = new PreviousSkillButton(skill, leftPos + 200, topPos + 4);
        nextSkillButton = new NextSkillButton(skill, leftPos + 213, topPos + 4);

        if (previousSkillButton.shouldPlace()) addRenderableWidget(previousSkillButton);
        if (nextSkillButton.shouldPlace()) addRenderableWidget(nextSkillButton);

        sortWidgets();
        openPage(0);
    }

    @Override
    public void tick()
    {
        super.tick();
        clientHandler().addScreenTick();
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta)
    {
        renderBackground(guiGraphics);
        renderBg(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTitle(guiGraphics);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int key, int scan, int modifier)
    {
        if (key == 0x41 || key == 0x107) // keys A or <-
        {
            openPreviousPage();
            return true;
        }
        else if (key == 0x44 || key == 0x106) // keys D or ->
        {
            openNextPage();
            return true;
        }
        return super.keyPressed(key, scan, modifier);
    }

    @Override
    public void mouseMoved(double p_94758_, double p_94759_) {
        super.mouseMoved(p_94758_, p_94759_);
    }

    void renderBg(GuiGraphics guiGraphics)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        handleShaderColor();

        guiGraphics.blit(textureLocation(), leftPos, topPos, 0, 0, 196, 210, 512, 256);
    }

    void renderTitle(GuiGraphics guiGraphics)
    {
        guiGraphics.drawString(font(), getTitle(), leftPos + 8, topPos + 6, 4210752, false);
    }

    void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
        children().forEach(guiEventListener ->
        {
            if (guiEventListener instanceof CompactButton button) button.renderTooltip(guiGraphics, mouseX, mouseY);
        });
    }

    public void $addWidget(AbstractWidget widget) { addRenderableWidget(widget); }

    public void $removeWidget(AbstractWidget widget) { removeWidget(widget); }

    List<XpSourceWidget<?>> widgetsOnPage() { return widgetsOnPage(this.page); }

    List<XpSourceWidget<?>> widgetsOnPage(int page)
    {
        int fromIndex = page * WIDGETS_PER_PAGE;
        int toIndex = Math.min(fromIndex + WIDGETS_PER_PAGE, widgets.size());

        return widgets.subList(fromIndex, toIndex);
    }

    void sortWidgets() { sortWidgets(sortButton.sortFunction); }

    void sortWidgets(XpSourceSortFunction sortFunction)
    {
        widgets.removeIf(widget -> widget instanceof CoveredXpSourceWidget);
        widgets.sort(sortFunction);
        widgets.forEach(widget -> widget.removeSelf(this));

        if (hasHidden) widgets.add(new CoveredXpSourceWidget(0, 0));

        for (int i = 0; i < widgets.size(); i++)
        {
            int relativeIndex = i % WIDGETS_PER_PAGE;
            int row = relativeIndex / 2;
            int column = relativeIndex % 2;

            XpSourceWidget<?> widget = widgets.get(i);
            widget.setPos((leftPos + 8) + column * XpSourceWidget.WIDTH, (topPos + 18) + row * XpSourceWidget.HEIGHT);
        }
    }

    void openPage(int page)
    {
        closePage();
        this.page = page;
        widgetsOnPage().forEach(widget -> widget.addSelf(this));

        if (pageCount > 1 && page != firstPage) addRenderableWidget(previousButton);
        if (pageCount > 1 && page != lastPage) addRenderableWidget(nextButton);
    }

    void openPreviousPage()
    {
        if (page > firstPage) openPage(page - 1);
    }

    void openNextPage()
    {
        if (page < lastPage) openPage(page + 1);
    }

    void closePage()
    {
        widgetsOnPage(page).forEach(xpSourceWidget -> xpSourceWidget.removeSelf(this));
        removeWidget(previousButton);
        removeWidget(nextButton);
    }


}
