package github.poscard8.peritia.util.gui;

import github.poscard8.peritia.client.screen.SkillScreen;
import github.poscard8.peritia.skill.LevelLayout;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.SkillInstance;
import github.poscard8.peritia.util.gui.button.SkillMilestoneButton;
import github.poscard8.peritia.util.skill.LevelLayoutHelper;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SkillTab implements PeritiaUIElement
{
    public static final int WIDTH = 160;
    public static final int HEIGHT = 96;

    protected final SkillScreen screen;
    protected final List<SkillMilestoneButton> buttons = new ArrayList<>();

    public final int x;
    public final int y;
    protected final Skill skill;
    protected final LevelLayout layout;
    protected final int fullWidth;

    protected int currentX = 0;

    public SkillTab(int x, int y, SkillScreen screen, Skill skill)
    {
        this.x = x;
        this.y = y;
        this.screen = screen;
        this.skill = skill;
        this.layout = skill.levelLayout();

        int fullWidth = layout.getXOffset(skill.maxLevel()) + LevelLayoutHelper.EDGE_THICKNESS + LevelLayoutHelper.BUTTON_SIZE;
        this.fullWidth = Math.max(fullWidth, WIDTH);

        init();
    }

    protected void init()
    {
        for (int i = 1; i <= skill.maxLevel(); i++)
        {
            SkillMilestoneButton button = new SkillMilestoneButton(skill, i, this);
            buttons.add(button);
            screen.$addWidget(button);
        }

        int deltaX = layout.getXOffset(1) - layout.getXOffset(instance().levelOfInterest()) + 66;
        move(deltaX);
    }

    public void refresh()
    {
        buttons.forEach(screen::$removeWidget);
        buttons.clear();
        currentX = 0;

        init();
    }

    public void move(int x)
    {
        int newX = Mth.clamp(currentX - x, 1, fullWidth - WIDTH - 1);

        try
        {
            buttons.forEach(button -> button.move(currentX - newX, 0));
            currentX = newX;
        }
        catch (ConcurrentModificationException ignored) {}
    }

    public boolean isInside(double mouseX, double mouseY)
    {
        boolean xCheck = mouseX >= x && mouseX < x + WIDTH;
        boolean yCheck = mouseY >= y && mouseY < y + HEIGHT;
        return xCheck && yCheck;
    }

    public SkillInstance instance() { return viewingSkillData().getSkill(skill); }

}
