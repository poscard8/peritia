package github.poscard8.peritia.network;

import github.poscard8.peritia.network.packet.clientbound.*;
import github.poscard8.peritia.network.packet.serverbound.*;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;

import static github.poscard8.peritia.Peritia.asResource;

public class PeritiaNetworkHandler
{
    static final String PROTOCOL_VERSION = "1";
    static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(

            asResource("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    static int COUNTER = 0;

    public static SimpleChannel getChannel() { return CHANNEL; }

    public static <P extends Packet> void sendToServer(P packet) { getChannel().sendToServer(packet); }

    public static <P extends Packet> void sendToClient(P packet, ServerPlayer player) { sendToClient(packet, player.connection.connection); }

    public static <P extends Packet> void sendToClient(P packet, Connection connection) { getChannel().sendTo(packet, connection, NetworkDirection.PLAY_TO_CLIENT); }

    public static <P extends Packet> void registerPacket(Class<P> clazz, Function<FriendlyByteBuf, P> decoder)
    {
        CHANNEL.registerMessage(COUNTER, clazz, Packet::encode, decoder, Packet::consumeWrapper);
        COUNTER++;
    }

    public static void registerPackets()
    {
        registerPacket(PeritiaResourcesPacket.class, PeritiaResourcesPacket::new);
        registerPacket(GameContextPacket.class, GameContextPacket::new);
        registerPacket(LookContextPacket.class, LookContextPacket::new);
        registerPacket(InstructionsPacket.class, InstructionsPacket::new);
        registerPacket(SkillDataPacket.class, SkillDataPacket::new);
        registerPacket(XpSourceDataPacket.class, XpSourceDataPacket::new);
        registerPacket(AscensionSystemPacket.class, AscensionSystemPacket::new);
        registerPacket(XpGainPacket.class, XpGainPacket::new);
        registerPacket(LevelUpPacket.class, LevelUpPacket::new);
        registerPacket(LevelUpReadyPacket.class, LevelUpReadyPacket::new);
        registerPacket(EncyclopediaUpdatePacket.class, EncyclopediaUpdatePacket::new);
        registerPacket(EncyclopediaCompletePacket.class, EncyclopediaCompletePacket::new);
        registerPacket(ChestLuckPacket.class, ChestLuckPacket::new);
        registerPacket(RefreshScreenPacket.class, RefreshScreenPacket::new);
        registerPacket(AttributePacket.class, AttributePacket::new);

        registerPacket(OpenMenuPacket.class, OpenMenuPacket::new);
        registerPacket(AscendPacket.class, AscendPacket::new);
        registerPacket(ClaimAllPacket.class, ClaimAllPacket::new);
        registerPacket(ClaimRewardsPacket.class, ClaimRewardsPacket::new);
        registerPacket(PayRestrictionsPacket.class, PayRestrictionsPacket::new);
        registerPacket(CraftPacket.class, CraftPacket::new);
        registerPacket(UseGrindstonePacket.class, UseGrindstonePacket::new);
        registerPacket(BrewPotionPacket.class, BrewPotionPacket::new);
    }



}
