package github.poscard8.peritia.util.xpsource;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.util.PeritiaRegistries;
import github.poscard8.peritia.util.serialization.StringSerializable;
import github.poscard8.peritia.util.skill.SkillFunction;
import github.poscard8.peritia.xpsource.DataXpSource;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.XpSourceType;
import github.poscard8.peritia.xpsource.type.BlockXpSource;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public interface DataXpSourceFunction extends Predicate<DataXpSource>, Iterable<DataXpSource>, StringSerializable<DataXpSourceFunction>
{
    static DataXpSourceFunction empty() { return None.INSTANCE; }

    static DataXpSourceFunction loadWithExceptions(String data)
    {
        if (data.equals("all")) return All.INSTANCE;
        if (SingleSkill.isValidString(data)) return SingleSkill.tryLoad(data);
        if (Type.isValidString(data)) return Type.tryLoad(data);
        return Single.empty().load(data);
    }

    boolean isEmpty();

    default List<DataXpSource> getXpSources() { return Peritia.xpSourceHandler().dataXpSources().stream().filter(this).toList(); }

    @Override
    @NotNull
    default Iterator<DataXpSource> iterator() { return getXpSources().iterator(); }

    @Override
    default DataXpSourceFunction fallback() { return empty(); }


    class Single implements DataXpSourceFunction
    {
        protected DataXpSource xpSource;

        public Single(DataXpSource xpSource) { this.xpSource = xpSource; }

        public static Single empty() { return new Single(BlockXpSource.empty()); }

        public DataXpSource xpSource() { return xpSource; }

        @Override
        public boolean isEmpty() { return xpSource() != null; }

        @Override
        public boolean test(DataXpSource xpSource) { return xpSource().equals(xpSource); }

        @Override
        public DataXpSourceFunction load(String data)
        {
            XpSource xpSource = XpSource.byKey(ResourceLocation.tryParse(data));

            if (xpSource == null)
            {
                throw new RuntimeException(String.format("Invalid argument for xp source: %s", data));
            }
            else if (!(xpSource instanceof DataXpSource))
            {
                throw new RuntimeException(String.format("Xp source %s is not a data xp source", data));
            }

            this.xpSource = (DataXpSource) xpSource;
            return this;
        }

        @Override
        public String save() { return xpSource.stringKey(); }
    }

    class SingleSkill implements DataXpSourceFunction
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
        public boolean test(DataXpSource xpSource)
        {
            SkillFunction skillFunction = xpSource.skillFunction();

            return skillFunction instanceof SkillFunction.All ||
                    skillFunction instanceof SkillFunction.Single single && single.skill().equals(skill()) ||
                    skillFunction instanceof SkillFunction.Double double0 && double0.contains(skill());
        }

        @Override
        public boolean isEmpty() { return skill().isEmpty(); }

        @Override
        public DataXpSourceFunction load(String data)
        {
            if (!isValidString(data)) throw new RuntimeException(String.format("Invalid argument for xp source function: %s", data));

            String string = data.substring(1);
            this.skill = Skill.byString(string);
            return this;
        }

        @Override
        public String save() { return "#" + skill.stringKey(); }
    }

    class Type implements DataXpSourceFunction
    {
        @Nullable
        protected XpSourceType<?> type;

        public Type(@Nullable XpSourceType<?> type) { this.type = type; }

        public static Type empty() { return new Type(null); }

        public static Type tryLoad(String data)
        {
            return empty().loadWithFallback(data) instanceof Type type ? type : empty();
        }

        public static boolean isValidString(String data)
        {
            if (!data.startsWith("$")) return false;
            String string = data.substring(1);

            ResourceLocation key = ResourceLocation.tryParse(string);
            if (key == null) return false;

            XpSourceType<?> type = PeritiaRegistries.xpSourceTypes().getValue(key);
            return type != null && type.isDataType();
        }

        @Nullable
        public XpSourceType<?> type() { return type; }

        @Override
        public boolean test(DataXpSource xpSource) { return xpSource.type() == type(); }

        @Override
        public boolean isEmpty() { return type == null || !type.isDataType(); }

        @Override
        public DataXpSourceFunction load(String data)
        {
            if (!isValidString(data)) throw new RuntimeException(String.format("Invalid argument for xp source function: %s", data));

            ResourceLocation key = ResourceLocation.tryParse(data.substring(1));

            this.type = PeritiaRegistries.xpSourceTypes().getValue(key);
            return this;
        }

        @Override
        public String save() { return type == null ? "none" : "$" + type.key().toString(); }
    }

    enum All implements DataXpSourceFunction
    {
        INSTANCE;

        @Override
        public boolean test(DataXpSource xpSource) { return true; }

        @Override
        public boolean isEmpty() { return false; }

        @Override
        public DataXpSourceFunction load(String data) { return INSTANCE; }

        @Override
        public String save() { return "all"; }
    }

    enum None implements DataXpSourceFunction
    {
        INSTANCE;

        @Override
        public boolean test(DataXpSource xpSource) { return false; }

        @Override
        public boolean isEmpty() { return true; }

        @Override
        public DataXpSourceFunction load(String data) { return INSTANCE; }

        @Override
        public String save() { return "none"; }
    }

}
