package github.poscard8.peritia.client.menu;

import github.poscard8.peritia.registry.PeritiaMenuTypes;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public class ProfileMenu extends PeritiaMainMenu
{
    public ProfileMenu(int id, Inventory inventory) { this(PeritiaMenuTypes.PROFILE.get(), id, inventory); }

    public ProfileMenu(MenuType<?> menuType, int id, Inventory inventory) { super(menuType, id, inventory, PROFILE_MENU_OFFSET); }

    public static MenuProvider provider(Player player)
    {
        return new SimpleMenuProvider((id, inventory, serverPlayer) -> new ProfileMenu(id, inventory) , player.getName().plainCopy());
    }

}
