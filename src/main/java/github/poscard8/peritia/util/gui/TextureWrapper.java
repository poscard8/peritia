package github.poscard8.peritia.util.gui;

import com.google.gson.JsonObject;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import github.poscard8.peritia.util.serialization.Proportions;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class TextureWrapper implements JsonSerializable<TextureWrapper>
{
    public static final Proportions DEFAULT_MENU_SIZE = new Proportions(176, 212);
    public static final Proportions DEFAULT_IMAGE_SIZE = new Proportions(256, 256);

    protected @Nullable ResourceLocation texture = null;
    protected Proportions imageSize = Proportions.empty();
    protected Proportions size = Proportions.empty();
    protected Proportions offset = Proportions.empty();

    public TextureWrapper() {}

    public TextureWrapper(@Nullable ResourceLocation texture, Proportions imageSize, Proportions size, Proportions offset)
    {
        this.texture = texture;
        this.imageSize = imageSize;
        this.size = size;
        this.offset = offset;
    }

    public static TextureWrapper empty() { return new TextureWrapper(); }

    public static TextureWrapper tryLoadForMenu(JsonObject data)
    {
        TextureWrapper wrapper = forMenu();
        return wrapper.loadWithFallback(data, wrapper.copy());
    }

    public static TextureWrapper tryLoadForMilestone(JsonObject data)
    {
        TextureWrapper wrapper = forMilestone();
        return wrapper.loadWithFallback(data, wrapper.copy());
    }

    public static TextureWrapper forMenu() { return new TextureWrapper(null, DEFAULT_IMAGE_SIZE, DEFAULT_MENU_SIZE, Proportions.empty()); }

    public static TextureWrapper forMilestone() { return new TextureWrapper(null, new Proportions(512, 256), Proportions.empty(), Proportions.empty()); }

    public TextureWrapper copy() { return new TextureWrapper(texture(), imageSize(), size(), offset()); }

    @Nullable
    public ResourceLocation texture() { return texture; }

    public boolean hasTexture() { return texture() != null; }

    public Proportions imageSize() { return imageSize; }

    public Proportions size() { return size; }

    public Proportions offset() { return offset; }

    @Override
    public TextureWrapper fallback() { return empty(); }

    @Override
    public TextureWrapper load(JsonObject data)
    {
        this.texture = JsonHelper.readResource(data, "texture", null);
        this.size = JsonHelper.readStringSerializable(data, "size", Proportions::tryLoad, size);
        this.offset = JsonHelper.readStringSerializable(data, "offset", Proportions::tryLoad, offset);
        this.imageSize = JsonHelper.readStringSerializable(data, "imageSize", Proportions::tryLoad, imageSize);

        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();
        if (texture != null) JsonHelper.write(data, "texture", texture);

        JsonHelper.write(data, "size", size);
        JsonHelper.write(data, "offset", offset);
        JsonHelper.write(data, "imageSize", imageSize);

        return data;
    }
}
