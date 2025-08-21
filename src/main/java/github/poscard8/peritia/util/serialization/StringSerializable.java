package github.poscard8.peritia.util.serialization;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public interface StringSerializable<T extends StringSerializable<T>> extends Serializable<String, T>, StringRepresentable
{
    @Override
    @NotNull
    default String getSerializedName() { return save(); }

}
