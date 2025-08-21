package github.poscard8.peritia.skill;

import com.google.gson.*;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.reward.RewardLike;
import github.poscard8.peritia.util.serialization.ElementSerializable;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.skill.SkillRequisite;
import github.poscard8.peritia.util.skill.SkillRequisites;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemLock implements SkillAssignable, RewardLike<ItemLock>, ElementSerializable<ItemLock>
{
    protected Item item;
    protected Skill skill;
    protected int until;
    protected JsonArray conditions = new JsonArray();

    public ItemLock(Item item, Skill skill, int until)
    {
        this.item = item;
        this.skill = skill;
        this.until = until;
    }

    public static ItemLock empty() { return new ItemLock(Items.AIR, Skill.empty(), Skill.DEFAULT_MIN_LEVEL); }

    public static ItemLock tryLoad(JsonElement data) { return empty().loadWithFallback(data); }

    public static SkillRequisites getRequisitesFor(Item item)
    {
        List<ItemLock> locks = new ArrayList<>();
        SkillRequisites requisites = SkillRequisites.empty();

        for (Skill skill : Peritia.skills()) locks.addAll(skill.itemLockList());

        for (ItemLock lock : locks)
        {
            if (lock.item() == item) requisites.requisites().add(lock.getRequisite());
        }
        return requisites;
    }

    public static boolean lockedForPlayer(Player player) { return !player.isCreative() && !player.isSpectator(); }

    public Item item() { return item; }

    public Skill skill() { return skill; }

    public int until() { return until; }

    public JsonArray conditions() { return conditions; }

    public boolean hasNoConditions() { return conditions().isEmpty(); }

    public boolean meetsConditions() { return CraftingHelper.processConditions(conditions(), ICondition.IContext.EMPTY); }

    public boolean doesNotMeetConditions() { return !meetsConditions(); }

    public SkillRequisite getRequisite() { return new SkillRequisite(skill().key(), SkillRequisite.Type.SINGLE, until()); }

    public SkillRequisites getAllRequisites() { return getRequisitesFor(item()); }

    public boolean isValid()
    {
        return item() != Items.AIR && !skill().isEmpty() && until() > skill().minLevel();
    }

    public boolean isInvalid() { return !isValid(); }

    public boolean isValidRewardLikeFor(@NotNull Skill skill, int level)
    {
        return getRequisite().isPossible() && getAllRequisites().milestoneStatus(skill, level).isValid();
    }

    @Override
    public boolean shouldDisplayText(Skill skill, int level) { return isValidRewardLikeFor(skill, level); }

    @Override
    public Component getText(Skill skill, int level)
    {
        SkillRequisites.MilestoneStatus milestoneStatus = getAllRequisites().milestoneStatus(skill, level);
        return Component.translatable("generic.peritia.item_lock", item().getDescription().getString())
                .withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(milestoneStatus.getText(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public int priority() { return 3; }

    @Override
    public ItemLock multiplyBy(int multiplier) { return this; }

    @Override
    public void assignSkill(@NotNull Skill skill)
    {
        this.skill = skill;
        this.until = Mth.clamp(until, skill.minLevel(), skill.maxLevel());
    }

    @Override
    public ItemLock fallback() { return empty(); }

    @Override
    public ItemLock load(JsonElement data)
    {
        String string;

        if (data.isJsonPrimitive() && data.getAsJsonPrimitive().isString())
        {
            string = data.getAsString();
        }
        else if (data.isJsonObject())
        {
            JsonObject jsonObject = data.getAsJsonObject();
            string = JsonHelper.readString(jsonObject, "lock");

            this.conditions = JsonHelper.readArray(jsonObject, "conditions", conditions);
        }
        else throw new JsonSyntaxException("Item locks are either strings or JSON objects");

        String[] split = string.split(",");
        String stringKey = split[0];
        ResourceLocation key = ResourceLocation.tryParse(stringKey);
        assert key != null;

        this.item = ForgeRegistries.ITEMS.getValue(key);
        this.until = Integer.parseInt(split[1]);

        return this;
    }

    @Override
    public JsonElement save()
    {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item());
        String stringKey = key == null ? "minecraft:air" : key.toString();
        String string = String.format("%s,%d", stringKey, until);

        if (hasNoConditions()) return new JsonPrimitive(string);

        JsonObject data = new JsonObject();
        JsonHelper.write(data, "lock", string);
        JsonHelper.write(data, "conditions", conditions);

        return data;
    }


}
