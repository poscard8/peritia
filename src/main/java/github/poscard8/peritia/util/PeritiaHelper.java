package github.poscard8.peritia.util;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import github.poscard8.peritia.util.serialization.JsonHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.phys.*;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

public class PeritiaHelper
{
    @Nullable
    static MinecraftServer SERVER;

    @Nullable
    public static MinecraftServer getServer() { return SERVER; }

    public static void setServer(@Nullable MinecraftServer server) { SERVER = server; }

    public static void executeOnServer(Entity entity, Consumer<ServerPlayer> consumer)
    {
        if (entity instanceof ServerPlayer player) consumer.accept(player);
    }

    public static StructureManager getStructureManager(ServerPlayer player)
    {
        MinecraftServer server = Objects.requireNonNull(player.getServer());
        return Objects.requireNonNull(server.getLevel(player.level().dimension())).structureManager();
    }

    @Nullable
    public static ServerPlayer getLookedAtPlayer(ServerPlayer player)
    {
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getViewVector(1.0F).scale(10.0F);
        Vec3 endPos = eyePos.add(lookVec);

        BlockHitResult blockHit = player.level().clip(new ClipContext(
                eyePos,
                endPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        if (blockHit.getType() != HitResult.Type.MISS) endPos = blockHit.getLocation();
        AABB area = player.getBoundingBox().expandTowards(lookVec).inflate(1.0F);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player.level(), player, eyePos, endPos,
                area, entity -> entity instanceof ServerPlayer other && player != other && !other.isInvisibleTo(player), 0.0F
        );
        return entityHit == null ? null : (ServerPlayer) entityHit.getEntity();
    }

    public static void giveExperienceToPlayer(ServerPlayer player, int xp) { ExperienceOrb.award((ServerLevel) player.level(), player.getEyePosition().add(0, -1, 0), xp); }

    public static Map<Enchantment, Integer> getEnchantmentMap(ItemStack stack)
    {
        Map<Enchantment, Integer> map = new HashMap<>();

        if (stack.isEmpty()) return map;
        if (!(stack.getItem() instanceof EnchantedBookItem)) return stack.getAllEnchantments();

        ListTag listTag = EnchantedBookItem.getEnchantments(stack);
        int size = listTag.size();

        for (int i = 0; i < size; i++) {

            try {

                CompoundTag compoundTag = listTag.getCompound(i);

                String id = compoundTag.getString("id");
                ResourceLocation key = ResourceLocation.tryParse(id);
                short level = compoundTag.getShort("lvl");

                if (ForgeRegistries.ENCHANTMENTS.containsKey(key)) map.put(ForgeRegistries.ENCHANTMENTS.getValue(key), (int) level);

            } catch (Exception ignored) {}
        }
        return map;
    }

    public static void addContainerToContainer(Container added, Container container)
    {
        int size = added.getContainerSize();
        for (int i = 0; i < size; i++)
        {
            ItemStack stack = added.getItem(i);
            if (!stack.isEmpty()) addItemToContainer(stack, container);
        }
    }

    public static void addItemToContainer(ItemStack stack, Container container)
    {
        int size = container.getContainerSize();
        int randomOffset = new Random().nextInt(size);

        for (int i = 0; i < size; i++)
        {
            int index = (i + randomOffset) % size;
            ItemStack existing = container.getItem(index);
            int maxSize = existing.getMaxStackSize();
            @Nullable CompoundTag nbt = existing.getTag();
            @Nullable CompoundTag newNbt = stack.getTag();

            if (existing.isEmpty() || !existing.isStackable()) continue;
            boolean hasNbt = nbt != null || newNbt != null;
            boolean flag = hasNbt ? ItemStack.isSameItemSameTags(stack, existing) : ItemStack.isSameItem(stack, existing);

            if (flag)
            {
                int totalCount = stack.getCount() + existing.getCount();
                if (totalCount <= maxSize)
                {
                    existing.grow(stack.getCount());
                    container.setItem(index, existing);
                    return;
                }
            }
        }

        for (int i = 0; i < size; i++)
        {
            int index = (i + randomOffset) % size;
            ItemStack existing = container.getItem(index);
            if (existing.isEmpty())
            {
                container.setItem(index, stack.copy());
                return;
            }
        }
    }

    public static MutableComponent deserializeText(JsonObject data) { return Component.Serializer.fromJson(data); }

    public static JsonObject serializeText(Component text)  { return Component.Serializer.toJsonTree(text).getAsJsonObject(); }

    public static JsonObject serializeItem(ItemStack stack)
    {
        JsonObject data = new JsonObject();

        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null) key = new ResourceLocation("air");
        int count = stack.getCount();

        JsonHelper.write(data, "item", key);
        JsonHelper.write(data, "count", count);

        if (stack.hasTag())
        {
            CompoundTag nbt = stack.getOrCreateTag();
            JsonObject jsonObject = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, nbt).getAsJsonObject();
            JsonHelper.write(data, "nbt", jsonObject);
        }

        return data;
    }

}
