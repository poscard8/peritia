package github.poscard8.peritia.util.serialization;

import com.google.gson.JsonObject;

public interface JsonSerializable<T extends JsonSerializable<T>> extends Serializable<JsonObject, T>
{
}
