package github.poscard8.peritia.skill.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.advancement.PeritiaAdvancementTriggers;
import github.poscard8.peritia.reward.RewardLike;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.data.ServerSkillData;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import github.poscard8.peritia.util.serialization.Loadable;
import github.poscard8.peritia.util.skill.SkillRequisites;
import github.poscard8.peritia.util.text.ExtraTextColors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SkillRecipe implements Loadable, RewardLike<SkillRecipe>, JsonSerializable<SkillRecipe>, Comparable<SkillRecipe>
{
    protected ResourceLocation key;
    protected String translationKey;
    protected SkillRequisites requisites = SkillRequisites.empty();
    protected SkillRecipeInput input = SkillRecipeInput.acceptAll();
    protected SkillRecipeInput input2 = SkillRecipeInput.acceptAll();
    protected ItemStack result = ItemStack.EMPTY;
    protected short index = 0;
    protected boolean trade = false;
    protected boolean hasText = true;
    protected JsonArray conditions = new JsonArray();

    public SkillRecipe(ResourceLocation key) { this.key = key; }

    public static SkillRecipe empty() { return empty(EMPTY_KEY); }

    public static SkillRecipe empty(ResourceLocation key) { return new SkillRecipe(key); }

    public static SkillRecipe tryLoad(JsonObject data) { return empty().loadWithFallback(data); }

    public static SkillRecipe tryLoad(ResourceLocation key, JsonObject data)
    {
        JsonHelper.write(data, "key", key);
        return empty(key).loadWithFallback(data, empty(key));
    }

    @Nullable
    public static SkillRecipe byString(String stringKey)
    {
        if (stringKey.contains(":"))
        {
            ResourceLocation key = ResourceLocation.tryParse(stringKey);
            return key == null ? null : byKey(key);
        }
        else
        {
            for (String namespace : DEFAULT_NAMESPACES)
            {
                ResourceLocation key = ResourceLocation.tryBuild(namespace, stringKey);
                SkillRecipe recipe = key == null ? null : byKey(key);
                if (recipe != null) return recipe;
            }
            return null;
        }
    }

    @Nullable
    public static SkillRecipe byKey(ResourceLocation key) { return Peritia.skillRecipeHandler().byKey(key); }

    static String defaultTranslationKey(ResourceLocation key) { return String.format("skill_recipe.%s.%s", key.getNamespace(), key.getPath()); }

    @Override
    public ResourceLocation key() { return key; }

    public String translationKey() { return translationKey; }

    public SkillRequisites requisites() { return requisites; }

    public boolean unlockedForPlayer(ServerPlayer player) { return requisites().testForRecipe(ServerSkillData.of(player)); }

    public SkillRecipeInput input() { return input; }

    public SkillRecipeInput secondInput() { return input2; }

    public boolean hasTwoInputs() { return !(secondInput() instanceof SkillRecipeInput.AcceptAll); }

    public boolean check(ServerPlayer player) { return input().check(player) && secondInput().check(player); }

    public void pay(ServerPlayer player)
    {
        input().pay(player);
        secondInput().pay(player);
    }

    public boolean canCraft(ServerPlayer player) { return check(player) && unlockedForPlayer(player); }

    public void tryCraft(ServerPlayer player)
    {
        if (canCraft(player))
        {
            pay(player);
            player.getInventory().placeItemBackInInventory(assemble());
            player.inventoryMenu.sendAllDataToRemote();

            PeritiaAdvancementTriggers.SKILL_RECIPE.trigger(player, this);
        }
    }

    public void tryCraftStack(ServerPlayer player)
    {
        int count = result.getMaxStackSize();
        boolean canCraft = canCraft(player);
        boolean advancementTriggered = false;

        while (canCraft && count > 0) {

            pay(player);
            player.getInventory().placeItemBackInInventory(assemble());
            player.inventoryMenu.sendAllDataToRemote();

            if (!advancementTriggered) PeritiaAdvancementTriggers.SKILL_RECIPE.trigger(player, this);

            count--;
            canCraft = canCraft(player);
        }
    }

    public ItemStack assemble() { return result.copy(); }

    public short index() { return index; }

    public int trueIndex() { return isTrade() ? Short.MAX_VALUE + 1 + index() : index(); }

    public boolean isTrade() { return trade; }

    @Override
    public boolean hasText() { return hasText; }

    @Override
    public JsonArray conditions() { return conditions; }

    public boolean isValidRewardLikeFor(@NotNull Skill skill, int level) { return requisites().milestoneStatus(skill, level).isValid(); }

    @Override
    public SkillRecipe multiplyBy(int multiplier) { return this; }

    @Override
    public boolean shouldDisplayText(Skill skill, int level) { return hasText() && isValidRewardLikeFor(skill, level); }

    @Override
    public Component getText(Skill skill, int level)
    {
        SkillRequisites.MilestoneStatus milestoneStatus = requisites().milestoneStatus(skill, level);
        return Component.translatable(translationKey()).withStyle(ChatFormatting.GOLD).append(milestoneStatus.getText(ExtraTextColors.BROWN));
    }

    @Override
    public int priority() { return isTrade() ? 5 : 4; }

    @Override
    public boolean isInvalid()
    {
        return Loadable.super.isInvalid() ||
                requisites().requisites().isEmpty() ||
                input().isInvalid() ||
                secondInput().isInvalid() ||
                assemble().isEmpty() ||
                input() instanceof SkillRecipeInput.AcceptAll;
    }

    @Override
    public SkillRecipe fallback() { return empty(); }

    @Override
    public SkillRecipe load(JsonObject data)
    {
        this.key = JsonHelper.readResource(data, "key", key);
        this.translationKey = JsonHelper.readString(data, "translationKey", defaultTranslationKey(key));
        this.requisites = JsonHelper.readArraySerializable(data, "requires", SkillRequisites::tryLoad, requisites);
        this.input = JsonHelper.readJsonSerializable(data, "input", SkillRecipeInput::tryLoad, input);
        this.input2 = JsonHelper.readJsonSerializable(data, "input2", SkillRecipeInput::tryLoad, input2);
        this.result = JsonHelper.readItem(data, "result", result);
        this.index = JsonHelper.readShort(data, "index", index);
        this.trade = JsonHelper.readBoolean(data, "trade", trade);
        this.hasText = JsonHelper.readBoolean(data, "hasText", hasText);
        this.conditions = JsonHelper.readArray(data, "conditions", conditions);

        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();
        JsonHelper.write(data, "key", key);
        JsonHelper.write(data, "translationKey", translationKey);
        JsonHelper.write(data, "requires", requisites);
        JsonHelper.write(data, "input", input);
        JsonHelper.write(data, "input2", input2);
        JsonHelper.write(data, "result", result);
        JsonHelper.write(data, "index", index);
        JsonHelper.write(data, "trade", trade);
        JsonHelper.write(data, "hasText", hasText);
        JsonHelper.write(data, "conditions", conditions);

        return data;
    }

    @Override
    public int compareTo(@NotNull SkillRecipe other) { return trueIndex() - other.trueIndex(); }

    @Override
    public boolean equals(Object object) { return object instanceof SkillRecipe recipe && key().equals(recipe.key()); }

}
