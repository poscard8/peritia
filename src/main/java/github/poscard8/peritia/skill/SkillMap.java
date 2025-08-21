package github.poscard8.peritia.skill;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SkillMap implements JsonSerializable<SkillMap>
{
    protected Map<Skill, SkillInstance> map;

    public SkillMap(Map<Skill, SkillInstance> map) { this.map = map; }

    public static SkillMap empty() { return new SkillMap(emptySkillMap()); }

    public static SkillMap max() { return new SkillMap(maxSkillMap()); }

    public static SkillMap tryLoad(JsonObject data) { return empty().loadWithFallback(data); }

    public static Map<Skill, SkillInstance> emptySkillMap()
    {
        Map<Skill, SkillInstance> map = new HashMap<>();
        Peritia.skills().forEach(skill -> map.put(skill, skill.newInstance()));
        return map;
    }

    public static Map<Skill, SkillInstance> maxSkillMap()
    {
        Map<Skill, SkillInstance> map = new HashMap<>();
        Peritia.skills().forEach(skill -> map.put(skill, skill.maxInstance()));
        return map;
    }

    public Map<Skill, SkillInstance> map() { return map; }

    public Collection<Skill> keys() { return map().keySet(); }

    public Collection<SkillInstance> values() { return map().values(); }

    public SkillInstance getSkill(Skill skill)
    {
        for (Skill existing : keys())
        {
            if (existing.key().equals(skill.key())) return map().get(existing);
        }
        SkillInstance instance = skill.newInstance();
        map().put(skill, instance);
        return instance;
    }

    public int getLevel(Skill skill) { return getSkill(skill).level(); }

    public void putSkill(SkillInstance instance) { map().put(instance.skill(), instance); }

    @Override
    public SkillMap fallback() { return empty(); }

    @Override
    public SkillMap load(JsonObject data)
    {
        for (Map.Entry<String, JsonElement> entry : data.entrySet())
        {
            String string = entry.getKey();
            JsonElement element = entry.getValue();

            if (!element.isJsonObject()) continue;

            JsonObject jsonObject = element.getAsJsonObject();
            ResourceLocation key = ResourceLocation.tryParse(string);
            if (key == null) continue;

            Skill skill = Skill.byKey(key);
            if (skill == null) continue;

            SkillInstance instance = SkillInstance.tryLoad(jsonObject, skill);
            map.put(skill, instance);
        }
        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();

        for (Map.Entry<Skill, SkillInstance> entry : map.entrySet())
        {
            Skill skill = entry.getKey();
            SkillInstance instance = entry.getValue();
            JsonHelper.write(data, skill.stringKey(), instance);
        }
        return data;
    }

}
