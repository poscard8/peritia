package github.poscard8.peritia.client.menu;

import github.poscard8.peritia.registry.PeritiaMenuTypes;
import github.poscard8.peritia.util.text.PeritiaTexts;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class SkillRecipeMenu extends PeritiaMainMenu
{
    public SkillRecipeMenu(int id, Inventory inventory) { this(PeritiaMenuTypes.SKILL_RECIPE.get(), id, inventory); }

    public SkillRecipeMenu(MenuType<?> menuType, int id, Inventory inventory) { super(menuType, id, inventory, SKILL_RECIPE_MENU_OFFSET); }

    public static MenuProvider provider()
    {
        return new SimpleMenuProvider((id, inventory, player) -> new SkillRecipeMenu(id, inventory) , PeritiaTexts.skillRecipesTitle());
    }

}
