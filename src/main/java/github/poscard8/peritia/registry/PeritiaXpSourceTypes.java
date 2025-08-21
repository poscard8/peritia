package github.poscard8.peritia.registry;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.xpsource.XpSourceType;
import github.poscard8.peritia.xpsource.type.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class PeritiaXpSourceTypes
{
    public static final DeferredRegister<XpSourceType<?>> ALL = DeferredRegister.create(Peritia.asResource("xp_source_type"), Peritia.ID);

    public static final RegistryObject<XpSourceType<EmptyXpSource>> EMPTY = ALL.register("empty", () -> new XpSourceType<>(EmptyXpSource.class, EmptyXpSource::tryLoad));
    public static final RegistryObject<XpSourceType<LoginXpSource>> LOGIN = ALL.register("login", () -> new XpSourceType<>(LoginXpSource.class, LoginXpSource::tryLoad));
    public static final RegistryObject<XpSourceType<SocialXpSource>> SOCIAL = ALL.register("social", () -> new XpSourceType<>(SocialXpSource.class, SocialXpSource::tryLoad));
    public static final RegistryObject<XpSourceType<BlockXpSource>> BLOCK = ALL.register("block", () -> new XpSourceType<>(BlockXpSource.class, BlockXpSource::tryLoad));
    public static final RegistryObject<XpSourceType<EntityXpSource>> ENTITY = ALL.register("entity", () -> new XpSourceType<>(EntityXpSource.class, EntityXpSource::tryLoad));
    public static final RegistryObject<XpSourceType<StructureXpSource>> STRUCTURE = ALL.register("structure", () -> new XpSourceType<>(StructureXpSource.class, StructureXpSource::tryLoad));
    public static final RegistryObject<XpSourceType<ChestXpSource>> CHEST = ALL.register("chest", () -> new XpSourceType<>(ChestXpSource.class, ChestXpSource::tryLoad));
    public static final RegistryObject<XpSourceType<RecipeXpSource>> RECIPE = ALL.register("recipe", () -> new XpSourceType<>(RecipeXpSource.class, RecipeXpSource::tryLoad));
    public static final RegistryObject<XpSourceType<TradeXpSource>> TRADE = ALL.register("trade", () -> new XpSourceType<>(TradeXpSource.class, TradeXpSource::tryLoad));
    public static final RegistryObject<XpSourceType<AdvancementXpSource>> ADVANCEMENT = ALL.register("advancement", () -> new XpSourceType<>(AdvancementXpSource.class, AdvancementXpSource::tryLoad));
    public static final RegistryObject<XpSourceType<EnchantmentXpSource>> ENCHANTMENT = ALL.register("enchantment", () -> new XpSourceType<>(EnchantmentXpSource.class, EnchantmentXpSource::tryLoad));
    public static final RegistryObject<XpSourceType<PotionXpSource>> POTION = ALL.register("potion", () -> new XpSourceType<>(PotionXpSource.class, PotionXpSource::tryLoad));
    public static final RegistryObject<XpSourceType<ConsumeXpSource>> CONSUME = ALL.register("consume", () -> new XpSourceType<>(ConsumeXpSource.class, ConsumeXpSource::tryLoad));
    public static final RegistryObject<XpSourceType<FishXpSource>> FISH = ALL.register("fish", () -> new XpSourceType<>(FishXpSource.class, FishXpSource::tryLoad));
    public static final RegistryObject<XpSourceType<GrindstoneXpSource>> GRINDSTONE = ALL.register("grindstone", () -> new XpSourceType<>(GrindstoneXpSource.class, GrindstoneXpSource::tryLoad));
    public static final RegistryObject<XpSourceType<AnvilXpSource>> ANVIL = ALL.register("anvil", () -> new XpSourceType<>(AnvilXpSource.class, AnvilXpSource::tryLoad));


    public static void register(IEventBus bus) { ALL.register(bus); }

}
