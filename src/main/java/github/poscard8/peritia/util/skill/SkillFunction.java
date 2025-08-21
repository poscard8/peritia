package github.poscard8.peritia.util.skill;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.data.SkillData;
import github.poscard8.peritia.util.serialization.StringSerializable;
import github.poscard8.peritia.util.text.PeritiaTexts;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

public interface SkillFunction extends StringSerializable<SkillFunction>
{
    static SkillFunction empty() { return None.INSTANCE; }

    static SkillFunction tryLoad(String data)
    {
        if (data.equals("all")) return All.INSTANCE;
        if (Double.isValidString(data)) return Double.tryLoad(data);
        if (Special.isValidString(data)) return Special.tryLoad(data);
        return Single.empty().loadWithFallback(data, empty());
    }

    static SkillFunction loadWithExceptions(String data)
    {
        if (data.equals("all")) return All.INSTANCE;
        if (Double.isValidString(data)) return Double.tryLoad(data);
        if (Special.isValidString(data)) return Special.tryLoad(data);
        return Single.empty().load(data);
    }

    List<Skill> getSkills(SkillData skillData);

    Component xpSourceText(@Nullable Skill menuSkill);

    int count();

    boolean isEmpty();

    @Override
    default SkillFunction fallback() { return empty(); }


    class Single implements SkillFunction
    {
        protected Skill skill;

        public Single(Skill skill) { this.skill = skill; }

        public static Single empty() { return new Single(Skill.empty()); }

        public static Single tryLoad(String data)
        {
            return empty().loadWithFallback(data) instanceof Single single ? single : empty();
        }

        public Skill skill() { return skill; }

        @Override
        public Component xpSourceText(@Nullable Skill menuSkill) { return skill().plainName(); }

        @Override
        public int count() { return 1; }

        @Override
        public boolean isEmpty() { return skill().isEmpty(); }

        @Override
        public List<Skill> getSkills(SkillData skillData) { return List.of(skill()); }

        @Override
        public SkillFunction load(String data)
        {
            this.skill = Skill.byString(data);
            if (skill == null) throw new RuntimeException(String.format("Invalid argument for skill: %s", data));

            return this;
        }

        @Override
        public String save() { return skill.stringKey(); }

    }
    
    class Double implements SkillFunction
    {
        protected Skill first;
        protected Skill second;
        
        public Double(Skill first, Skill second)
        {
            this.first = first;
            this.second = second;
        }
        
        public static Double empty() { return new Double(Skill.empty(), Skill.empty()); }
        
        public static Double tryLoad(String data)
        {
            return empty().loadWithFallback(data) instanceof Double double0 ? double0 : empty();
        }

        public static boolean isValidString(String data)
        {
            if (!data.contains(",")) return false;

            String[] split = data.split(",");
            Skill first = Skill.byString(split[0]);
            Skill second = Skill.byString(split[1]);

            return first != null && second != null && !first.stringKey().equals(second.stringKey());
        }

        public static boolean isValidPrefix(String data)
        {
            if (!data.endsWith(",")) return false;

            String substring = data.substring(0, data.length() - 1);
            return Skill.byString(substring) != null;
        }

        public Skill first() { return first; }

        public Skill second() { return second; }

        public boolean contains(Skill skill) { return first().equals(skill) || second().equals(skill); }

        @Override
        public List<Skill> getSkills(SkillData skillData) { return List.of(first(), second()); }

        @Override
        public Component xpSourceText(@Nullable Skill menuSkill) { return menuSkill == null ? Skill.empty().plainName() : menuSkill.plainName(); }

        @Override
        public int count() { return 2; }

        @Override
        public boolean isEmpty() { return first().isEmpty() || second().isEmpty(); }

        @Override
        public SkillFunction load(String data) 
        {
            String[] split = data.split(",");
            
            Skill first = Skill.byString(split[0]);
            if (first == null) throw new RuntimeException("Invalid argument for skill: " + split[0]);
            this.first = first;

            Skill second = Skill.byString(split[1]);
            if (second == null) throw new RuntimeException("Invalid argument for skill: " + split[1]);
            this.second = second;

            if (first.key().equals(second.key())) throw new RuntimeException("Double skill function has to have different skills");
            return this;
        }

        @Override
        public String save() { return first.stringKey() + "," + second.stringKey(); }
        
    }

    enum Special implements SkillFunction
    {
        HIGHEST("highest", skillData -> List.of(skillData.highestSkill())),
        LOWEST("lowest", skillData -> List.of(skillData.lowestSkill())),
        MEDIAN("median", skillData -> List.of(skillData.medianSkill())),
        RANDOM("random", skillData ->
        {
            int totalCount = Peritia.skills().size();
            int index = new Random().nextInt(totalCount);
            return List.of(Peritia.skills().get(index));
        });

        final String name;
        final Function<SkillData, List<Skill>> skillGetter;

        Special(String name, Function<SkillData, List<Skill>> skillGetter)
        {
            this.name = name;
            this.skillGetter = skillGetter;
        }

        public static Special empty() { return HIGHEST; }

        public static Special tryLoad(String data)
        {
            return empty().loadWithFallback(data) instanceof Special special ? special : empty();
        }

        public static boolean isValidString(String data)
        {
            for (Special special : values())
            {
                if (special.getName().equals(data)) return true;
            }
            return false;
        }

        public String getName() { return name; }

        @Override
        public List<Skill> getSkills(SkillData skillData) { return skillGetter.apply(skillData); }

        @Override
        public int count() { return 1; }

        @Override
        public Component xpSourceText(@Nullable Skill menuSkill) { return Component.translatable("xp_source.peritia.skill." + getName()); }

        @Override
        public boolean isEmpty() { return Peritia.skillHandler().getFirstSkill().isEmpty(); }

        @Override
        public SkillFunction load(String data)
        {
            for (Special special : values())
            {
                if (special.getName().equals(data)) return special;
            }
            throw new RuntimeException(String.format("Invalid argument for skill function: %s", data));
        }

        @Override
        public String save() { return getName(); }
    }

    enum All implements SkillFunction
    {
        INSTANCE;

        @Override
        public List<Skill> getSkills(SkillData skillData) { return Peritia.skills(); }

        @Override
        public Component xpSourceText(@Nullable Skill menuSkill) { return Component.translatable("xp_source.peritia.skill.all"); }

        @Override
        public int count() { return Peritia.skills().size(); }

        @Override
        public boolean isEmpty() { return false; }

        @Override
        public SkillFunction load(String data) { return INSTANCE; }

        @Override
        public String save() { return "all"; }
    }

    enum None implements SkillFunction
    {
        INSTANCE;

        @Override
        public List<Skill> getSkills(SkillData skillData) { return List.of(); }

        @Override
        public Component xpSourceText(@Nullable Skill menuSkill) { return PeritiaTexts.empty(); }

        @Override
        public int count() { return 0; }

        @Override
        public boolean isEmpty() { return true; }

        @Override
        public SkillFunction load(String data) { return INSTANCE; }

        @Override
        public String save() { return "none"; }
    }

}
