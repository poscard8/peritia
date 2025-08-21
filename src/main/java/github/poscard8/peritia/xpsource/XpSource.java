package github.poscard8.peritia.xpsource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.skill.data.ServerSkillData;
import github.poscard8.peritia.skill.data.SkillData;
import github.poscard8.peritia.util.PeritiaRegistries;
import github.poscard8.peritia.util.serialization.*;
import github.poscard8.peritia.util.skill.SkillFunction;
import github.poscard8.peritia.xpsource.type.EmptyXpSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public abstract class XpSource implements JsonSerializable<XpSource>, Comparable<XpSource>, Loadable
{
    protected ResourceLocation key;
    protected SkillFunction skillFunction = SkillFunction.empty();
    protected int xp = 0;
    protected Visibility visibility = Visibility.empty();
    protected short index = 0;
    protected JsonArray conditions = new JsonArray();

    public XpSource(ResourceLocation key) { this.key = key; }

    public static XpSource empty() { return EmptyXpSource.empty(); }

    public static XpSource tryLoad(JsonObject data)
    {
        ResourceLocation typeKey = JsonHelper.readResource(data, "type");

        @Nullable XpSourceType<?> type = PeritiaRegistries.xpSourceTypes().getValue(typeKey);
        if (type == null) return empty();

        @Nullable XpSource xpSource = type.loadXpSource(data);
        return xpSource != null ? xpSource : empty();
    }

    public static XpSource tryLoad(ResourceLocation key, JsonObject data)
    {
        JsonHelper.write(data, "key", key);
        return tryLoad(data);
    }

    @Nullable
    public static XpSource byString(String stringKey)
    {
        if (stringKey.contains(":"))
        {
            ResourceLocation key = ResourceLocation.tryParse(stringKey);
            return key == null ? null : byKey(key);
        }
        else
        {
            for (String namespace : DEFAULT_NAMESPACES)
            {
                ResourceLocation key = ResourceLocation.tryBuild(namespace, stringKey);
                XpSource xpSource = key == null ? null : byKey(key);
                if (xpSource != null) return xpSource;
            }
            return null;
        }
    }

    @Nullable
    public static XpSource byKey(ResourceLocation key) { return Peritia.xpSourceHandler().byKey(key); }

    public static boolean canPlayerGainXp(Player player) { return !player.isCreative() && !player.isSpectator(); }

    @Override
    public ResourceLocation key() { return key; }

    public abstract XpSourceType<?> type();

    public ResourceLocation typeKey() { return type().key(); }

    public SkillFunction skillFunction() { return skillFunction; }

    public int xp() { return xp; }

    public Visibility visibility() { return visibility; }

    public boolean shouldShow(Player player, SkillData skillData) { return visibility().showPredicate.test(player, skillData, this); }

    public boolean shouldHide(Player player, SkillData skillData) { return visibility().hidePredicate.test(player, skillData, this); }

    public short index() { return index; }

    @Override
    public JsonArray conditions() { return conditions; }

    @Override
    public boolean isInvalid()
    {
        return Loadable.super.isInvalid() ||
                type().isEmpty() ||
                skillFunction().isEmpty() ||
                xp() <= 0;
    }

    public void tryAward(ServerPlayer player) { tryAward(player, 1); }

    public void tryAward(ServerPlayer player, float multiplier)
    {
        if (canPlayerGainXp(player)) award(player, multiplier);
    }

    public void award(ServerPlayer player) { award(player, 1); }

    public void award(ServerPlayer player, float multiplier) { ServerSkillData.of(player).addXpToSkills(skillFunction(), this, Math.round(xp() * multiplier)); }

    @Override
    public XpSource fallback() { return null; }

    @Override
    public final XpSource load(JsonObject data)
    {
        this.key = JsonHelper.readResource(data, "key", key);
        this.skillFunction = JsonHelper.readStringSerializable(data, "skill", SkillFunction::tryLoad, skillFunction);
        this.xp = JsonHelper.readInt(data, "xp", xp);
        this.visibility = JsonHelper.readStringSerializable(data, "visibility", Visibility::tryLoad, visibility);
        this.index = JsonHelper.readShort(data, "index", index);
        this.conditions = JsonHelper.readArray(data, "conditions", conditions);

        loadAdditional(data);
        return this;
    }

    public abstract void loadAdditional(JsonObject data);

    @Override
    public final JsonObject save()
    {
        JsonObject data = new JsonObject();
        JsonHelper.write(data, "key", key);
        JsonHelper.write(data, "type", typeKey());
        JsonHelper.write(data, "skill", skillFunction);
        JsonHelper.write(data, "xp", xp);
        JsonHelper.write(data, "visibility", visibility);
        JsonHelper.write(data, "index", index);
        JsonHelper.write(data, "conditions", conditions);

        saveAdditional(data);
        return data;
    }

    public abstract void saveAdditional(JsonObject data);


    @Override
    public int compareTo(@NotNull XpSource other) { return index() - other.index(); }

    @Override
    public boolean equals(Object object) { return object instanceof XpSource xpSource && key().equals(xpSource.key()); }

    @Override
    public String toString() { return String.format("Xp source: %s", stringKey()); }


    public enum Visibility implements StringSerializable<Visibility>
    {
        DEFAULT("default", (player, skillData, xpSource) -> !XpSource.canPlayerGainXp(player) || skillData.hasDiscovered(xpSource)),
        SHOW("show", (player, skillData, xpSource) -> true),
        HIDE("hide", (player, skillData, xpSource) -> false, (player, skillData, xpSource) -> false);

        private final String name;
        private final TriPredicate<Player, SkillData, XpSource> showPredicate;
        private final TriPredicate<Player, SkillData, XpSource> hidePredicate;

        Visibility(String name, TriPredicate<Player, SkillData, XpSource> showPredicate) { this(name, showPredicate, showPredicate.negate()); }

        Visibility(String name, TriPredicate<Player, SkillData, XpSource> showPredicate, TriPredicate<Player, SkillData, XpSource> hidePredicate)
        {
            this.name = name;
            this.showPredicate = showPredicate;
            this.hidePredicate = hidePredicate;
        }

        public String getName() { return name; }

        public static Visibility empty() { return Visibility.DEFAULT; }

        public static Visibility tryLoad(String data) { return empty().loadWithFallback(data); }

        @Override
        public Visibility fallback() { return empty(); }

        @Override
        public Visibility load(String data)
        {
            return Arrays.stream(values()).filter(visibility -> visibility.getName().equals(data)).findFirst().orElse(fallback());
        }

        @Override
        public String save() { return getName(); }

    }

}
