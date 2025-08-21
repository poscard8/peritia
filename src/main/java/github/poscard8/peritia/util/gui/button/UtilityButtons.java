package github.poscard8.peritia.util.gui.button;

import github.poscard8.peritia.util.text.PeritiaTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static github.poscard8.peritia.client.screen.PeritiaMainScreen.textureLocation;

@OnlyIn(Dist.CLIENT)
public class UtilityButtons
{
    public static CompactButton back(int x, int y, Button.OnPress onPress)
    {
        return new CompactButton(x, y, 9, 12, 194, 0, textureLocation(), onPress).setText(PeritiaTexts.back()).setRequireAuthorization(false);
    }

    public static CompactButton info(int x, int y, Button.OnPress onPress)
    {
        return new CompactButton(x, y, 5, 16, 231, 0, textureLocation(), onPress).setText(PeritiaTexts.encyclopedia().copy().withStyle(ChatFormatting.GOLD));
    }

    public static CompactButton previous(int x, int y, Button.OnPress onPress)
    {
        return new CompactButton(x, y, 14, 20, 203, 0, textureLocation(), onPress).setText(PeritiaTexts.previousPage()).setRequireAuthorization(false);
    }

    public static CompactButton next(int x, int y, Button.OnPress onPress)
    {
        return new CompactButton(x, y, 14, 20, 217, 0, textureLocation(), onPress).setText(PeritiaTexts.nextPage()).setRequireAuthorization(false);
    }

}
