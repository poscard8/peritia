package github.poscard8.peritia.util.serialization;

import java.util.Objects;

public interface TriPredicate<T, I, V>
{
    boolean test(T t, I i, V v);

    default TriPredicate<T, I, V> and(TriPredicate<? super T, ? super I, ? super V> other)
    {
        Objects.requireNonNull(other);
        return (T t, I i, V v) -> test(t, i, v) && other.test(t, i, v);
    }


    default TriPredicate<T, I, V> or(TriPredicate<? super T, ? super I, ? super V> other)
    {
        Objects.requireNonNull(other);
        return (T t, I i, V v) -> test(t, i, v) || other.test(t, i, v);
    }

    default TriPredicate<T, I, V> negate() { return (T t, I i, V v) -> !test(t, i, v); }

}
