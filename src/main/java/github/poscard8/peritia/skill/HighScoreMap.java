package github.poscard8.peritia.skill;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class HighScoreMap implements JsonSerializable<HighScoreMap>
{
    protected Map<Skill, Integer> map;

    public HighScoreMap(Map<Skill, Integer> map) { this.map = map; }

    public static HighScoreMap empty() { return new HighScoreMap(emptyHighScoreMap()); }

    public static HighScoreMap max() { return new HighScoreMap(maxHighScoreMap()); }

    public static HighScoreMap tryLoad(JsonObject data) { return empty().loadWithFallback(data); }

    public static Map<Skill, Integer> emptyHighScoreMap()
    {
        Map<Skill, Integer> map = new HashMap<>();
        Peritia.skills().forEach(skill -> map.put(skill, skill.minLevel()));
        return map;
    }

    public static Map<Skill, Integer> maxHighScoreMap()
    {
        Map<Skill, Integer> map = new HashMap<>();
        Peritia.skills().forEach(skill -> map.put(skill, skill.maxLevel()));
        return map;
    }

    public Map<Skill, Integer> map() { return map; }

    public int getHighScore(Skill skill)
    {
        for (Skill existing : map().keySet())
        {
            if (existing.key().equals(skill.key())) return map().get(existing);
        }
        map().put(skill, skill.minLevel());
        return skill.minLevel();
    }

    public void tryPutHighScore(SkillInstance instance) { tryPutHighScore(instance.skill(), instance.level()); }

    public void tryPutHighScore(Skill skill, int highScore)
    {
        if (highScore > getHighScore(skill)) map().put(skill, highScore);
    }

    @Override
    public HighScoreMap fallback() { return empty(); }

    @Override
    public HighScoreMap load(JsonObject data)
    {
        for (Map.Entry<String, JsonElement> entry : data.entrySet())
        {
            String string = entry.getKey();
            int highScore = entry.getValue().getAsInt();

            ResourceLocation key = ResourceLocation.tryParse(string);
            if (key == null) continue;

            Skill skill = Skill.byKey(key);
            if (skill == null) continue;

            map.put(skill, highScore);
        }
        return this;
    }

    @Override
    public JsonObject save()
    {
        JsonObject data = new JsonObject();

        for (Map.Entry<Skill, Integer> entry : map.entrySet())
        {
            Skill skill = entry.getKey();
            int highScore = entry.getValue();
            JsonHelper.write(data, skill.stringKey(), highScore);
        }
        return data;
    }

}
