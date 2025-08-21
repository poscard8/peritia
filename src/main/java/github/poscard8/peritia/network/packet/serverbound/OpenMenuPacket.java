package github.poscard8.peritia.network.packet.serverbound;

import github.poscard8.peritia.client.menu.PeritiaMainMenu;
import github.poscard8.peritia.client.menu.ProfileMenu;
import github.poscard8.peritia.client.menu.SkillMenu;
import github.poscard8.peritia.client.menu.SkillRecipeMenu;
import github.poscard8.peritia.network.Packet;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.data.ServerSkillData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class OpenMenuPacket implements Packet
{
    public static final byte MAIN_MENU_ID = 0;
    public static final byte SKILL_MENU_ID = 1;
    public static final byte SKILL_RECIPE_MENU_ID = 2;
    public static final byte PROFILE_MENU_ID = 3;

    public byte id;
    public byte data;

    public OpenMenuPacket(byte id) { this(id, (byte) 0); }

    public OpenMenuPacket(byte id, byte data)
    {
        this.id = id;
        this.data = data;
    }

    public OpenMenuPacket(FriendlyByteBuf buffer)
    {
        this.id = buffer.readByte();
        this.data = buffer.readByte();
    }

    public static OpenMenuPacket mainMenu() { return mainMenu(true); }

    public static OpenMenuPacket mainMenu(boolean fade)
    {
        byte data = fade ? (byte) 1 : 0;
        return new OpenMenuPacket(MAIN_MENU_ID, data);
    }

    public static OpenMenuPacket skillMenu(Skill skill) { return new OpenMenuPacket(SKILL_MENU_ID, (byte) skill.positionIndex()); }

    public static OpenMenuPacket skillRecipeMenu() { return new OpenMenuPacket(SKILL_RECIPE_MENU_ID); }

    public static OpenMenuPacket profileMenu() { return new OpenMenuPacket(PROFILE_MENU_ID); }

    @Override
    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeByte(id);
        buffer.writeByte(data);
    }

    @Override
    public Result consume(NetworkEvent.Context context)
    {
        ServerPlayer player = context.getSender();
        if (player == null) return Result.PASS;

        switch (id)
        {
            case MAIN_MENU_ID -> player.openMenu(PeritiaMainMenu.provider(data));
            case SKILL_MENU_ID -> player.openMenu(SkillMenu.provider(data));
            case SKILL_RECIPE_MENU_ID -> player.openMenu(SkillRecipeMenu.provider());
            case PROFILE_MENU_ID -> player.openMenu(ProfileMenu.provider(player));
            default -> {}
        }
        ServerSkillData.of(player).update();
        return Result.SUCCESS;
    }

}
