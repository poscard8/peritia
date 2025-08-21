package github.poscard8.peritia.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.client.menu.SkillRecipeMenu;
import github.poscard8.peritia.config.PeritiaClientConfig;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.serverbound.OpenMenuPacket;
import github.poscard8.peritia.util.gui.PeritiaUIElement;
import github.poscard8.peritia.util.gui.TextureStyle;
import github.poscard8.peritia.util.gui.button.CompactButton;
import github.poscard8.peritia.util.gui.button.UtilityButtons;
import github.poscard8.peritia.util.gui.widget.SkillRecipeWidget;
import github.poscard8.peritia.util.text.PeritiaTexts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * Main screen of the mod.
 */
@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public final class SkillRecipeScreen extends AbstractContainerScreen<SkillRecipeMenu> implements PeritiaUIElement
{
    public static final int WIDGETS_PER_PAGE = 6;

    public LocalPlayer localPlayer = null;

    public EditBox editBox;
    public boolean editBoxSelected;
    public List<SkillRecipeWidget> allWidgets;
    public List<SkillRecipeWidget> filtered;
    CompactButton previousButton;
    CompactButton nextButton;
    int page;
    int pageCount;
    int firstPage;
    int lastPage;

    public SkillRecipeScreen(SkillRecipeMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, title);
        titleLabelY -= 3;
        inventoryLabelY += 19;
    }

    public static ResourceLocation textureLocation()
    {
        TextureStyle textureStyle = PeritiaClientConfig.UI_TEXTURE_STYLE.get();
        return Peritia.asResource(String.format("textures/gui/skill_recipe%s.png", textureStyle.suffix()));
    }

    @Override
    protected void init()
    {
        super.init();

        clientHandler().setScreenTicks(0);
        clientHandler().setFade(false);
        localPlayer = player();

        editBox = new EditBox(font(), leftPos + 61, topPos - 16, 71, 9, PeritiaTexts.search(textureStyle().searchTextColor()))
        {
            @Override
            public void insertText(String string)
            {
                super.insertText(string);
                SkillRecipeScreen.this.update();
            }

            @Override
            public void deleteChars(int count)
            {
                super.deleteChars(count);
                SkillRecipeScreen.this.update();
            }
        };
        editBoxSelected = false;
        page = 0;

        Integer searchTextColor = textureStyle().searchTextColor().getColor();
        if (searchTextColor == null) searchTextColor = 0xFFFFFF;

        editBox.setBordered(false);
        editBox.setHint(PeritiaTexts.search(textureStyle().searchTextColor()));
        editBox.setTextColor(searchTextColor);

        addRenderableWidget(editBox);
        addRenderableWidget(UtilityButtons.back(leftPos - 13, topPos + 1, button -> PeritiaNetworkHandler.sendToServer(OpenMenuPacket.mainMenu(false))));

        allWidgets = new ArrayList<>();
        filtered = new ArrayList<>();
        Peritia.skillRecipes().forEach(recipe -> allWidgets.add(new SkillRecipeWidget(recipe, leftPos + 8, topPos + 15)));

        previousButton = UtilityButtons.previous(leftPos - 21, topPos + 41, button -> openPreviousPage());
        nextButton = UtilityButtons.next(leftPos + 183, topPos + 41, button -> openNextPage());

        update();
    }

    @Override
    protected void containerTick()
    {
        super.containerTick();
        clientHandler().addScreenTick();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta)
    {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        handleShaderColor();

        RenderSystem.enableBlend();
        guiGraphics.blit(textureLocation(), leftPos, topPos - 3, 0, 0, 176, 188);
        guiGraphics.blit(textureLocation(), leftPos + 38, topPos - 24, 0, 188, 100, 21);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        children().forEach(guiEventListener ->
        {
            if (guiEventListener instanceof CompactButton button) button.renderTooltip(guiGraphics, mouseX, mouseY);
        });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int key)
    {
        boolean xCheck = mouseX >= leftPos + 57 && mouseX < leftPos + 133;
        boolean yCheck = mouseY >= topPos - 19 && mouseY < topPos - 6;

        editBoxSelected = xCheck && yCheck;
        editBox.setFocused(editBoxSelected);
        return super.mouseClicked(mouseX, mouseY, key);
    }

    @Override
    public boolean keyPressed(int key, int scan, int modifier)
    {
        if (!editBoxSelected)
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
        }
        return super.keyPressed(key, scan, modifier);
    }

    @Override
    public int getXSize() { return super.getXSize() + 16; }

    public void $addWidget(AbstractWidget widget) { addRenderableWidget(widget); }

    public void $removeWidget(AbstractWidget widget) { removeWidget(widget); }

    public String getSearchString() { return editBox.getValue(); }

    public void update()
    {
        filtered.forEach(widget -> widget.removeSelf(this));
        filter();
        setupPages();
        setupWidgets();
        openPage(0);
    }

    void filter()
    {
        filtered = allWidgets.stream().filter(widget -> widget.isRelatedTo(getSearchString())).toList();
    }

    void setupPages()
    {
        int size = filtered.size();

        this.page = 0;
        this.pageCount = Math.max(1, (size + WIDGETS_PER_PAGE - 1) / WIDGETS_PER_PAGE);
        this.firstPage = 0;
        this.lastPage = pageCount - 1;
    }

    void setupWidgets()
    {
        for (int i = 0; i < filtered.size(); i++)
        {
            int relativeIndex = i % WIDGETS_PER_PAGE;
            int row = relativeIndex / 2;
            int column = relativeIndex % 2;

            SkillRecipeWidget widget = filtered.get(i);
            widget.setPos(row, column);
        }
    }

    List<SkillRecipeWidget> widgetsOnPage(int page)
    {
        int fromIndex = page * WIDGETS_PER_PAGE;
        int toIndex = Math.min(fromIndex + WIDGETS_PER_PAGE, filtered.size());
        return filtered.subList(fromIndex, toIndex);
    }

    void openPage(int page)
    {
        closePage();
        this.page = page;
        widgetsOnPage(page).forEach(widget -> widget.addSelf(this));

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
        widgetsOnPage(page).forEach(widget -> widget.removeSelf(this));
        removeWidget(previousButton);
        removeWidget(nextButton);
    }

}
