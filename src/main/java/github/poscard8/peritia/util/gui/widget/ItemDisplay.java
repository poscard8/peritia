package github.poscard8.peritia.util.gui.widget;

import github.poscard8.peritia.network.ClientHandler;
import github.poscard8.peritia.util.gui.PeritiaUIElement;
import github.poscard8.peritia.util.text.TextGetter;
import github.poscard8.peritia.util.xpsource.BlockInputs;
import github.poscard8.peritia.util.xpsource.EntityInputs;
import github.poscard8.peritia.util.xpsource.ItemInputs;
import github.poscard8.peritia.util.xpsource.ResourceInputs;
import github.poscard8.peritia.xpsource.type.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class ItemDisplay extends AbstractWidget implements PeritiaUIElement
{
    public static final int SIZE = 16;
    public static final int TICK_PERIOD = 24;

    protected static final Map<EntityType<?>, Item> ENTITY_ITEM_MAP = new HashMap<>();

    public static final int FULL_RANGE = 1;
    public static final int FIRST_HALF = 3;
    public static final int SECOND_HALF = 4;
    public static final int FIRST_THIRD = 6;
    public static final int SECOND_THIRD = 7;
    public static final int LAST_THIRD = 8;

    protected final List<Item> items;
    protected final ItemStack[] array;
    protected final TextGetter textGetter;
    protected final int rangeId;
    protected final int beginIndex;
    protected final int endIndex;

    public ItemDisplay(List<Item> items, int rangeId, TextGetter textGetter, int x, int y)
    {
        super(x, y, SIZE, SIZE, Component.empty());
        this.items = items;
        this.rangeId = rangeId;
        this.textGetter = textGetter;

        int size = items.size();

        this.array = new ItemStack[size];
        for (int i = 0; i < size; i++) array[i] = items.get(i).getDefaultInstance();

        int[] indexes = new int[]{0, size, 0, (size + 1) / 2, size, 0, (size + 1) / 3, ((size + 1) * 2) / 3, size};
        this.beginIndex = indexes[rangeId - 1];
        this.endIndex = indexes[rangeId];
    }

    public static ItemDisplay block(BlockXpSource xpSource, int rangeId, int x, int y)
    {
        BlockInputs inputs = xpSource.inputs();
        TextGetter textGetter = TextGetter.block(inputs);

        return new ItemDisplay(getItemsBlock(xpSource), rangeId, textGetter, x, y);
    }

    public static ItemDisplay entity(EntityXpSource xpSource, int rangeId, int x, int y)
    {
        EntityInputs inputs = xpSource.inputs();
        TextGetter textGetter = TextGetter.entity(inputs);

        return new ItemDisplay(getItemsEntity(xpSource), rangeId, textGetter, x, y);
    }

    public static ItemDisplay item(ItemInputs inputs, int rangeId, int x, int y)
    {
        TextGetter textGetter = TextGetter.item(inputs);
        return new ItemDisplay(getItemsGeneral(inputs), rangeId, textGetter, x, y);
    }

    public static ItemDisplay recipeResult(RecipeXpSource xpSource, int x, int y)
    {
        TextGetter textGetter = TextGetter.recipe(xpSource);
        return new ItemDisplay(getItemsRecipe(xpSource), FULL_RANGE, textGetter, x, y);
    }

    public static ItemDisplay recipeIngredient(RecipeXpSource xpSource, int x, int y)
    {
        ItemInputs inputs = xpSource.ingredientInputs();
        TextGetter textGetter = TextGetter.ingredient(inputs);
        return new ItemDisplay(getItemsGeneral(inputs), FULL_RANGE, textGetter, x, y);
    }

    public static ItemDisplay chest(ChestXpSource xpSource, int x, int y)
    {
        TextGetter textGetter = TextGetter.chest(xpSource);
        return new ItemDisplay(List.of(Items.CHEST), FULL_RANGE, textGetter, x, y);
    }

    public static ItemDisplay anvilLeft(AnvilXpSource xpSource, int x, int y)
    {
        TextGetter textGetter = TextGetter.anvilLeft(xpSource);
        return new ItemDisplay(getItemsGeneral(xpSource.leftInputs()), FULL_RANGE, textGetter, x, y);
    }

    public static ItemDisplay anvilRight(AnvilXpSource xpSource, int x, int y)
    {
        TextGetter textGetter = TextGetter.anvilRight(xpSource);
        return new ItemDisplay(getItemsGeneral(xpSource.rightInputs()), FULL_RANGE, textGetter, x, y);
    }

    public static ItemDisplay profession(TradeXpSource xpSource, int x, int y)
    {
        TextGetter textGetter = TextGetter.profession(xpSource);
        return new ItemDisplay(getItemsPoi(xpSource), FULL_RANGE, textGetter, x, y);
    }

    @SuppressWarnings("deprecation")
    public static List<Item> getItemsBlock(BlockXpSource xpSource)
    {
        List<Item> items = new ArrayList<>();
        BlockInputs inputs = xpSource.inputs();

        for (Block block : ForgeRegistries.BLOCKS.getValues())
        {
            if (inputs.testForDisplay(block))
            {
                Item item = Item.byBlock(block);
                if (item != Items.AIR && !items.contains(item)) items.add(item);
            }
        }
        if (items.isEmpty()) items.add(Items.GRASS_BLOCK);
        return items;
    }

    public static List<Item> getItemsEntity(EntityXpSource xpSource)
    {
        List<Item> items = new ArrayList<>();
        EntityInputs inputs = xpSource.inputs();

        for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES.getValues())
        {
            if (inputs.test(entityType))
            {
                Item item = ENTITY_ITEM_MAP.get(entityType);
                if (item != null) items.add(item);
            }
        }
        if (items.isEmpty()) items.add(Items.GRASS_BLOCK);
        return items;
    }

    public static List<Item> getItemsGeneral(@Nullable ItemInputs inputs)
    {
        if (inputs == null) return List.of();
        List<Item> items = new ArrayList<>();

        for (Item item : ForgeRegistries.ITEMS.getValues())
        {
            if (inputs.test(item.getDefaultInstance()))
            {
                if (item != Items.AIR && !items.contains(item)) items.add(item);
            }
        }
        if (items.isEmpty()) items.add(Items.GRASS_BLOCK);
        return items;
    }

    public static List<Item> getItemsRecipe(RecipeXpSource xpSource)
    {
        List<Item> items = new ArrayList<>();
        ResourceInputs inputs = xpSource.keyInputs();
        RecipeManager recipeManager = ClientHandler.getRecipeManager();
        ClientPacketListener listener = Minecraft.getInstance().getConnection();

        for (ResourceLocation key : recipeManager.getRecipeIds().collect(Collectors.toSet()))
        {
            if (inputs.test(key) && listener != null)
            {
                Optional<? extends Recipe<?>> optional = recipeManager.byKey(key);
                optional.ifPresent(recipe ->
                {
                    ItemStack result = recipe.getResultItem(listener.registryAccess());
                    if (!result.isEmpty())
                    {
                        Item item = result.getItem();
                        if (item != Items.AIR && !items.contains(item)) items.add(item);
                    }
                });
            }
        }
        if (items.isEmpty()) items.add(Items.GRASS_BLOCK);
        return items;
    }

    @SuppressWarnings("deprecation")
    public static List<Item> getItemsPoi(TradeXpSource xpSource)
    {
        List<VillagerProfession> professions = new ArrayList<>();
        ResourceInputs inputs = xpSource.professionInputs();
        IForgeRegistry<VillagerProfession> registry = ForgeRegistries.VILLAGER_PROFESSIONS;

        if (inputs == null) return List.of();

        for (VillagerProfession profession : registry.getValues())
        {
            ResourceLocation key = registry.getKey(profession);
            if (key != null && inputs.test(key)) professions.add(profession);
        }

        List<Item> items = new ArrayList<>();

        for (VillagerProfession profession : professions)
        {
            for (Block block : ForgeRegistries.BLOCKS.getValues())
            {
                Optional<Holder<PoiType>> optional = PoiTypes.forState(block.defaultBlockState());
                optional.ifPresent(holder ->
                {
                    if (profession.heldJobSite().test(holder))
                    {
                        Item item = Item.byBlock(block);
                        if (item != Items.AIR && !items.contains(item)) items.add(item);
                    }
                });
            }
        }
        if (items.isEmpty()) items.add(Items.GRASS_BLOCK);
        return items;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta)
    {
        guiGraphics.renderItem(currentItem(), getX(), getY());
        if (isHovered()) guiGraphics.renderTooltip(font(), getTexts(), Optional.empty(), mouseX, mouseY);
    }

    public List<Component> getTexts() { return textGetter.apply(clientHandler()); }

    public ItemStack currentItem()
    {
        int cycles = screenTicks() / TICK_PERIOD;
        int delta = endIndex - beginIndex;
        if (delta == 0) return array[beginIndex];

        return array[(cycles % delta) + beginIndex];
    }

    public void changePos(int deltaX, int deltaY)
    {
        setX(getX() + deltaX);
        setY(getY() + deltaY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}

    static
    {
        for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES.getValues())
        {
            @Nullable SpawnEggItem spawnEgg = ForgeSpawnEggItem.fromEntityType(entityType);
            if (spawnEgg != null) ENTITY_ITEM_MAP.put(entityType, spawnEgg);
        }

        ENTITY_ITEM_MAP.put(EntityType.PLAYER, Items.PLAYER_HEAD);
        ENTITY_ITEM_MAP.put(EntityType.ZOMBIE, Items.ZOMBIE_HEAD);
        ENTITY_ITEM_MAP.put(EntityType.SKELETON, Items.SKELETON_SKULL);
        ENTITY_ITEM_MAP.put(EntityType.WITHER_SKELETON, Items.WITHER_SKELETON_SKULL);
        ENTITY_ITEM_MAP.put(EntityType.CREEPER, Items.CREEPER_HEAD);
        ENTITY_ITEM_MAP.put(EntityType.PIGLIN, Items.PIGLIN_HEAD);
        ENTITY_ITEM_MAP.put(EntityType.ENDER_DRAGON, Items.DRAGON_HEAD);
        ENTITY_ITEM_MAP.put(EntityType.COD, Items.COD);
        ENTITY_ITEM_MAP.put(EntityType.SALMON, Items.SALMON);
        ENTITY_ITEM_MAP.put(EntityType.TROPICAL_FISH, Items.TROPICAL_FISH);
        ENTITY_ITEM_MAP.put(EntityType.PUFFERFISH, Items.PUFFERFISH);
    }

}
