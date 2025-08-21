package github.poscard8.peritia.skill.recipe;

import com.google.gson.JsonObject;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class SkillRecipeInput implements JsonSerializable<SkillRecipeInput>
{
    protected Ingredient ingredient = Ingredient.EMPTY;
    protected int count = 0;

    public SkillRecipeInput() {}

    public static SkillRecipeInput empty() { return new SkillRecipeInput(); }

    public static SkillRecipeInput acceptAll() { return AcceptAll.INSTANCE; }

    public static SkillRecipeInput tryLoad(JsonObject data)
    {
        return data.size() == 0 ? acceptAll() : empty().loadWithFallback(data, acceptAll());
    }

    public Ingredient ingredient() { return ingredient; }

    public int count() { return count; }

    public boolean isValid() { return !ingredient().isEmpty() && count() > 0; }

    public boolean isInvalid() { return !isValid(); }

    public boolean check(Player player)
    {
        if (player.isCreative()) return true;
        int itemCount = 0;

        Inventory inventory = player.getInventory();
        for (ItemStack stack : inventory.items)
        {
            if (ingredient().test(stack)) itemCount += stack.getCount();
        }
        return itemCount >= count();
    }

    public void pay(Player player)
    {
        if (player.isCreative()) return;
        int paid = 0;

        Inventory inventory = player.getInventory();
        for (ItemStack stack : inventory.items)
        {
            if (ingredient().test(stack))
            {
                while (paid < count() && stack.getCount() > 0)
                {
                    stack.shrink(1);
                    paid++;
                }
            }
            if (paid == count()) break;
        }
    }

    public ItemStack[] getStacks()
    {
        ItemStack[] stacks = ingredient().getItems();
        for (ItemStack stack : stacks) stack.setCount(count());
        return stacks;
    }

    public List<Item> getItems()
    {
        ItemStack[] stacks = ingredient().getItems();
        List<Item> items = new ArrayList<>();

        for (ItemStack stack : stacks)
        {
            Item item = stack.getItem();
            if (!items.contains(item)) items.add(item);
        }
        return items;
    }

    @Override
    public SkillRecipeInput fallback() { return empty(); }

    @Override
    public SkillRecipeInput load(JsonObject data)
    {
        this.ingredient = JsonHelper.readIngredient(data, "ingredient", ingredient);
        this.count = JsonHelper.readInt(data, "count", count);
        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();
        JsonHelper.write(data, "ingredient", ingredient);
        JsonHelper.write(data, "count", count);

        return data;
    }


    public static class AcceptAll extends SkillRecipeInput
    {
        private static final AcceptAll INSTANCE = new AcceptAll();

        private AcceptAll() {}

        @Override
        public boolean isValid() { return true; }

        @Override
        public boolean check(Player player) { return true; }

        @Override
        public void pay(Player player) {}

        @Override
        public List<Item> getItems() { return new ArrayList<>(); }

        @Override
        public SkillRecipeInput load(JsonObject data) { return this; }

        @Override
        public JsonObject save() { return new JsonObject(); }

    }

}
