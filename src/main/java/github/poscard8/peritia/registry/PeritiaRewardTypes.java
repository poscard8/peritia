package github.poscard8.peritia.registry;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.reward.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class PeritiaRewardTypes
{
    public static final DeferredRegister<RewardType<?>> ALL = DeferredRegister.create(Peritia.asResource("reward_type"), Peritia.ID);

    public static final RegistryObject<RewardType<EmptyReward>> EMPTY = ALL.register("empty", () -> new RewardType<>(EmptyReward::tryLoad, 100));
    public static final RegistryObject<RewardType<ItemReward>> ITEM = ALL.register("item", () -> new RewardType<>(ItemReward::tryLoad, 6, "item"));
    public static final RegistryObject<RewardType<GameXpReward>> GAME_XP = ALL.register("game_xp", () -> new RewardType<>(GameXpReward::tryLoad, 8, "xp"));
    public static final RegistryObject<RewardType<TextReward>> TEXT = ALL.register("text", () -> new RewardType<>(TextReward::tryLoad, 90, "text", "translate", "score", "selector", "keybind", "storage"));

    public static void register(IEventBus bus) { ALL.register(bus); }

}
