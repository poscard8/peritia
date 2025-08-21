package github.poscard8.peritia.client.menu;

import github.poscard8.peritia.registry.PeritiaMenuTypes;
import github.poscard8.peritia.skill.Skill;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class SkillMenu extends PeritiaMainMenu
{
    protected final Skill skill;

    public SkillMenu(int id, Inventory inventory) { this(Skill.byIndex(id), inventory); }

    public SkillMenu(Skill skill, Inventory inventory) { this(PeritiaMenuTypes.SKILL.get(), skill, inventory); }

    public SkillMenu(MenuType<?> menuType, Skill skill, Inventory inventory)
    {
        super(menuType, skill.positionIndex(), inventory, SKILL_MENU_OFFSET);
        this.skill = skill;
    }

    public static MenuProvider provider(byte positionIndex)
    {
        Skill skill = Skill.byIndex(positionIndex);
        return new SimpleMenuProvider((id, inventory, player) -> new SkillMenu(skill, inventory) , skill.plainName());
    }

    public Skill skill() { return skill; }

}
