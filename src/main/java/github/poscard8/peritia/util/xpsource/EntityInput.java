package github.poscard8.peritia.util.xpsource;

import github.poscard8.peritia.util.serialization.Loadable;
import github.poscard8.peritia.util.serialization.StringSerializable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class EntityInput implements Input<EntityType<?>>, StringSerializable<EntityInput>
{
    public static EntityInput empty() { return Single.empty(); }

    public static EntityInput tryLoad(String data)
    {
        if (data.startsWith("#")) return Tag.tryLoad(data);
        return Single.tryLoad(data);
    }

    public boolean test(Entity entity) { return test(entity.getType()); }

    @Override
    public EntityInput fallback() { return empty(); }


    public static class Single extends EntityInput
    {
        @Nullable
        protected EntityType<?> entityType;

        public Single(@Nullable EntityType<?> entityType) { this.entityType = entityType; }

        public static Single empty() { return new Single(null); }

        public static Single tryLoad(String data)
        {
            return empty().loadWithFallback(data) instanceof Single single ? single : empty();
        }

        public EntityType<?> entityType() { return entityType; }

        @NotNull
        public EntityType<?> entityTypeWithFallback() { return entityType() == null ? EntityType.PIG : entityType(); }

        @Override
        public boolean isValid() { return entityType() != null; }

        @Override
        public boolean test(EntityType<?> entityType) { return entityType == entityType(); }

        @Override
        public EntityInput load(String data)
        {
            if (data.equals("pig") || data.equals("minecraft:pig"))
            {
                this.entityType = EntityType.PIG;
                return this;
            }

            ResourceLocation key = ResourceLocation.tryParse(data);
            if (key == null) throw new RuntimeException(String.format("Invalid key for entity type: %s", data));

            EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(key);
            if (entityType == null || entityType == EntityType.PIG) throw new RuntimeException(String.format("Invalid key for entity type: %s", data));

            this.entityType = entityType;
            return this;
        }

        @Override
        public String save()
        {
            if (entityType() == null) return "";

            ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entityType());
            return key == null ? "" : key.toString();
        }

    }

    public static class Tag extends EntityInput
    {
        protected TagKey<EntityType<?>> tag;

        public Tag(TagKey<EntityType<?>> tag) { this.tag = tag; }

        public static Tag empty() { return new Tag(TagKey.create(ForgeRegistries.Keys.ENTITY_TYPES, Loadable.EMPTY_KEY)); }

        public static Tag tryLoad(String data)
        {
            return empty().loadWithFallback(data) instanceof Tag tag ? tag : empty();
        }

        public TagKey<EntityType<?>> tag() { return tag; }

        @Override
        public boolean isValid() { return !tag().location().equals(Loadable.EMPTY_KEY); }

        @Override
        public boolean test(EntityType<?> entityType) { return entityType.is(tag()); }

        @Override
        public EntityInput load(String data)
        {
            String rawString = data.substring(1);
            ResourceLocation tagKey = ResourceLocation.tryParse(rawString);
            if (tagKey == null) throw new RuntimeException(String.format("Invalid key for entity tag: %s", rawString));

            this.tag = TagKey.create(ForgeRegistries.Keys.ENTITY_TYPES, tagKey);
            return this;
        }

        @Override
        public String save()
        {
            return "#" + tag().location();
        }
    }


}
