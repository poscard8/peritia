package github.poscard8.peritia.util.gui.button;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.client.screen.XpSourceScreen;
import github.poscard8.peritia.mixin.client.accessor.MouseHandlerInvoker;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.text.PeritiaTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static github.poscard8.peritia.client.screen.PeritiaMainScreen.textureLocation;

@OnlyIn(Dist.CLIENT)
public class NextSkillButton extends CompactButton
{
    public NextSkillButton(Skill skill, int x, int y)
    {
        super(x, y, 9, 12, 194, 24, textureLocation(), press());
        setSkill(skill);
        setTextGetter(clientHandler ->
        {
            List<Component> texts = new ArrayList<>();
            ChatFormatting[] colors = new ChatFormatting[]{ChatFormatting.WHITE, ChatFormatting.GRAY, ChatFormatting.DARK_GRAY};
            Skill[] skills = nextSkills();

            texts.add(PeritiaTexts.nextSkill());
            for (int i = 0; i < 3; i++)
            {
                Skill next = skills[i];
                if (next == null) break;

                texts.add(PeritiaTexts.skill(next, colors[i]));
            }
            return texts;
        });
    }

    protected static OnPress press()
    {
        return button0 ->
        {
            if (button0 instanceof NextSkillButton button)
            {
                Skill next = button.nextSkills()[0];
                if (next != null)
                {
                    Minecraft minecraft = Minecraft.getInstance();
                    MouseHandler mouseHandler = minecraft.mouseHandler;

                    double mouseX = mouseHandler.xpos();
                    double mouseY = mouseHandler.ypos();

                    Screen screen = minecraft.screen;
                    if (screen != null) screen.onClose();
                    minecraft.setScreen(new XpSourceScreen(next));

                    long window = minecraft.getWindow().getWindow();
                    ((MouseHandlerInvoker) mouseHandler).invokeMove(window, mouseX, mouseY);
                    GLFW.glfwSetCursorPos(window, mouseX, mouseY);
                }
            }
        };
    }

    public Skill[] nextSkills()
    {
        Skill[] allSkills = Peritia.skillHandler().valueArray();
        Skill[] reordered = new Skill[Skill.MAX_SKILL_COUNT];
        Skill[] skills = new Skill[3];
        if (Peritia.skills().size() < 2) return skills;

        assert skill != null;
        int originIndex = 0;
        int nextCount = 0;

        for (int i = 0; i < allSkills.length; i++)
        {
            if (skill.equals(allSkills[i]))
            {
                originIndex = i;
                break;
            }
        }

        for (int i = 0; i < reordered.length; i++)
        {
            int index = i >= originIndex ? i - originIndex : i + reordered.length - originIndex;
            reordered[index] = allSkills[i];
        }

        for (int i = 1; i < allSkills.length && nextCount < 3; i++)
        {
            if (reordered[i] != null) skills[nextCount++] = reordered[i];
        }
        return skills;
    }

    @Override
    public boolean renderSkillIcon() { return false; }

    @Override
    public boolean shouldRender(int mouseX, int mouseY)
    {
        int distanceX = Math.abs(mouseX - (getX()));
        int distanceY = Math.abs(mouseY - (getY() + 6));
        double deltaSquare = Math.pow(distanceX, 2) + Math.pow(distanceY, 2);

        return deltaSquare <= 576;
    }

    public boolean shouldPlace()
    {
        for (Skill skill : nextSkills())
        {
            if (skill != null) return true;
        }
        return false;
    }




}
