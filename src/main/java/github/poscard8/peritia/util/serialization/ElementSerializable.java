package github.poscard8.peritia.util.serialization;

import com.google.gson.JsonElement;

public interface ElementSerializable<T extends ElementSerializable<T>> extends Serializable<JsonElement, T> {}
