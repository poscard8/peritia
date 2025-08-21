package github.poscard8.peritia.util.xpsource;

import github.poscard8.peritia.util.serialization.StringSerializable;

public class PropertyPredicate implements StringSerializable<PropertyPredicate>
{
    public static final String EMPTY_PROPERTY_KEY = "none";

    protected String fullName = EMPTY_PROPERTY_KEY + "=";
    protected String propertyName = EMPTY_PROPERTY_KEY;
    protected boolean invert = false;

    public PropertyPredicate() {}

    public static PropertyPredicate empty() { return new PropertyPredicate(); }

    public static PropertyPredicate tryLoad(String data) { return empty().loadWithFallback(data); }

    public String fullName() { return fullName; }

    public String propertyName() { return propertyName; }

    public boolean inverted() { return invert; }

    public boolean isEmpty() { return propertyName().equals(EMPTY_PROPERTY_KEY); }

    @Override
    public PropertyPredicate fallback() { return empty(); }

    @Override
    public PropertyPredicate load(String data)
    {
        String[] split = data.split("=");
        if (split.length != 2) throw new RuntimeException(String.format("Property predicate %s should only contain one '=' character", data));

        this.invert = data.startsWith("!");
        this.fullName = invert ? data.substring(1) : data;
        this.propertyName = fullName.split("=")[0];

        return this;
    }

    @Override
    public String save() { return inverted() ? "!" + fullName : fullName; }

}
