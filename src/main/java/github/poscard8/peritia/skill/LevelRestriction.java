package github.poscard8.peritia.skill;

import com.google.gson.JsonObject;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import github.poscard8.peritia.util.skill.AtFunction;
import github.poscard8.peritia.util.skill.AtFunctionHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class LevelRestriction implements SkillAssignable, JsonSerializable<LevelRestriction>, AtFunctionHolder
{
    protected Skill skill;
    protected AtFunction at;
    protected Item item;
    protected int count;

    public LevelRestriction(Skill skill, AtFunction at, Item item, int count)
    {
        this.skill = skill;
        this.at = at;
        this.item = item;
        this.count = count;
    }

    public static LevelRestriction empty() { return new LevelRestriction(Skill.empty(), AtFunction.empty(), Items.AIR, 0); }

    public static LevelRestriction tryLoad(JsonObject data) { return empty().loadWithFallback(data); }

    @Override
    public AtFunction at() { return at; }

    public Item item() { return item; }

    public ItemStack itemStack() { return item() != null ? new ItemStack(Objects.requireNonNull(item()), count()) : ItemStack.EMPTY; }

    public int count() { return count; }

    public boolean checkInventory(ServerPlayer player) { return checkInventory(player.getInventory()); }

    public boolean checkInventory(Inventory inventory)
    {
        for (ItemStack stack : inventory.items)
        {
            if (stack.getItem() == item() && stack.getCount() >= count()) return true;
        }
        return false;
    }

    public void takeItem(ServerPlayer player)
    {
        for (ItemStack stack : player.getInventory().items)
        {
            if (stack.getItem() == item() && stack.getCount() >= count())
            {
                stack.shrink(count());
                break;
            }
        }
        player.inventoryMenu.sendAllDataToRemote();
    }

    @Override
    public void assignSkill(Skill skill) { this.skill = skill; }

    @Override
    public LevelRestriction fallback() { return empty(); }

    @Override
    public LevelRestriction load(JsonObject data)
    {
        this.at = JsonHelper.readElementSerializable(data, "at", AtFunction::tryLoad, at);
        this.item = JsonHelper.readRegistrable(data, "item", ForgeRegistries.ITEMS, item);
        this.count = JsonHelper.readInt(data, "count", count);

        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();
        JsonHelper.write(data, "at", at);
        JsonHelper.write(data, "item", item, ForgeRegistries.ITEMS);
        JsonHelper.write(data, "count", count);

        return data;
    }

}
