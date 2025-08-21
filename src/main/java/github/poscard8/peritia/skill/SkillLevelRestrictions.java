package github.poscard8.peritia.skill;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import github.poscard8.peritia.util.serialization.ArraySerializable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SkillLevelRestrictions implements SkillAssignable, Iterable<LevelRestriction>, ArraySerializable<SkillLevelRestrictions>
{
    protected List<LevelRestriction> restrictions;

    public SkillLevelRestrictions(List<LevelRestriction> restrictions) { this.restrictions = restrictions; }

    public static SkillLevelRestrictions empty() { return new SkillLevelRestrictions(new ArrayList<>()); }

    public static SkillLevelRestrictions tryLoad(JsonArray data) { return empty().loadWithFallback(data); }

    public List<LevelRestriction> restrictions() { return restrictions; }

    @Override
    public void assignSkill(Skill skill)
    {
        restrictions().forEach(restriction -> restriction.assignSkill(skill));
    }

    @Override
    @NotNull
    public Iterator<LevelRestriction> iterator() { return restrictions().iterator(); }

    @Override
    public SkillLevelRestrictions fallback() { return empty(); }

    @Override
    public SkillLevelRestrictions load(JsonArray data)
    {
        for (JsonElement element : data)
        {
            if (element.isJsonObject()) restrictions.add(LevelRestriction.tryLoad(element.getAsJsonObject()));
        }

        return this;
    }

    @Override
    public JsonArray save()
    {
        JsonArray data = new JsonArray();
        for (LevelRestriction restriction : this) data.add(restriction.save());

        return data;
    }

}
