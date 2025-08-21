package github.poscard8.peritia.util.skill;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.serialization.ArraySerializable;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class LevelUpContext implements ArraySerializable<LevelUpContext>
{
    protected final List<Component> components = new ArrayList<>();

    public LevelUpContext() {}

    public static LevelUpContext empty() { return new LevelUpContext(); }

    public static LevelUpContext tryLoad(JsonArray data) { return empty().loadWithFallback(data); }

    public List<Component> components() { return components; }

    public Component getComponent(int index) { return components().get(index); }

    public int size() { return components().size(); }

    public void add(Skill skill, int oldLevel, int newLevel) { components().add(new Component(skill, oldLevel, newLevel)); }

    public void add(Component component) { components().add(component); }

    public void forEachComponent(Consumer<Component> consumer) { components().forEach(consumer); }

    public void optimizeComponents()
    {
        Component[] newComponents = new Component[components.size()];
        Set<Integer> indicesToRemove = new HashSet<>();

        for (int i = 0; i < components.size(); i++)
        {
            Component current = components.get(i);

            for (int j = i + 1; j < components.size(); j++)
            {
                Component next = components.get(j);
                @Nullable Component combined = Component.tryAppend(current, next);
                if (combined == null) continue;

                indicesToRemove.add(j);
                newComponents[i] = combined;
            }
        }

        for (int i = 0; i < components.size(); i++)
        {
            @Nullable Component component = newComponents[i];
            if (component != null) components.set(i, component);
        }

        List<Integer> sorted = indicesToRemove.stream().sorted(Comparator.reverseOrder()).toList();
        for (int i : sorted) components.remove(i);
    }

    public LevelUpContext copy()
    {
        LevelUpContext copy = new LevelUpContext();
        components().forEach(copy::add);

        return copy;
    }

    public LevelUpContext append(LevelUpContext other)
    {
        other.components().forEach(this::add);
        optimizeComponents();
        return this;
    }

    @Override
    public LevelUpContext fallback() { return empty(); }

    @Override
    public LevelUpContext load(JsonArray data)
    {
        for (JsonElement element : data)
        {
            if (element.isJsonArray())
            {
                Component component = Component.tryLoad(element.getAsJsonArray());
                if (!component.isEmpty()) components.add(component);
            }
        }
        return this;
    }

    @Override
    public JsonArray save()
    {
        JsonArray data = new JsonArray();
        for (Component component : components) data.add(component.save());
        return data;
    }


    public record Component(Skill skill, int oldLevel, int newLevel) implements ArraySerializable<Component>
    {
        public static Component empty() { return new Component(Skill.empty(), Skill.DEFAULT_MIN_LEVEL, Skill.DEFAULT_MIN_LEVEL); }

        public static Component tryLoad(JsonArray data) { return empty().loadWithFallback(data); }

        static boolean canAppend(Component first, Component second)
        {
            return first.skill().equals(second.skill()) && first.newLevel() == second.oldLevel();
        }

        @Nullable
        static Component tryAppend(Component first, Component second)
        {
            if (canAppend(first, second))
            {
                return new Component(second.skill(), first.oldLevel(), second.newLevel());
            }
            return null;
        }

        public boolean isEmpty() { return skill().isEmpty(); }

        @Override
        public Component fallback() { return empty(); }

        @Override
        public Component load(JsonArray data)
        {
            String string = data.get(0).getAsString();
            Skill skill = Skill.byString(string);
            if (skill == null) throw new RuntimeException("Invalid string for skill: " + string);

            int oldLevel = data.get(1).getAsInt();
            int newLevel = data.get(2).getAsInt();

            return new Component(skill, oldLevel, newLevel);
        }

        @Override
        public JsonArray save()
        {
            JsonArray data = new JsonArray();
            data.add(skill().stringKey());
            data.add(oldLevel());
            data.add(newLevel());

            return data;
        }
    }

}
