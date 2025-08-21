package github.poscard8.peritia.util.text.insertion;

import github.poscard8.peritia.network.PeritiaClientHandler;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.serverbound.ClaimRewardsPacket;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.SkillInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ClaimRewardInsertion(Skill skill, int oldLevel, int newLevel) implements Insertion
{
    public static final String PREFIX = "claimReward";

    public static ClaimRewardInsertion empty() { return new ClaimRewardInsertion(Skill.empty(), 0, 1); }

    public static Insertion tryLoad(String data) { return empty().loadWithFallback(data); }

    @Override
    public void accept(PeritiaClientHandler clientHandler)
    {
        SkillInstance instance = clientHandler.skillData().getSkill(skill());
        boolean canClaim = false;

        for (int lvl = oldLevel() + 1; lvl <= newLevel(); lvl++)
        {
            if (instance.canClaimReward(lvl))
            {
                canClaim = true;
                break;
            }
        }

        if (canClaim) clientHandler.playLocalSound(SoundEvents.ITEM_PICKUP, 0.8F, 1);
        PeritiaNetworkHandler.sendToServer(new ClaimRewardsPacket(skill(), oldLevel(), newLevel()));
    }

    @Override
    public Insertion load(String data)
    {
        String[] split = data.split(",");
        ResourceLocation key = ResourceLocation.tryParse(split[1]);
        if (key == null) throw new RuntimeException(String.format("Invalid skill key: %s", split[1]));

        Skill skill = Skill.byKey(key);
        int oldLevel = Integer.parseInt(split[2]);
        int newLevel = Integer.parseInt(split[3]);

        return new ClaimRewardInsertion(skill, oldLevel, newLevel);
    }

    @Override
    public String save() { return String.format("%s,%s,%d,%d", PREFIX, skill.stringKey(), oldLevel, newLevel); }

    @Override
    public String toString() { return save(); }


}
