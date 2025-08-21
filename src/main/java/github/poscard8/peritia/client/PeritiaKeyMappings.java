package github.poscard8.peritia.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PeritiaKeyMappings
{
    public static final KeyMapping MAIN_MENU = new KeyMapping("key.peritia.main_menu", InputConstants.KEY_R, "key.categories.peritia");

}
