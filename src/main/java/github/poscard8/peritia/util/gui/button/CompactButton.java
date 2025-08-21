package github.poscard8.peritia.util.gui.button;

import com.mojang.blaze3d.vertex.PoseStack;
import github.poscard8.peritia.client.screen.PeritiaMainScreen;
import github.poscard8.peritia.network.ClientHandler;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.SkillTextures;
import github.poscard8.peritia.util.gui.PeritiaUIElement;
import github.poscard8.peritia.util.text.PeritiaTexts;
import github.poscard8.peritia.util.text.TextGetter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Button that can display multiple texts when hovered.
 */
@OnlyIn(Dist.CLIENT)
public class CompactButton extends ImageButton implements PeritiaUIElement
{
    public static final int DEFAULT_SIZE = 18;

    public @NotNull TextGetter textGetter = TextGetter.empty();
    public @Nullable Skill skill;
    public @NotNull ItemStack stack = ItemStack.EMPTY;
    public @Nullable Function<CompactButton, Component> notifyTextFunction;
    public @Nullable Function<CompactButton, Component> countTextFunction;
    public boolean requireAuthorization = true;

    public CompactButton(int x, int y) { this(x, y, button -> {}); }

    public CompactButton(int x, int y, OnPress onPress) { this(x, y, 176, 0, PeritiaMainScreen.textureLocation(), onPress); }

    public CompactButton(int x, int y, int xTexStart, int yTexStart, ResourceLocation location, OnPress onPress) { this(x, y, DEFAULT_SIZE, DEFAULT_SIZE, xTexStart, yTexStart, location, onPress); }

    public CompactButton(int x, int y, int width, int height, int xTexStart, int yTexStart, ResourceLocation location, OnPress onPress) { this(x, y, width, height, xTexStart, yTexStart, location, 256, 256, onPress); }

    public CompactButton(int x, int y, int width, int height, int xTexStart, int yTexStart, ResourceLocation location, int textureWidth, int textureHeight, OnPress onPress)
    {
        super(x, y, width, height, xTexStart, yTexStart, height, location, textureWidth, textureHeight, onPress);
    }

    public static void renderSkillIcon(GuiGraphics guiGraphics, int x, int y, Skill skill)
    {
        if (skill == null) return;

        boolean isMaxLevel = ClientHandler.getSkillData().getSkill(skill).isMaxLevel();
        SkillTextures textures = skill.textures();
        ItemStack icon = isMaxLevel ? textures.completeIcon() : textures.icon();

        guiGraphics.renderItem(icon, x, y);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta)
    {
        if (!shouldRender(mouseX, mouseY)) return;

        handleShaderColor();
        super.render(guiGraphics, mouseX, mouseY, delta);

        PoseStack poseStack = guiGraphics.pose();

        if (skill() != null && renderSkillIcon())
        {
            renderSkillIcon(guiGraphics, getX() + 1, getY() + 1, skill());
        }
        else
        {
            if (!itemStack().isEmpty()) guiGraphics.renderItem(itemStack(), getX() + 1, getY() + 1);
        }

        if (notifyTextFunction != null)
        {
            Component text = notifyText();
            if (text != null)
            {
                TextColor color = text.getStyle().getColor();
                int colorValue = color == null ? 0xFFFFFF : color.getValue();

                poseStack.pushPose();
                poseStack.translate(0.0D, 0.0D, 320.0D);

                guiGraphics.drawString(font(), text, getX() + width - font().width(text), getY(), colorValue);
                poseStack.popPose();
            }
        }

        if (countTextFunction != null)
        {
            Component text = countText();
            if (text != null)
            {
                TextColor color = text.getStyle().getColor();
                int colorValue = color == null ? 0xFFFFFF : color.getValue();

                float ratio = 0.75F;
                float inverse = 1 / ratio;

                poseStack.pushPose();
                poseStack.scale(ratio, ratio, ratio);
                poseStack.translate(0.0D, 0.0D, 360.0D);

                int x = Math.round((getX() + width - (font().width(text)) * ratio) * inverse);
                int y = Math.round((getY() + height - 6) * inverse);
                guiGraphics.drawString(font(), text, x, y, colorValue);

                poseStack.scale(1, 1, 1);
                poseStack.popPose();
            }
        }
    }

    public void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
        if (isHovered() && isInside(mouseX, mouseY)) guiGraphics.renderTooltip(font(), getTexts(), Optional.empty(), mouseX, mouseY);
    }

    public List<Component> getTexts() { return textGetter.apply(clientHandler()); }

    public CompactButton setText(@NotNull Component text) { return setTextGetter(TextGetter.single(text)); }

    public CompactButton setTextGetter(@NotNull TextGetter textGetter)
    {
        this.textGetter = textGetter;
        return this;
    }

    @Nullable
    public Skill skill() { return skill; }

    public CompactButton setSkill(@Nullable Skill skill)
    {
        this.skill = skill;
        return this;
    }

    @NotNull
    public ItemStack itemStack() { return stack; }

    public CompactButton setItem(@NotNull Item item) { return setItemStack(item.getDefaultInstance()); }

    public CompactButton setItemStack(@NotNull ItemStack stack)
    {
        this.stack = stack;
        return this;
    }

    @Nullable
    public Component notifyText() { return notifyTextFunction == null ? null : notifyTextFunction.apply(this); }

    public CompactButton setNotifyText(@Nullable Function<CompactButton, Component> notifyTextFunction)
    {
        this.notifyTextFunction = notifyTextFunction;
        return this;
    }

    @Nullable
    public Component countText() { return countTextFunction == null ? null : countTextFunction.apply(this); }

    public CompactButton setCountText(Number count) { return setCountText(button -> PeritiaTexts.makeText(count)); }

    public CompactButton setCountText(@Nullable Function<CompactButton, Component> countTextFunction)
    {
        this.countTextFunction = countTextFunction;
        return this;
    }

    public boolean requiresAuthorization() { return requireAuthorization; }

    public CompactButton setRequireAuthorization(boolean requireAuthorization)
    {
        this.requireAuthorization = requireAuthorization;
        return this;
    }

    public boolean shouldRender(int mouseX, int mouseY) { return true; }

    public boolean renderSkillIcon() { return true; }

    /**
     * @return Is inside a given area. Since there is no area defined yet, returns true.
     */
    public boolean isInside(double mouseX, double mouseY) { return true; }

    public boolean canClick() { return !requiresAuthorization() || isScreenAuthorized(); }

    @Override
    protected boolean clicked(double mouseX, double mouseY)
    {
        return isInside(mouseX, mouseY) && canClick() && super.clicked(mouseX, mouseY);
    }

    @Override
    public boolean isFocused() { return super.isFocused() && isHovered(); }

    public void move(int x, int y)
    {
        setX(getX() + x);
        setY(getY() + y);
    }

}
