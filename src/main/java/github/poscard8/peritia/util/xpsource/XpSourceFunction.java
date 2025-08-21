package github.poscard8.peritia.util.xpsource;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.serialization.StringSerializable;
import github.poscard8.peritia.util.skill.SkillFunction;
import github.poscard8.peritia.xpsource.XpSource;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public interface XpSourceFunction extends Predicate<XpSource>, Iterable<XpSource>, StringSerializable<XpSourceFunction>
{
    static XpSourceFunction empty() { return None.INSTANCE; }

    static XpSourceFunction loadWithExceptions(String data)
    {
        if (data.equals("all")) return All.INSTANCE;
        if (SingleSkill.isValidString(data)) return SingleSkill.tryLoad(data);
        return Single.empty().load(data);
    }

    boolean isEmpty();

    default List<XpSource> getXpSources() { return Peritia.xpSources().stream().filter(this).toList(); }

    @Override
    @NotNull
    default Iterator<XpSource> iterator() { return getXpSources().iterator(); }

    @Override
    default XpSourceFunction fallback() { return empty(); }


    class Single implements XpSourceFunction
    {
        protected XpSource xpSource;

        public Single(XpSource xpSource) { this.xpSource = xpSource; }

        public static Single empty() { return new Single(XpSource.empty()); }

        public XpSource xpSource() { return xpSource; }

        @Override
        public boolean isEmpty() { return xpSource() != null; }

        @Override
        public boolean test(XpSource xpSource) { return xpSource().equals(xpSource); }

        @Override
        public XpSourceFunction load(String data)
        {
            XpSource xpSource = XpSource.byKey(ResourceLocation.tryParse(data));

            if (xpSource == null)
            {
                throw new RuntimeException(String.format("Invalid argument for xp source: %s", data));
            }

            this.xpSource = xpSource;
            return this;
        }

        @Override
        public String save() { return xpSource.stringKey(); }
    }

    class SingleSkill implements XpSourceFunction
    {
        protected Skill skill;

        public SingleSkill(Skill skill) { this.skill = skill; }

        public static SingleSkill empty() { return new SingleSkill(Skill.empty()); }

        public static SingleSkill tryLoad(String data)
        {
            return empty().loadWithFallback(data) instanceof SingleSkill singleSkill ? singleSkill : empty();
        }

        public static boolean isValidString(String data)
        {
            if (!data.startsWith("#")) return false;
            String string = data.substring(1);
            return Skill.byString(string) != null;
        }

        public Skill skill() { return skill; }

        @Override
        public boolean test(XpSource xpSource)
        {
            SkillFunction skillFunction = xpSource.skillFunction();

            return skillFunction instanceof SkillFunction.All ||
                    skillFunction instanceof SkillFunction.Single single && single.skill().equals(skill()) ||
                    skillFunction instanceof SkillFunction.Double double0 && double0.contains(skill());
        }

        @Override
        public boolean isEmpty() { return skill().isEmpty(); }

        @Override
        public XpSourceFunction load(String data)
        {
            if (!isValidString(data)) throw new RuntimeException(String.format("Invalid argument for xp source function: %s", data));

            String string = data.substring(1);
            this.skill = Skill.byString(string);
            return this;
        }

        @Override
        public String save() { return "#" + skill.stringKey(); }
    }

    enum All implements XpSourceFunction
    {
        INSTANCE;

        @Override
        public boolean test(XpSource xpSource) { return true; }

        @Override
        public boolean isEmpty() { return false; }

        @Override
        public XpSourceFunction load(String data) { return INSTANCE; }

        @Override
        public String save() { return "all"; }
    }

    enum None implements XpSourceFunction
    {
        INSTANCE;

        @Override
        public boolean test(XpSource xpSource) { return false; }

        @Override
        public boolean isEmpty() { return true; }

        @Override
        public XpSourceFunction load(String data) { return INSTANCE; }

        @Override
        public String save() { return "none"; }
    }

}
