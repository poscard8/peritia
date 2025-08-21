package github.poscard8.peritia.util.xpsource;

import com.google.gson.JsonPrimitive;

import java.util.function.Predicate;

public interface Input<T> extends Predicate<T>
{
    String ALL_NAME = "all";
    JsonPrimitive ALL_NAME_PRIMITIVE = new JsonPrimitive(ALL_NAME);

    boolean isValid();

    default boolean isInvalid() { return !isValid(); }


}
