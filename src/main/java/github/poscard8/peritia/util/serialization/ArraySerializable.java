package github.poscard8.peritia.util.serialization;

import com.google.gson.JsonArray;

public interface ArraySerializable<T extends ArraySerializable<T>> extends Serializable<JsonArray, T>
{
}
