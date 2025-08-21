package github.poscard8.peritia.util.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import github.poscard8.peritia.util.PeritiaHelper;
import github.poscard8.peritia.util.serialization.ElementSerializable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class Hint implements ElementSerializable<Hint>
{
    protected Component text = Component.empty();
    protected @Nullable String translationKey = null;

    protected Hint() {}

    public static Hint empty() { return new Hint(); }

    public static Hint tryLoad(JsonElement data) { return empty().loadWithFallback(data); }

    public Component text() { return text; }

    public void format(ChatFormatting... formatting) { this.text = text().copy().withStyle(formatting); }

    public boolean isEmpty() { return text().equals(Component.empty()); }

    @Nullable
    public String translationKey() { return translationKey; }

    @Override
    public Hint fallback() { return empty(); }

    @Override
    public Hint load(JsonElement data)
    {
        if (data.isJsonObject())
        {
            this.text = PeritiaHelper.deserializeText(data.getAsJsonObject());
        }
        else
        {
            String translationKey = data.getAsString();

            this.translationKey = translationKey;
            this.text = Component.translatable(translationKey);
            format(ChatFormatting.GRAY);
        }
        return this;
    }

    @Override
    public JsonElement save()
    {
        String translationKey = translationKey();
        return translationKey == null ? PeritiaHelper.serializeText(text) : new JsonPrimitive(translationKey);
    }

}
