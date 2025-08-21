package github.poscard8.peritia.util.text.insertion;

import github.poscard8.peritia.network.PeritiaClientHandler;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.serverbound.OpenMenuPacket;
import github.poscard8.peritia.skill.Skill;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record OpenSkillMenuInsertion(Skill skill) implements Insertion
{
    public static final String PREFIX = "openSkillMenu";

    public static OpenSkillMenuInsertion empty() { return new OpenSkillMenuInsertion(Skill.empty()); }

    public static Insertion tryLoad(String data) { return empty().loadWithFallback(data); }

    @Override
    public void accept(PeritiaClientHandler clientHandler)
    {
        clientHandler.playLocalSound(SoundEvents.UI_BUTTON_CLICK, 0.8F, 1);
        PeritiaNetworkHandler.sendToServer(OpenMenuPacket.skillMenu(skill));
    }

    @Override
    public Insertion load(String data)
    {
        String[] split = data.split(",");
        ResourceLocation key = ResourceLocation.tryParse(split[1]);
        if (key == null) throw new RuntimeException(String.format("Invalid skill key: %s", split[1]));

        Skill skill = Skill.byKey(key);
        return new OpenSkillMenuInsertion(skill);
    }

    @Override
    public String save() { return String.format("%s,%s", PREFIX, skill.stringKey()); }

    @Override
    public String toString() { return save(); }

}
