package github.poscard8.peritia.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.client.menu.ProfileMenu;
import github.poscard8.peritia.config.PeritiaClientConfig;
import github.poscard8.peritia.network.ClientHandler;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.serverbound.OpenMenuPacket;
import github.poscard8.peritia.registry.PeritiaAttributes;
import github.poscard8.peritia.util.gui.PeritiaUIElement;
import github.poscard8.peritia.util.gui.TextureStyle;
import github.poscard8.peritia.util.gui.button.AscensionButton;
import github.poscard8.peritia.util.gui.button.CompactButton;
import github.poscard8.peritia.util.gui.button.UtilityButtons;
import github.poscard8.peritia.util.text.PeritiaTexts;
import github.poscard8.peritia.util.text.TextGetter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public final class ProfileScreen extends AbstractContainerScreen<ProfileMenu> implements PeritiaUIElement
{
    public final CompactButton[] buttons = new CompactButton[6];

    @SuppressWarnings("unused")
    public ProfileScreen(ProfileMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, PeritiaTexts.playerName(ClientHandler.getInstance().player(), ClientHandler.getLookContext()).plainCopy());
        titleLabelY -= 4;
    }

    public static ResourceLocation textureLocation()
    {
        TextureStyle textureStyle = PeritiaClientConfig.UI_TEXTURE_STYLE.get();
        return Peritia.asResource(String.format("textures/gui/profile%s.png", textureStyle.suffix()));
    }

    @Override
    protected void init()
    {
        super.init();

        clientHandler().setScreenTicks(0);
        clientHandler().setFade(false);

        Player player = player();
        assert player != null;

        int wisdom = viewingAttributeMap().roundedValueOf(PeritiaAttributes.WISDOM.get());
        int chestLuck = viewingAttributeMap().roundedValueOf(PeritiaAttributes.CHEST_LUCK.get());

        buttons[0] = UtilityButtons.back(leftPos - 13, topPos, button -> PeritiaNetworkHandler.sendToServer(OpenMenuPacket.mainMenu(false)));
        buttons[1] = new CompactButton(leftPos + 43, topPos + 22).setTextGetter(TextGetter.highestSkill()).setSkill(viewingSkillData().highestSkill()).setCountText(button ->
        {
            int level = viewingSkillData().getLevel(Objects.requireNonNull(button.skill()));
            return PeritiaTexts.makeText(level);
        });
        buttons[2] = new AscensionButton(leftPos + 79, topPos + 22);
        buttons[3] = new CompactButton(leftPos + 115, topPos + 22).setTextGetter(TextGetter.timePlayed()).setItem(Items.CLOCK).setCountText(clientHandler().totalTimeInHours());
        buttons[4] = new CompactButton(leftPos + 61, topPos + 40).setTextGetter(TextGetter.wisdom()).setItem(Items.ENCHANTED_BOOK).setCountText(wisdom);
        buttons[5] = new CompactButton(leftPos + 97, topPos + 40).setTextGetter(TextGetter.chestLuck()).setItem(Items.CHEST).setCountText(chestLuck);


        for (CompactButton button : buttons) addRenderableWidget(button);
    }

    @Override
    protected void containerTick()
    {
        super.containerTick();
        clientHandler().addScreenTick();

        if (isScreenAuthorized()) ((AscensionButton) buttons[2]).tick();
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

        guiGraphics.blit(textureLocation(), leftPos - 18, topPos - 4, 0, 0, 212, 170);
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
    public int getXSize() { return super.getXSize() + 16; }

}
