package github.poscard8.peritia.util.skill;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.SkillInstance;
import github.poscard8.peritia.util.serialization.ArraySerializable;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class XpGainContext implements JsonSerializable<XpGainContext>
{
    protected final List<Component> components = new ArrayList<>();
    protected int defaultXp;
    protected boolean forUI = false; // only client-side, not serialized
    protected boolean random = false; // only client-side, not serialized
    protected boolean blocked = false; // only client-side, not serialized

    public XpGainContext(int defaultXp) { this.defaultXp = defaultXp; }

    public static XpGainContext empty() { return new XpGainContext(0); }

    public static XpGainContext tryLoad(JsonObject data) { return empty().loadWithFallback(data); }

    public List<Component> components() { return components; }

    public Component getComponent(int index) { return components().get(index); }

    public int size() { return components().size(); }

    public void add(Skill skill) { components().add(new Component(skill, skill.minLevel(), 0, defaultXp())); }

    public void add(SkillInstance instance) { components().add(new Component(instance.skill(), instance.level(), instance.xp(), defaultXp())); }

    public void add(Component component) { components().add(component); }

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

    public int defaultXp() { return defaultXp; }

    public boolean forUI() { return forUI; }

    public void setForUI(boolean forUI) { this.forUI = forUI; }

    public boolean random() { return random; }

    public void setRandom(boolean random) { this.random = random; }

    public boolean blocked() { return blocked; }

    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public XpGainContext copy()
    {
        XpGainContext copy = new XpGainContext(defaultXp());
        components().forEach(copy::add);
        copy.forUI = forUI();
        copy.random = random();
        copy.blocked = blocked();

        return copy;
    }

    public XpGainContext append(XpGainContext other)
    {
        other.components().forEach(this::add);
        optimizeComponents();
        return this;
    }

    public XpGainContext filter(int threshold)
    {
        components().removeIf(component -> component.deltaXp() < threshold);
        return this;
    }

    @Override
    public XpGainContext fallback() { return empty(); }

    @Override
    public XpGainContext load(JsonObject data)
    {
        JsonArray array = JsonHelper.readArray(data, "components");

        for (JsonElement element : array)
        {
            if (element.isJsonArray())
            {
                Component component = Component.tryLoad(element.getAsJsonArray());
                if (!component.isEmpty()) components.add(component);
            }
        }
        this.defaultXp = JsonHelper.readInt(data, "defaultXp", defaultXp);
        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();
        JsonArray array = new JsonArray();

        for (Component component : components) array.add(component.save());
        JsonHelper.write(data, "components", array);
        JsonHelper.write(data, "defaultXp", defaultXp);

        return data;
    }


    public record Component(Skill skill, int level, int xp, int deltaXp) implements ArraySerializable<Component>
    {
        public static Component empty() { return new Component(Skill.empty(), Skill.DEFAULT_MIN_LEVEL, 0, 0); }

        public static Component tryLoad(JsonArray data) { return empty().loadWithFallback(data); }

        public SkillInstance asSkillInstance() { return SkillInstance.fromXpGainContext(this); }

        static boolean canAppend(Component first, Component second) { return first.skill().equals(second.skill()); }

        @Nullable
        static Component tryAppend(Component first, Component second)
        {
            if (canAppend(first, second))
            {
                int delta = first.deltaXp() + second.deltaXp();
                return new Component(second.skill(), second.level(), second.xp(), delta);
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

            int level = data.get(1).getAsInt();
            int xp = data.get(2).getAsInt();
            int deltaXp = data.get(3).getAsInt();

            return new Component(skill, level, xp, deltaXp);
        }

        @Override
        public JsonArray save()
        {
            JsonArray data = new JsonArray();
            data.add(skill().stringKey());
            data.add(level());
            data.add(xp());
            data.add(deltaXp());

            return data;
        }
    }

}
