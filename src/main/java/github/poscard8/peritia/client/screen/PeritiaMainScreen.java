package github.poscard8.peritia.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.client.PeritiaKeyMappings;
import github.poscard8.peritia.client.menu.PeritiaMainMenu;
import github.poscard8.peritia.config.PeritiaClientConfig;
import github.poscard8.peritia.network.ClientHandler;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.serverbound.ClaimAllPacket;
import github.poscard8.peritia.network.packet.serverbound.OpenMenuPacket;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.gui.PeritiaUIElement;
import github.poscard8.peritia.util.gui.TextureStyle;
import github.poscard8.peritia.util.gui.button.CompactButton;
import github.poscard8.peritia.util.gui.button.SkillMenuButton;
import github.poscard8.peritia.util.text.PeritiaTexts;
import github.poscard8.peritia.util.text.TextGetter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * Main screen of the mod.
 */
@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public final class PeritiaMainScreen extends AbstractContainerScreen<PeritiaMainMenu> implements PeritiaUIElement
{
    public boolean fade;
    public boolean canClose;

    public PeritiaMainScreen(PeritiaMainMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, validateTitle(title));
        this.fade = menu.fade();
        titleLabelY += 9;
        inventoryLabelY += 13;
    }

    public static ResourceLocation textureLocation()
    {
        TextureStyle textureStyle = PeritiaClientConfig.UI_TEXTURE_STYLE.get();
        return Peritia.asResource(String.format("textures/gui/main%s.png", textureStyle.suffix()));
    }

    public static Component validateTitle(Component title)
    {
        return ClientHandler.getLookContext() == null ? title : title.copy().append(PeritiaTexts.ofPlayer(ClientHandler.getLookContext()));
    }

    @Override
    protected void init()
    {
        super.init();

        clientHandler().setScreenTicks(0);
        clientHandler().setFade(fade);

        ItemStack playerHead = Items.PLAYER_HEAD.getDefaultInstance();
        String playerName = lookContext() == null ? player().getName().getString() : Objects.requireNonNull(lookContext()).playerName();

        playerHead.getOrCreateTag().putString("SkullOwner", playerName);

        for (Skill skill : Peritia.skills()) addRenderableWidget(SkillMenuButton.withOffset(skill, leftPos + 7, topPos + 27));

        if (isScreenAuthorized())
        {
            addRenderableWidget(new CompactButton(leftPos + 61, topPos - 13, 176, 72, textureLocation(), button -> PeritiaNetworkHandler.sendToServer(new ClaimAllPacket())).setTextGetter(TextGetter.single(PeritiaTexts.claimAll())));
            addRenderableWidget(new CompactButton(leftPos + 97, topPos - 13, 176, 108, textureLocation(), button -> PeritiaNetworkHandler.sendToServer(OpenMenuPacket.skillRecipeMenu())).setText(PeritiaTexts.skillRecipesTitle().copy().withStyle(ChatFormatting.GOLD)));
        }

        addRenderableWidget(new CompactButton(leftPos + 79, topPos - 13, 176, 36, textureLocation(), button -> PeritiaNetworkHandler.sendToServer(OpenMenuPacket.profileMenu())).setTextGetter(TextGetter.profile()).setItemStack(playerHead).setCountText(button ->
                {
                    return isScreenAuthorized() && viewingSkillData().hasExtraLegacyScore() && viewingSkillData().canAscend() ? PeritiaTexts.makePlus(viewingSkillData().extraLegacyScore()) : Component.empty();
                })
                .setRequireAuthorization(false));

        canClose = false;
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

        guiGraphics.blit(textureLocation(), leftPos, topPos + 9, 0, 0, imageWidth, 170);
        guiGraphics.blit(textureLocation(), leftPos + 54, topPos - 20, 0, 170, 68, 29);
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
    public boolean keyPressed(int key, int scan, int modifier)
    {
        if (PeritiaKeyMappings.MAIN_MENU.matches(key, scan) && canClose)
        {
            onClose();
            return true;
        }
        else return super.keyPressed(key, scan, modifier);
    }

    @Override
    public boolean keyReleased(int key, int scan, int modifier)
    {
        canClose = true;
        return super.keyReleased(key, scan, modifier);
    }

}
