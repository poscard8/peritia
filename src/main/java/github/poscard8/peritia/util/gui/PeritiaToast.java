package github.poscard8.peritia.util.gui;

import github.poscard8.peritia.Peritia;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.resources.ResourceLocation;

public interface PeritiaToast extends PeritiaUIElement
{
    ResourceLocation VANILLA_TEXTURE_FILE = Toast.TEXTURE;
    ResourceLocation MOD_TEXTURE_FILE = Peritia.asResource("textures/gui/toasts.png");

    int WIDTH = 160;
    int HEIGHT = 32;

    int getTextureYStart();

    default void renderBg(GuiGraphics guiGraphics)
    {
        if (textureStyle() == TextureStyle.VANILLA)
        {
            guiGraphics.blit(VANILLA_TEXTURE_FILE, 0, 0, 0, 0, WIDTH, HEIGHT);
        }
        else guiGraphics.blit(MOD_TEXTURE_FILE, 0, 0, 0, getTextureYStart(), WIDTH, HEIGHT);
    }

}
