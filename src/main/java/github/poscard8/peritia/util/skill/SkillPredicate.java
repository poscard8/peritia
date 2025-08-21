package github.poscard8.peritia.util.skill;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.serialization.StringSerializable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Predicate;

public class SkillPredicate implements Predicate<Skill>, Iterable<Skill>, StringSerializable<SkillPredicate>
{
    public static final String ANY_NAME = "any";

    protected @Nullable ResourceLocation skillKey = null;
    protected boolean acceptAny = true;

    public SkillPredicate() {}

    public SkillPredicate(@Nullable ResourceLocation skillKey)
    {
        this.skillKey = skillKey;
        this.acceptAny = skillKey == null;
    }

    public static SkillPredicate empty() { return new SkillPredicate(); }

    public static SkillPredicate tryLoad(String data) { return empty().loadWithFallback(data); }

    @Nullable
    public ResourceLocation skillKey() { return skillKey; }

    @Nullable
    public Skill skill() { return skillKey == null ? null : Skill.byKey(skillKey()); }

    public boolean acceptsAny() { return acceptAny; }

    @Override
    public boolean test(Skill skill) { return acceptsAny() || skill.equals(skill()); }

    @Override
    @NotNull
    public Iterator<Skill> iterator() { return Peritia.skills().stream().filter(this).iterator(); }

    @Override
    public SkillPredicate fallback() { return empty(); }

    @Override
    public SkillPredicate load(String data)
    {
        ResourceLocation recipeKey = data.equals(ANY_NAME) ? null : ResourceLocation.tryParse(data);
        return new SkillPredicate(recipeKey);
    }

    @Override
    public String save() { return acceptAny || skillKey == null ? ANY_NAME : skillKey.toString(); }



}
