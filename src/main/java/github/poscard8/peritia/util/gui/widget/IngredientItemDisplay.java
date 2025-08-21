package github.poscard8.peritia.util.gui.widget;

import com.mojang.brigadier.StringReader;
import github.poscard8.peritia.skill.recipe.SkillRecipeInput;
import github.poscard8.peritia.util.text.TextGetter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class IngredientItemDisplay extends ItemDisplay
{
    @Nullable
    public final ItemStack result;
    public final int count;
    public boolean unlocked = true;

    public IngredientItemDisplay(SkillRecipeInput input, int x, int y) { this(input.getItems(), null, TextGetter.empty(), input.count(), x, y); }

    public IngredientItemDisplay(ItemStack result, TextGetter textGetter, int count, int x, int y) { this(List.of(result.getItem()), result, textGetter, count, x, y); }

    public IngredientItemDisplay(List<Item> items, @Nullable ItemStack result, TextGetter textGetter, int count, int x, int y)
    {
        super(items, FULL_RANGE, textGetter, x, y);
        this.result = result;
        this.count = count;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta)
    {
        super.renderWidget(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.renderItemDecorations(font(), currentItem(), getX(), getY());
    }

    @Override
    public List<Component> getTexts()
    {
        TooltipFlag flag = minecraft().options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
        List<Component> texts = currentItem().getTooltipLines(player(), flag);
        texts.addAll(textGetter.apply(clientHandler()));

        if (unlocked) return texts;

        List<Component> lockedTexts = new ArrayList<>();
        for (Component text : texts) lockedTexts.add(makeDarkText(text));
        return lockedTexts;
    }

    @Override
    public ItemStack currentItem()
    {
        if (result != null) return result;

        ItemStack stack = super.currentItem();
        stack.setCount(count);
        return stack;
    }

    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }

    public Component makeDarkText(Component text)
    {
        String string = text.getString();
        boolean italic = string.contains("§o");
        Style style = text.getStyle().withItalic(italic).withColor(ChatFormatting.DARK_GRAY);

        StringReader reader = new StringReader(text.getString());
        StringBuilder builder = new StringBuilder();

        while (reader.canRead())
        {
            char c = reader.read();
            if (c == '§')
            {
                if (reader.canRead()) reader.skip();
            }
            else builder.append(c);
        }
        return Component.literal(builder.toString()).withStyle(style);
    }

}
