package github.poscard8.peritia.util.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import github.poscard8.peritia.client.screen.SkillRecipeScreen;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.serverbound.CraftPacket;
import github.poscard8.peritia.skill.recipe.SkillRecipe;
import github.poscard8.peritia.util.gui.PeritiaUIElement;
import github.poscard8.peritia.util.text.PeritiaTexts;
import github.poscard8.peritia.util.text.TextGetter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class SkillRecipeWidget extends AbstractWidget implements PeritiaUIElement
{
    public static final int LEFT_PART_WIDTH = 56;
    public static final int RIGHT_PART_WIDTH = 24;
    public static final int WIDTH = LEFT_PART_WIDTH + RIGHT_PART_WIDTH;
    public static final int HEIGHT = 24;

    public final SkillRecipe recipe;
    public int row;
    public int column;

    public List<IngredientItemDisplay> displays;

    protected int mouseX;
    protected int mouseY;

    public SkillRecipeWidget(SkillRecipe recipe, int x, int y)
    {
        super(x, y, WIDTH, HEIGHT, Component.empty());
        this.recipe = recipe;
        this.row = 0;
        this.column = 0;
        this.displays = new ArrayList<>();

        if (recipe.hasTwoInputs())
        {
            displays.add(new IngredientItemDisplay(recipe.input(), getX() + 4, getY() + 4));
            displays.add(new IngredientItemDisplay(recipe.secondInput(), getX() + 24, getY() + 4));
        }
        else
        {
            displays.add(new IngredientItemDisplay(recipe.input(), getX() + 24, getY() + 4));
        }
        ItemStack result = recipe.assemble();
        displays.add(new IngredientItemDisplay(result.copy(), TextGetter.craftGuide(isUnlocked()), result.getCount(), getX() + 60, getY() + 4));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta)
    {
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        renderBg(guiGraphics);

        if (isArrowPartHovered() && !isUnlocked())
        {
            guiGraphics.renderTooltip(font(), PeritiaTexts.$requisitesForRecipe(recipe.requisites(), skillData()), Optional.empty(), mouseX, mouseY);
        }
    }

    protected void renderBg(GuiGraphics guiGraphics)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        handleShaderColor();
        guiGraphics.blit(SkillRecipeScreen.textureLocation(), getX(), getY(), 176, getTextureY(), WIDTH, HEIGHT);
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY)
    {
        if (isRightPartHovered()) PeritiaNetworkHandler.sendToServer(new CraftPacket(recipe, Screen.hasShiftDown()));
        return super.clicked(mouseX, mouseY);
    }

    @Override
    public int getX() { return super.getX() + WIDTH * column; }

    @Override
    public int getY() { return super.getY() + HEIGHT * row; }

    public void setPos(int row, int column)
    {
        int deltaX = (column - this.column) * WIDTH;
        int deltaY = (row - this.row) * HEIGHT;

        this.row = row;
        this.column = column;

        displays.forEach(display ->
        {
            display.setX(display.getX() + deltaX);
            display.setY(display.getY() + deltaY);
        });
    }

    public boolean isLeftPartHovered()
    {
        boolean xCheck = mouseX >= getX() && mouseX < getX() + LEFT_PART_WIDTH;
        boolean yCheck = mouseY >= getY() && mouseY < getY() + HEIGHT;
        return xCheck && yCheck;
    }

    public boolean isRightPartHovered()
    {
        boolean xCheck = mouseX >= getX() + LEFT_PART_WIDTH && mouseX < getX() + WIDTH;
        boolean yCheck = mouseY >= getY() && mouseY < getY() + HEIGHT;
        return xCheck && yCheck;
    }

    public boolean isArrowPartHovered()
    {
        boolean xCheck = mouseX >= getX() + 46 && mouseX < getX() + LEFT_PART_WIDTH;
        boolean yCheck = mouseY >= getY() && mouseY < getY() + HEIGHT;
        return xCheck && yCheck;
    }

    public int getTextureY()
    {
        if (isUnlocked())
        {
            if (isLeftPartHovered()) return 24;
            if (isRightPartHovered()) return 48;
            return 0;
        }
        return 72;
    }

    public boolean isUnlocked() { return recipe.requisites().testForRecipe(skillData()); }

    public boolean isRelatedTo(String searchString)
    {
        TooltipFlag flag = minecraft().options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
        ItemStack result = recipe.assemble();

        for (Component text : result.getTooltipLines(player(), flag))
        {
            if (text.getString().toLowerCase().contains(searchString.toLowerCase())) return true;
        }
        return false;
    }

    public void addSelf(SkillRecipeScreen screen)
    {
        screen.$addWidget(this);
        displays.forEach(display -> display.setUnlocked(isUnlocked()));
        displays.forEach(screen::$addWidget);
    }

    public void removeSelf(SkillRecipeScreen screen)
    {
        screen.$removeWidget(this);
        displays.forEach(screen::$removeWidget);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}


}
