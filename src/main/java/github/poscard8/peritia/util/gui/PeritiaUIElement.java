package github.poscard8.peritia.util.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import github.poscard8.peritia.ascension.ClientAscensionSystem;
import github.poscard8.peritia.client.MenuPreferences;
import github.poscard8.peritia.config.PeritiaClientConfig;
import github.poscard8.peritia.network.PeritiaClientHandler;
import github.poscard8.peritia.skill.data.ClientSkillData;
import github.poscard8.peritia.util.minecraft.GameContext;
import github.poscard8.peritia.util.minecraft.LookContext;
import github.poscard8.peritia.util.minecraft.SimpleAttributeMap;
import github.poscard8.peritia.xpsource.data.ClientXpSourceData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public interface PeritiaUIElement
{
    @NotNull
    default PeritiaClientHandler clientHandler() { return PeritiaClientHandler.getInstance(); }

    @NotNull
    default Minecraft minecraft() { return clientHandler().minecraft(); }

    default LocalPlayer player() { return minecraft().player; }

    default Font font() { return minecraft().font; }

    @NotNull
    default MenuPreferences preferences() { return MenuPreferences.getInstance(); }

    default ClientSkillData skillData() { return clientHandler().skillData(); }

    default ClientXpSourceData xpSourceData() { return clientHandler().xpSourceData(); }

    default ClientAscensionSystem ascensionSystem() { return clientHandler().ascensionSystem(); }

    default GameContext gameContext() { return clientHandler().gameContext(); }

    default LookContext lookContext() { return clientHandler().lookContext(); }

    default boolean isScreenAuthorized() { return clientHandler().isScreenAuthorized(); }

    default ClientSkillData viewingSkillData() { return clientHandler().viewingSkillData(); }

    default SimpleAttributeMap viewingAttributeMap() { return clientHandler().viewingAttributeMap(); }

    default SimpleAttributeMap attributeMap() { return clientHandler().attributeMap(); }

    default TextureStyle textureStyle() { return PeritiaClientConfig.UI_TEXTURE_STYLE.get(); }

    default int screenTicks() { return clientHandler().screenTicks(); }

    default void refresh() {}

    default void handleShaderColor()
    {
        float rgb = clientHandler().getRGB();
        RenderSystem.setShaderColor(rgb, rgb, rgb, 1.0F);
    }

}
