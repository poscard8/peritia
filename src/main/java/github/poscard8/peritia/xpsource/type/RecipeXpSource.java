package github.poscard8.peritia.xpsource.type;

import com.google.gson.JsonObject;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.StringSerializable;
import github.poscard8.peritia.util.text.Hints;
import github.poscard8.peritia.util.xpsource.ItemInputs;
import github.poscard8.peritia.util.xpsource.ResourceInputs;
import github.poscard8.peritia.xpsource.GuiXpSource;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.XpSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class RecipeXpSource extends GuiXpSource
{
    protected ResourceInputs keyInputs = ResourceInputs.empty();
    protected ItemInputs ingredientInputs = ItemInputs.acceptAll();
    protected Category category = Category.CRAFT;
    protected Hints hints = Hints.empty();
    protected boolean useHints = false;

    public RecipeXpSource(ResourceLocation key) { super(key); }

    public static RecipeXpSource empty() { return new RecipeXpSource(EMPTY_KEY); }

    public static RecipeXpSource tryLoad(JsonObject data)
    {
        @Nullable XpSource xpSource = empty().loadWithFallback(data);
        return xpSource != null ? (RecipeXpSource) xpSource : null;
    }

    @Override
    public XpSourceType<?> type() { return PeritiaXpSourceTypes.RECIPE.get(); }

    public ResourceInputs keyInputs() { return keyInputs; }

    public ItemInputs ingredientInputs() { return ingredientInputs; }

    public Category category() { return category; }

    public Hints hints() { return hints; }

    public boolean shouldUseHints() { return useHints; }

    @Override
    public boolean isInvalid() { return super.isInvalid() || keyInputs().isEmpty(); }

    public boolean validateRecipe(Recipe<?> recipe, List<ItemStack> items)
    {
        ResourceLocation recipeKey = recipe.getId();

        boolean recipeValidated = keyInputs().test(recipeKey);
        boolean itemsValidated = false;

        for (ItemStack stack : items)
        {
            if (Objects.requireNonNull(ingredientInputs()).test(stack))
            {
                itemsValidated = true;
                break;
            }
        }
        return recipeValidated && itemsValidated;
    }

    public void handleRecipe(ServerPlayer player, Recipe<?> recipe, List<ItemStack> items) { handleRecipe(player, recipe, items, 1); }

    public void handleRecipe(ServerPlayer player, Recipe<?> recipe, List<ItemStack> items, int multiplier)
    {
        if (validateRecipe(recipe, items)) addWaitingXp(player, multiplier);
    }

    @Override
    public void loadAdditional(JsonObject data)
    {
        this.keyInputs = JsonHelper.readElementSerializable(data, "keys", ResourceInputs::tryLoad, keyInputs);
        this.ingredientInputs = JsonHelper.readElementSerializable(data, "ingredients", ItemInputs::tryLoad, ingredientInputs);
        this.category = JsonHelper.readStringSerializable(data, "category", Category::tryLoad, category);
        this.hints = JsonHelper.readArraySerializable(data, "hints", Hints::tryLoad, hints);
        this.useHints = JsonHelper.readBoolean(data, "useHints", data.has("hints"));
    }

    @Override
    public void saveAdditional(JsonObject data)
    {
        JsonHelper.write(data, "keys", keyInputs);
        JsonHelper.write(data, "ingredients", ingredientInputs);
        JsonHelper.write(data, "category", category);
        JsonHelper.write(data, "hints", hints);
        JsonHelper.write(data, "useHints", useHints);
    }

    @SuppressWarnings("unused")
    public record Category(String name) implements StringSerializable<Category>
    {
        static final Map<String, Category> MAP = new HashMap<>();

        public static final Category CRAFT = new Category("craft");
        public static final Category SMELT = new Category("smelt");
        public static final Category COOK = new Category("cook");

        public Category(String name)
        {
            this.name = name;
            MAP.put(name, this);
        }

        public static Category empty() { return CRAFT; }

        public static Category tryLoad(String data) { return empty().loadWithFallback(data); }

        @Override
        public Category fallback() { return empty(); }

        @Override
        public Category load(String data) { return MAP.getOrDefault(data, fallback()); }

        @Override
        public String save() { return name; }

    }

}
