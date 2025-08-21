package github.poscard8.peritia.registry;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.command.DataXpSourceArgument;
import github.poscard8.peritia.command.SingleSkillArgument;
import github.poscard8.peritia.command.SkillArgument;
import github.poscard8.peritia.command.XpSourceArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("unused")
public class PeritiaArgumentTypes
{
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ALL = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, Peritia.ID);

    public static final RegistryObject<SingletonArgumentInfo<SkillArgument>> SKILL = ALL.register("skill", () -> ArgumentTypeInfos.registerByClass(SkillArgument.class, SingletonArgumentInfo.contextFree(SkillArgument::of)));
    public static final RegistryObject<SingletonArgumentInfo<SingleSkillArgument>> SINGLE_SKILL = ALL.register("single_skill", () -> ArgumentTypeInfos.registerByClass(SingleSkillArgument.class, SingletonArgumentInfo.contextFree(SingleSkillArgument::of)));
    public static final RegistryObject<SingletonArgumentInfo<XpSourceArgument>> XP_SOURCE = ALL.register("xp_source", () -> ArgumentTypeInfos.registerByClass(XpSourceArgument.class, SingletonArgumentInfo.contextFree(XpSourceArgument::of)));
    public static final RegistryObject<SingletonArgumentInfo<DataXpSourceArgument>> DATA_XP_SOURCE = ALL.register("data_xp_source", () -> ArgumentTypeInfos.registerByClass(DataXpSourceArgument.class, SingletonArgumentInfo.contextFree(DataXpSourceArgument::of)));

    public static void register(IEventBus bus) { ALL.register(bus); }

}
