package github.poscard8.peritia.util.gui.button;

import github.poscard8.peritia.Peritia;
import github.poscard8.peritia.config.PeritiaClientConfig;
import github.poscard8.peritia.network.ClientHandler;
import github.poscard8.peritia.network.PeritiaClientHandler;
import github.poscard8.peritia.network.PeritiaNetworkHandler;
import github.poscard8.peritia.network.packet.serverbound.ClaimRewardsPacket;
import github.poscard8.peritia.network.packet.serverbound.PayRestrictionsPacket;
import github.poscard8.peritia.skill.LevelLayout;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.SkillInstance;
import github.poscard8.peritia.util.gui.SkillTab;
import github.poscard8.peritia.util.gui.TextureStyle;
import github.poscard8.peritia.util.text.PeritiaTexts;
import github.poscard8.peritia.util.text.TextGetter;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class SkillMilestoneButton extends CompactButton
{
    public static final int SIZE = 22;

    protected final SkillTab tab;
    protected final int level;

    public SkillMilestoneButton(Skill skill, int level, SkillTab tab) { this(skill, level, tab, skill.levelLayout()); }

    public SkillMilestoneButton(Skill skill, int level, SkillTab tab, LevelLayout layout) { this(skill, level, tab.x + layout.getXOffset(level), tab.y + layout.getYOffset(level), tab, layout); }

    public SkillMilestoneButton(Skill skill, int level, int x, int y, SkillTab tab, LevelLayout layout)
    {
        super(x, y, SIZE, SIZE, layout.getXTexStart(skill, level), layout.getYTexStart(ClientHandler.getViewingSkillData().getSkill(skill), level), getTextureLocation(skill), 512, 256, onPress(skill, level));
        this.tab = tab;
        this.level = level;

        setSkill(skill);
        setTextGetter(TextGetter.skillMilestone(skill, level));
        setNotifyText(button0 ->
        {
           if (button0 instanceof SkillMilestoneButton button)
           {
               if (!button.skillInstance().isRewardClaimed(level) && status().canClaim()) return PeritiaTexts.glowExclamationMark();
           }
           return null;
        });

        if (level % 5 == 0 || level == skill.maxLevel()) setCountText(level);
    }

    protected static ResourceLocation getTextureLocation(Skill skill)
    {
        @Nullable ResourceLocation textureLocation = skill.textures().milestoneTexture();
        if (PeritiaClientConfig.CUSTOM_SKILL_MENUS.get() && textureLocation != null) return textureLocation;

        TextureStyle textureStyle = PeritiaClientConfig.UI_TEXTURE_STYLE.get();
        return Peritia.asResource(String.format("textures/gui/milestone%s.png", textureStyle.suffix()));
    }

    protected static OnPress onPress(Skill skill, int level)
    {
        return button ->
        {
            PeritiaClientHandler clientHandler = PeritiaClientHandler.getInstance();

            SkillInstance instance = clientHandler.skillData().getSkill(skill);
            if (instance.canClaimReward(level) && instance.level() >= level)
            {
                PeritiaNetworkHandler.sendToServer(new ClaimRewardsPacket(skill, level));
            }
            else if (instance.canPayRestrictions(level))
            {
                PeritiaNetworkHandler.sendToServer(new PayRestrictionsPacket(skill, level));
            }
        };
    }

    @Override
    public boolean shouldRender(int mouseX, int mouseY) { return !Screen.hasControlDown(); }

    @Override
    public boolean renderSkillIcon() { return false; }

    @Override
    public boolean isInside(double mouseX, double mouseY) { return tab.isInside(mouseX, mouseY); }

    public SkillInstance skillInstance() { return viewingSkillData().getSkill(skill()); }

    public SkillInstance.MilestoneStatus status() { return skillInstance().milestoneStatus(level); }

}
