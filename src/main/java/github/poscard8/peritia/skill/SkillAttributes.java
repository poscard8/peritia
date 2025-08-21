package github.poscard8.peritia.skill;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import github.poscard8.peritia.util.serialization.ArraySerializable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SkillAttributes implements Iterable<SkillAttributeInstance>, ArraySerializable<SkillAttributes>
{
    protected List<SkillAttributeInstance> attributes;

    public SkillAttributes(List<SkillAttributeInstance> attributes) { this.attributes = attributes; }

    public static SkillAttributes empty() { return new SkillAttributes(new ArrayList<>()); }

    public static SkillAttributes tryLoad(JsonArray data) { return empty().loadWithFallback(data); }

    public List<SkillAttributeInstance> attributes() { return attributes; }

    @Override
    @NotNull
    public Iterator<SkillAttributeInstance> iterator() { return attributes().iterator(); }

    public SkillAttributes copy() { return new SkillAttributes(new ArrayList<>(attributes())); }

    @Override
    public SkillAttributes fallback() { return empty(); }

    @Override
    public SkillAttributes load(JsonArray data) {

        for (JsonElement element : data)
        {
            if (element.isJsonObject()) attributes.add(SkillAttributeInstance.tryLoad(element.getAsJsonObject()));
        }
        return this;
    }

    @Override
    public JsonArray save()
    {
        JsonArray data = new JsonArray();
        for (SkillAttributeInstance attribute : this) data.add(attribute.save());

        return data;
    }

}
