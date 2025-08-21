package github.poscard8.peritia.skill;

import github.poscard8.peritia.util.serialization.StringSerializable;

import java.util.Arrays;

public enum Priority implements Comparable<Priority>, StringSerializable<Priority>
{
    HIGH("high", 2),
    NORMAL("normal", 1),
    LOW("low", 0);

    private final String name;
    private final int value;

    Priority(String name, int value)
    {
        this.name = name;
        this.value = value;
    }

    public static Priority empty() { return Priority.NORMAL; }

    public static Priority tryLoad(String data) { return empty().loadWithFallback(data); }

    public String getName() { return name; }

    public int value() { return value; }

    public boolean shouldReplace(Priority other) { return value() > other.value(); }

    @Override
    public Priority fallback() { return empty(); }

    @Override
    public Priority load(String data)
    {
        return Arrays.stream(values()).filter(priority -> priority.getName().equals(data)).findFirst().orElse(fallback());
    }

    @Override
    public String save() { return getName(); }

}
