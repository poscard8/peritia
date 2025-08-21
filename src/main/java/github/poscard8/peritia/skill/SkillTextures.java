package github.poscard8.peritia.skill;

import com.google.gson.JsonObject;
import github.poscard8.peritia.util.gui.TextureWrapper;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import github.poscard8.peritia.util.serialization.Proportions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class SkillTextures implements JsonSerializable<SkillTextures>
{
    protected Item iconItem;
    protected ItemStack icon;
    protected ItemStack completeIcon;
    protected TextureWrapper menu;
    protected TextureWrapper overlay;
    protected TextureWrapper milestone;
    protected boolean alwaysRenderOverlay;

    public SkillTextures(Item iconItem, TextureWrapper menu, TextureWrapper overlay, TextureWrapper milestone, boolean alwaysRenderOverlay)
    {
        this.iconItem = iconItem;
        this.icon = iconItem.getDefaultInstance();

        ItemStack enchantedCopy = icon.copy();
        enchantedCopy.enchant(Enchantments.UNBREAKING, 1);

        this.completeIcon = enchantedCopy;
        this.menu = menu;
        this.overlay = overlay;
        this.milestone = milestone;
        this.alwaysRenderOverlay = alwaysRenderOverlay;
    }

    public static SkillTextures empty() { return new SkillTextures(Items.IRON_SWORD, TextureWrapper.forMenu(), TextureWrapper.forMenu(), TextureWrapper.forMilestone(), true); }

    public static SkillTextures tryLoad(JsonObject data) { return empty().loadWithFallback(data); }

    protected static ResourceLocation translate(ResourceLocation resource) { return new ResourceLocation(resource.getNamespace(), String.format("textures/%s.png", resource.getPath())); }

    public Item iconItem() { return iconItem; }

    public ItemStack icon() { return icon; }

    public ItemStack completeIcon() { return completeIcon; }

    public TextureWrapper menu() { return menu; }

    public ResourceLocation menuTexture() { return menu().texture() == null ? null : translate(Objects.requireNonNull(menu().texture())); }

    public Proportions menuSize() { return menu().size(); }

    public Proportions menuOffset() { return menu().offset(); }

    public TextureWrapper overlay() { return overlay; }

    public ResourceLocation overlayTexture() { return overlay().texture() == null ? null : translate(Objects.requireNonNull(overlay().texture())); }

    public Proportions overlaySize() { return overlay().size(); }

    public Proportions overlayOffset() { return overlay().offset(); }

    public TextureWrapper milestone() { return milestone; }

    public ResourceLocation milestoneTexture() { return milestone().texture() == null ? null : translate(Objects.requireNonNull(milestone().texture())); }

    public boolean alwaysRenderOverlay() { return alwaysRenderOverlay; }

    @Override
    public SkillTextures fallback() { return empty(); }

    @Override
    public SkillTextures load(JsonObject data)
    {
        this.iconItem = JsonHelper.readRegistrable(data, "icon", ForgeRegistries.ITEMS, iconItem);
        this.icon = iconItem.getDefaultInstance();

        ItemStack enchantedCopy = icon.copy();
        enchantedCopy.enchant(Enchantments.UNBREAKING, 1);
        this.completeIcon = enchantedCopy;

        this.menu = JsonHelper.readJsonSerializable(data, "menu", TextureWrapper::tryLoadForMenu, menu);
        this.overlay = JsonHelper.readJsonSerializable(data, "overlay", TextureWrapper::tryLoadForMenu, overlay);
        this.milestone = JsonHelper.readJsonSerializable(data, "milestone", TextureWrapper::tryLoadForMilestone, milestone);
        this.alwaysRenderOverlay = JsonHelper.readBoolean(data, "alwaysRenderOverlay", alwaysRenderOverlay);

        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();
        JsonHelper.write(data, "icon", iconItem, ForgeRegistries.ITEMS);
        JsonHelper.write(data, "menu", menu);
        JsonHelper.write(data, "overlay", overlay);
        JsonHelper.write(data, "milestone", milestone);
        JsonHelper.write(data, "alwaysRenderOverlay", alwaysRenderOverlay);

        return data;
    }

}
