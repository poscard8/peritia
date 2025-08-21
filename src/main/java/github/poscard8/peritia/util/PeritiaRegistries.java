package github.poscard8.peritia.util;

import github.poscard8.peritia.reward.RewardType;
import github.poscard8.peritia.util.serialization.Loadable;
import github.poscard8.peritia.xpsource.XpSourceType;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

import static github.poscard8.peritia.Peritia.asResource;

public class PeritiaRegistries
{
    public static IForgeRegistry<RewardType<?>> rewardTypes() { return REWARD_TYPES_SUPPLIER.get(); }

    public static IForgeRegistry<XpSourceType<?>> xpSourceTypes() { return XP_SOURCE_TYPES_SUPPLIER.get(); }

    public static final RegistryBuilder<RewardType<?>> REWARD_TYPES_BUILDER = new RegistryBuilder<RewardType<?>>()
            .setName(asResource("reward_type"))
            .add((owner, stage, id, key, type, oldType) -> type.assignKey(key.location()))
            .setDefaultKey(Loadable.EMPTY_KEY);

    public static Supplier<IForgeRegistry<RewardType<?>>> REWARD_TYPES_SUPPLIER = null;

    public static final RegistryBuilder<XpSourceType<?>> XP_SOURCE_TYPES_BUILDER = new RegistryBuilder<XpSourceType<?>>()
            .setName(asResource("xp_source_type"))
            .add((owner, stage, id, key, type, oldType) -> type.assignKey(key.location()))
            .setDefaultKey(Loadable.EMPTY_KEY);

    public static Supplier<IForgeRegistry<XpSourceType<?>>> XP_SOURCE_TYPES_SUPPLIER = null;


}
