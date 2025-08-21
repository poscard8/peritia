package github.poscard8.peritia.util.xpsource;

import github.poscard8.peritia.util.gui.widget.XpSourceWidget;
import github.poscard8.peritia.util.serialization.StringSerializable;
import github.poscard8.peritia.xpsource.XpSource;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.function.BiFunction;

public enum XpSourceSortFunction implements StringSerializable<XpSourceSortFunction>, Comparator<XpSourceWidget<?>>
{
    DEFAULT_ORDER("defaultOrder", XpSource::compareTo),
    XP_ASCENDING("xpAscending", (first, second) ->
    {
        return first.xp() == second.xp() ? first.compareTo(second) : first.xp() - second.xp();
    }),
    XP_DESCENDING("xpDescending", (first, second) ->
    {
        return first.xp() == second.xp() ? first.compareTo(second) : second.xp() - first.xp();
    });

    private final String name;
    private final BiFunction<XpSource, XpSource, Integer> function;

    XpSourceSortFunction(String name, BiFunction<XpSource, XpSource, Integer> function)
    {
        this.name = name;
        this.function = function;
    }

    public static XpSourceSortFunction empty() { return DEFAULT_ORDER; }

    public static XpSourceSortFunction tryLoad(String data) { return empty().loadWithFallback(data); }

    public String getName() { return name; }

    public Component getText() { return Component.translatable("generic.peritia.xp_source_sort." + getName()); }

    @Override
    public int compare(XpSourceWidget<?> first, XpSourceWidget<?> second) { return function.apply(first.xpSource(), second.xpSource()); }

    @Override
    public XpSourceSortFunction fallback() { return DEFAULT_ORDER; }

    @Override
    public XpSourceSortFunction load(String data)
    {
        for (XpSourceSortFunction sortFunction : values())
        {
            if (data.equals(sortFunction.getName())) return sortFunction;
        }
        return this;
    }

    @Override
    public String save() { return getName(); }



}
