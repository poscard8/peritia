package github.poscard8.peritia.xpsource.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.poscard8.peritia.network.ClientHandler;
import github.poscard8.peritia.registry.PeritiaXpSourceTypes;
import github.poscard8.peritia.util.serialization.JsonHelper;
import github.poscard8.peritia.util.serialization.JsonSerializable;
import github.poscard8.peritia.util.xpsource.EntityInputs;
import github.poscard8.peritia.util.xpsource.GearFunction;
import github.poscard8.peritia.xpsource.DataXpSource;
import github.poscard8.peritia.xpsource.XpSource;
import github.poscard8.peritia.xpsource.XpSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityXpSource extends DataXpSource
{
    protected final Map<String, Context> contextMap = new HashMap<>();

    protected EntityInputs inputs = EntityInputs.empty();
    protected GearFunction gearFunction = GearFunction.empty();
    protected float assistRange = 4.0F;
    protected boolean allowFarming = false;

    public EntityXpSource(ResourceLocation key) { super(key); }

    public static EntityXpSource empty() { return new EntityXpSource(EMPTY_KEY); }

    @Nullable
    public static EntityXpSource tryLoad(JsonObject data)
    {
        @Nullable XpSource xpSource = empty().loadWithFallback(data);
        return xpSource != null ? (EntityXpSource) xpSource : null;
    }

    @Override
    public XpSourceType<?> type() { return PeritiaXpSourceTypes.ENTITY.get(); }

    public EntityInputs inputs() { return inputs; }

    public GearFunction gearFunction() { return gearFunction; }

    public float assistRange() { return assistRange; }

    public boolean isFarmingAllowed() { return allowFarming; }

    @Override
    public boolean isValidDataSource() { return !isFarmingAllowed(); }

    @Override
    public boolean isInvalid() { return super.isInvalid() || inputs().isEmpty(); }

    public Context getContextClient()
    {
        tryLoadClient();
        return contextMap.getOrDefault(ClientHandler.getPlayerUUID(), Context.empty());
    }

    public Context getContext(ServerPlayer player)
    {
        tryLoad(player);
        return contextMap.getOrDefault(player.getStringUUID(), Context.empty());
    }

    public void setContext(ServerPlayer player, Context context)
    {
        contextMap.put(player.getStringUUID(), context);
        update(player);
    }

    public void awardAssistors(ServerPlayer player, Entity entity, float multiplier)
    {
        Vec3 origin = entity.position();
        Vec3 minPos = origin.subtract(assistRange(), assistRange(), assistRange());
        Vec3 maxPos = origin.add(assistRange(), assistRange(), assistRange());

        AABB area = new AABB(minPos, maxPos);
        List<ServerPlayer> assistors = player.level().getEntitiesOfClass(ServerPlayer.class, area);

        assistors.remove(player);
        assistors.forEach(assistor -> award(assistor, multiplier));
    }

    public void handleEntityKill(ServerPlayer player, Entity entity)
    {
        if (!canPlayerGainXp(player)) return;
        float gearMultiplier = gearFunction().getMultiplier(entity);

        if (isFarmingAllowed())
        {
            if (inputs().test(entity))
            {
                award(player, gearMultiplier);
                awardAssistors(player, entity, gearMultiplier);
            }
        }
        else
        {
            Context context = getContext(player);

            if (inputs().test(entity))
            {
                float multiplier = context.getMultiplier(entity) * gearMultiplier;

                award(player, multiplier);
                awardAssistors(player, entity, multiplier);
            }
            context.update(entity);
            setContext(player, context);
        }
    }

    @Override
    public void loadData(JsonObject data)
    {
        contextMap.clear();

        for (Map.Entry<String, JsonElement> entry : data.entrySet())
        {
            contextMap.put(entry.getKey(), Context.tryLoad(entry.getValue().getAsJsonObject()));
        }
    }

    @Override
    public JsonObject saveData()
    {
        JsonObject data = new JsonObject();

        for (String key : contextMap.keySet())
        {
            Context value = contextMap.get(key);
            JsonHelper.write(data, key, value);
        }
        return data;
    }

    @Override
    public void loadAdditional(JsonObject data)
    {
        this.inputs = JsonHelper.readElementSerializable(data, "inputs", EntityInputs::tryLoad, inputs);
        this.gearFunction = JsonHelper.readStringSerializable(data, "gearFunction", GearFunction::tryLoad, gearFunction);
        this.assistRange = JsonHelper.readFloat(data, "assistRange", assistRange);
        this.allowFarming = JsonHelper.readBoolean(data, "allowFarming", allowFarming);
    }

    @Override
    public void saveAdditional(JsonObject data)
    {
        JsonHelper.write(data, "inputs", inputs);
        JsonHelper.write(data, "gearFunction", gearFunction);
        JsonHelper.write(data, "assistRange", assistRange);
        JsonHelper.write(data, "allowFarming", allowFarming);
    }


    public static class Context implements JsonSerializable<Context>
    {
        protected EntityType<?> entityType;
        protected int killStreak;

        public Context(EntityType<?> entityType, int killStreak)
        {
            this.entityType = entityType;
            this.killStreak = killStreak;
        }

        public static Context empty() { return new Context(EntityType.PIG, 0); }

        public static Context tryLoad(JsonObject data) { return empty().loadWithFallback(data); }

        public EntityType<?> entityType() { return entityType; }

        public void setEntityType(EntityType<?> entityType) { this.entityType = entityType; }

        public int killStreak() { return killStreak; }

        public void setKillStreak(int killStreak) { this.killStreak = killStreak; }

        public void incrementKillStreak() { setKillStreak(killStreak() + 1); }

        public float getMultiplier(Entity newEntityKilled)
        {
            if (entityType() == newEntityKilled.getType())
            {
                int x = Math.max(0, killStreak() - 4);
                int xSquare = (int) Math.pow(x, 2);
                return (float) (0.8F * (Math.pow(0.975F, xSquare)) + 0.2F);
            }
            return 1;
        }

        public void update(Entity newEntityKilled)
        {
            if (entityType() == newEntityKilled.getType())
            {
                incrementKillStreak();
            }
            else
            {
                setEntityType(newEntityKilled.getType());
                setKillStreak(1);
            }
        }

        @Override
        public Context fallback() { return empty(); }

        @Override
        public Context load(JsonObject data)
        {
            this.entityType = JsonHelper.readRegistrable(data, "entityType", ForgeRegistries.ENTITY_TYPES, entityType);
            this.killStreak = JsonHelper.readInt(data, "killStreak", killStreak);

            return this;
        }

        @Override
        public JsonObject save()
        {
            JsonObject data = new JsonObject();
            JsonHelper.write(data, "entityType", entityType, ForgeRegistries.ENTITY_TYPES);
            JsonHelper.write(data, "killStreak", killStreak);

            return data;
        }
    }

}
