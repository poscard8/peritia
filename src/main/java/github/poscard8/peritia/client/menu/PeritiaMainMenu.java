package github.poscard8.peritia.client.menu;

import github.poscard8.peritia.registry.PeritiaMenuTypes;
import github.poscard8.peritia.util.text.PeritiaTexts;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Simple menu with inventory + hotbar slots (36 in total).
 */
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class PeritiaMainMenu extends AbstractContainerMenu
{
    public static final int MAIN_MENU_OFFSET = 13;
    public static final int SKILL_MENU_OFFSET = 22;
    public static final int SKILL_RECIPE_MENU_OFFSET = 19;
    public static final int PROFILE_MENU_OFFSET = 0;

    protected final boolean fade;

    public PeritiaMainMenu(int id, Inventory inventory) { this(id == 1, inventory); }

    public PeritiaMainMenu(boolean fade, Inventory inventory) { this(PeritiaMenuTypes.MAIN.get(), fade, inventory); }

    public PeritiaMainMenu(MenuType<?> menuType, boolean fade, Inventory inventory)
    {
        super(menuType, id(fade));

        for(int x = 0; x < 9; ++x) addSlot(new Slot(inventory, x, 8 + x * 18, 142 + MAIN_MENU_OFFSET));

        for(int y = 1; y < 4; ++y)
        {
            for(int x = 0; x < 9; ++x) addSlot(new Slot(inventory, x + y * 9, 8 + x * 18, 66 + y * 18 + MAIN_MENU_OFFSET));
        }

        this.fade = fade;
    }

    public PeritiaMainMenu(MenuType<?> menuType, int id, Inventory inventory, int offset)
    {
        super(menuType, id);
        this.fade = false;

        for(int x = 0; x < 9; ++x) addSlot(new Slot(inventory, x, 8 + x * 18, 142 + offset));

        for(int y = 1; y < 4; ++y)
        {
            for(int x = 0; x < 9; ++x) addSlot(new Slot(inventory, x + y * 9, 8 + x * 18, 66 + y * 18 + offset));
        }
    }

    public static MenuProvider provider(byte data)
    {
        return new SimpleMenuProvider((id, inventory, player) -> new PeritiaMainMenu(data == 1, inventory) , PeritiaTexts.skillsTitle());
    }

    protected static int id(boolean fade) { return fade ? 1 : 0; }

    public boolean fade() { return fade; }

    @Override
    @NotNull
    public ItemStack quickMoveStack(Player player, int index)
    {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot.hasItem())
        {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (index >= 9 && index < 36)
            {
                if (!moveItemStackTo(stack, 0, 9, false)) return ItemStack.EMPTY;
            }
            else if (index < 9)
            {
                if (!moveItemStackTo(stack, 9, 36, false)) return ItemStack.EMPTY;
            }
            else if (!moveItemStackTo(stack, 0, 36, false)) return ItemStack.EMPTY;


            if (stack.isEmpty()) { slot.set(ItemStack.EMPTY); } else slot.setChanged();
            if (stack.getCount() == result.getCount()) return ItemStack.EMPTY;

            slot.onTake(player, stack);
            if (index == 0) player.drop(stack, false);
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) { return true; }

}
