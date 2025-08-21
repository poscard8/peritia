package github.poscard8.peritia.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.client.menu.SkillMenu;
import github.poscard8.peritia.config.PeritiaClientConfig;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.serverbound.OpenMenuPacket;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.gui.PeritiaUIElement;
import github.poscard8.peritia.util.gui.SkillTab;
import github.poscard8.peritia.util.gui.TextureStyle;
import github.poscard8.peritia.util.gui.TextureWrapper;
import github.poscard8.peritia.util.gui.button.CompactButton;
import github.poscard8.peritia.util.gui.button.UtilityButtons;
import github.poscard8.peritia.util.serialization.Proportions;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Main screen of the mod.
 */
@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public final class SkillScreen extends AbstractContainerScreen<SkillMenu> implements PeritiaUIElement
{
    Skill skill;
    int width;
    int height;

    SkillTab skillTab;
    float deltaX;

    public SkillScreen(SkillMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, PeritiaMainScreen.validateTitle(title));

        this.skill = menu.skill();
        this.width = skill.textures().menuSize().x();
        this.height = skill.textures().menuSize().y();
        titleLabelY -= 24;
        inventoryLabelY += 22;
    }

    public static ResourceLocation textureLocation()
    {
        TextureStyle textureStyle = PeritiaClientConfig.UI_TEXTURE_STYLE.get();
        return Peritia.asResource(String.format("textures/gui/skill%s.png", textureStyle.suffix()));
    }

    @Override
    protected void init()
    {
        super.init();

        clientHandler().setScreenTicks(0);
        clientHandler().setFade(false);

        this.skillTab = new SkillTab(x0(), y0(), this, skill);
        this.deltaX = 0;

        addRenderableWidget(UtilityButtons.back(leftPos - 13, topPos - 20, button -> PeritiaNetworkHandler.sendToServer(OpenMenuPacket.mainMenu(false))));

        if (isScreenAuthorized())
        {
            addRenderableWidget(UtilityButtons.info(leftPos + 182, topPos - 22, button ->
            {
                onClose();
                minecraft().setScreen(new XpSourceScreen(skill()));
            }));
        }
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
        handleButtons(guiGraphics, mouseX, mouseY, delta);
        renderOverlay(guiGraphics);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        handleShaderColor();

        RenderSystem.enableBlend();
        guiGraphics.blit(texture(), leftPos + menuOffset().x(), topPos + menuOffset().y() - 24, 0, 0, menuSize().x(), menuSize().y(), menuImageSize().x(), menuImageSize().y());
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        try
        {
            children().forEach(guiEventListener ->
            {
                if (guiEventListener instanceof CompactButton button && button.shouldRender(mouseX, mouseY))
                {
                    button.renderTooltip(guiGraphics, mouseX, mouseY);
                }
            });
        }
        catch (Exception ignored) {}
    }

    public void renderOverlay(GuiGraphics guiGraphics)
    {
        ResourceLocation texture = overlayTexture();
        if (texture == null || !shouldRenderOverlay()) return;

        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 380.0D);

        RenderSystem.enableBlend();
        guiGraphics.blit(texture, leftPos + overlayOffset().x(), topPos + overlayOffset().y() - 24, 0, 0, overlaySize().x(), overlaySize().y(), overlayImageSize().x(), overlayImageSize().y());
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    public void handleButtons(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta)
    {
        try
        {
            guiGraphics.enableScissor(x0(), y0(), x1(), y1());

            children().forEach(guiEventListener ->
            {
                if (guiEventListener instanceof AbstractWidget widget) widget.render(guiGraphics, mouseX, mouseY, delta);
            });
            guiGraphics.disableScissor();
        }
        catch (Exception ignored) {}
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrolls)
    {
        deltaX = 9 * (float) scrolls;
        moveTab();
        return super.mouseScrolled(mouseX, mouseY, scrolls);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int key, double dx, double dy)
    {
        deltaX = (float) dx;
        moveTab();
        return super.mouseDragged(mouseX, mouseY, key, dx, dy);
    }

    @Override
    public boolean keyPressed(int key, int scan, int modifier)
    {
        if (key == 0x41 || key == 0x107) // keys A or <-
        {
            deltaX = 22;
            moveTab();
            return true;
        }
        else if (key == 0x44 || key == 0x106) // keys D or ->
        {
            deltaX = -22;
            moveTab();
            return true;
        }
        return super.keyPressed(key, scan, modifier);
    }

    @Override
    public int getXSize()
    {
        int menuSize = menuSize().x() + menuOffset().x();
        int overlaySize = overlaySize().x() + overlayOffset().x();

        return Math.max(Math.max(menuSize, overlaySize), 186);
    }

    @Override
    public void refresh() { skillTab.refresh(); }

    public ResourceLocation texture()
    {
        boolean canUseCustom = hasCustomEnabled() && skill().textures().menuTexture() != null;
        return canUseCustom ? skill().textures().menuTexture() : textureLocation();
    }

    @Nullable
    public ResourceLocation overlayTexture()
    {
        return hasCustomEnabled() && skill().textures().overlayTexture() != null ? skill().textures().overlayTexture() : null;
    }

    public Skill skill() { return skill; }

    public Proportions menuImageSize() { return hasCustomEnabled() ? skill().textures().menu().imageSize() : TextureWrapper.DEFAULT_IMAGE_SIZE; }

    public Proportions menuSize() { return hasCustomEnabled() ? skill().textures().menuSize() : TextureWrapper.DEFAULT_MENU_SIZE; }

    public Proportions menuOffset()  { return hasCustomEnabled() ? skill().textures().menuOffset() : Proportions.empty(); }

    public Proportions overlayImageSize() { return hasCustomEnabled() ? skill().textures().overlay().imageSize() : TextureWrapper.DEFAULT_IMAGE_SIZE; }

    public Proportions overlaySize() { return hasCustomEnabled() ? skill().textures().overlaySize() : TextureWrapper.DEFAULT_MENU_SIZE; }

    public Proportions overlayOffset() { return hasCustomEnabled() ? skill().textures().overlayOffset() : Proportions.empty(); }

    public boolean hasCustomEnabled() { return PeritiaClientConfig.CUSTOM_SKILL_MENUS.get(); }
    
    public boolean shouldRenderOverlay() { return isScreenAuthorized() || skill().textures().alwaysRenderOverlay(); }

    public int x0() { return leftPos + 8; }

    public int x1() { return x0() + 160; }

    public int y0() { return topPos - 6; }

    public int y1() { return y0() + 96; }

    public void $addWidget(AbstractWidget widget) { addWidget(widget); }

    public void $removeWidget(AbstractWidget widget) { removeWidget(widget); }

    public void moveTab()
    {
        skillTab.move(Math.round(deltaX));
        deltaX = 0;
    }

}
