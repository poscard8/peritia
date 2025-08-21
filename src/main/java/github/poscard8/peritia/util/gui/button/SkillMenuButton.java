package github.poscard8.peritia.util.gui.button;

import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.serverbound.OpenMenuPacket;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.SkillInstance;
import github.poscard8.peritia.util.text.TextGetter;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Button that opens the skill menu.
 */
@OnlyIn(Dist.CLIENT)
public class SkillMenuButton extends CompactButton
{
    public SkillMenuButton(Skill skill, int x, int y)
    {
        super(x, y, press(skill));
        setSkill(skill);
        setTextGetter(TextGetter.skillMenu(skill));
        setNotifyText(button0 ->
        {
            if (button0 instanceof SkillMenuButton button)
            {
                return button.skillInstance().status().text();
            }
            return Component.empty();
        });
        setRequireAuthorization(false);
    }

    public static SkillMenuButton withOffset(Skill skill, int x, int y) { return new SkillMenuButton(skill, x + skill.column() * 18, y + skill.row() * 18); }

    protected static OnPress press(Skill skill)
    {
        return button -> PeritiaNetworkHandler.sendToServer(OpenMenuPacket.skillMenu(skill));
    }

    public SkillInstance skillInstance() { return viewingSkillData().getSkill(skill); }

}
