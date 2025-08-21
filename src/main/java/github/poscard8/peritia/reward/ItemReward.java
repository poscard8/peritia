package github.poscard8.peritia.reward;

import com.google.gson.JsonObject;
import github.poscard8.peritia.registry.PeritiaRewardTypes;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.PeritiaHelper;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.skill.AtFunction;
import github.poscard8.peritia.util.text.PeritiaTexts;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class ItemReward extends Reward
{
    protected ItemStack item;

    public ItemReward(AtFunction at, ItemStack item)
    {
        super(at);
        this.item = item;
    }

    public static ItemReward empty() { return new ItemReward(AtFunction.empty(), ItemStack.EMPTY); }

    public static ItemReward tryLoad(JsonObject data)
    {
        return empty().loadWithFallback(data) instanceof ItemReward itemReward ? itemReward : empty();
    }

    @Override
    public RewardType<?> type() { return PeritiaRewardTypes.ITEM.get(); }

    public ItemStack item() { return item.copy(); }

    @Override
    public ItemReward multiplyBy(int multiplier)
    {
        ItemStack newStack = item();
        newStack.setCount(item.getCount() * multiplier);
        return new ItemReward(at(), newStack);
    }

    @Override
    public boolean shouldDisplayText(Skill skill, int level) { return !item().isEmpty(); }

    @Override
    public Component getText(Skill skill, int level) { return PeritiaTexts.itemStack(item()); }

    @Override
    public void award(ServerPlayer player) { player.getInventory().placeItemBackInInventory(item()); }

    @Override
    public Reward load(JsonObject data)
    {
        this.at = JsonHelper.readElementSerializable(data, "at", AtFunction::tryLoad, at);
        this.item = ShapedRecipe.itemStackFromJson(data);

        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = PeritiaHelper.serializeItem(item);
        JsonHelper.write(data, "at", at);

        return data;
    }

}
