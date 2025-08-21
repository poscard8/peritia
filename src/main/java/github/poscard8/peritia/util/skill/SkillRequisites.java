package github.poscard8.peritia.util.skill;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import github.poscard8.peritia.config.PeritiaServerConfig;
import github.poscard8.peritia.skill.Skill;
import github.poscard8.peritia.skill.data.SkillData;
import github.poscard8.peritia.util.serialization.ArraySerializable;
import github.poscard8.peritia.util.text.PeritiaTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.UnaryOperator;

public class SkillRequisites implements Iterable<SkillRequisite>, ArraySerializable<SkillRequisites>
{
    protected final List<SkillRequisite> requisites = new ArrayList<>();

    public SkillRequisites() {}

    public static SkillRequisites empty() { return new SkillRequisites(); }

    public static SkillRequisites tryLoad(JsonArray data) { return empty().loadWithFallback(data); }

    public List<SkillRequisite> requisites() { return requisites; }

    public void tryAdd(SkillRequisite requisite)
    {
        for (SkillRequisite existing : requisites())
        {
            if (existing.makesOtherRedundant(requisite)) return;
        }
        requisites().removeIf(requisite::makesOtherRedundant);
        requisites().add(requisite);
    }

    public MilestoneStatus milestoneStatus(Skill skill, int level)
    {
        switch (requisites().size())
        {
            case 0 -> { return MilestoneStatus.NONE; }
            case 1 ->
            {
                SkillRequisite requisite = requisites().get(0);
                if (requisite.level() != level) return MilestoneStatus.NONE;

                switch (requisite.type())
                {
                    case ANY -> { return MilestoneStatus.OPTIONAL; }
                    case ALL -> { return MilestoneStatus.PARTIALLY; }
                    default -> { return skill.key().equals(requisite.skillKey()) ? MilestoneStatus.REQUIRED : MilestoneStatus.NONE; }
                }
            }
            default ->
            {
                for (SkillRequisite requisite : requisites())
                {
                    if (requisite.level() == level)
                    {
                        if (requisite.type() != SkillRequisite.Type.SINGLE || skill.key().equals(requisite.skillKey())) return MilestoneStatus.PARTIALLY;
                    }
                }
                return MilestoneStatus.NONE;
            }
        }
    }

    @Override
    @NotNull
    public Iterator<SkillRequisite> iterator() { return requisites().iterator(); }

    public boolean testForRecipe(SkillData skillData)
    {
        boolean keep = PeritiaServerConfig.KEEP_SKILL_RECIPES.get();
        return test(skillData, keep);
    }

    public boolean testForLockedItem(SkillData skillData)
    {
        boolean keep = PeritiaServerConfig.KEEP_UNLOCKED_ITEMS.get();
        return test(skillData, keep);
    }

    public boolean test(SkillData skillData, boolean keep)
    {
        for (SkillRequisite requisite : this)
        {
            if (!requisite.test(skillData, keep)) return false;
        }
        return true;
    }

    @Override
    public SkillRequisites fallback() { return empty(); }

    @Override
    public SkillRequisites load(JsonArray data)
    {
        for (JsonElement element : data)
        {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString())
            {
                String string = element.getAsString();
                SkillRequisite requisite = SkillRequisite.tryLoad(string);
                if (requisite.isValid()) tryAdd(requisite);
            }
        }
        return this;
    }

    @Override
    public JsonArray save()
    {
        JsonArray data = new JsonArray();
        for (SkillRequisite requisite : this) data.add(requisite.save());

        return data;
    }


    public enum MilestoneStatus
    {
        REQUIRED(PeritiaTexts.empty()),
        OPTIONAL(PeritiaTexts.optional()),
        PARTIALLY(PeritiaTexts.partially()),
        NONE(PeritiaTexts.empty(), false);

        private final Component text;
        private final boolean valid;

        MilestoneStatus(Component text) { this(text, true); }

        MilestoneStatus(Component text, boolean valid)
        {
            this.text = text;
            this.valid = valid;
        }

        public Component getText(ChatFormatting... formatting) { return text.copy().withStyle(formatting); }

        public Component getText(UnaryOperator<Style> styleModifier) { return text.copy().withStyle(styleModifier); }

        public boolean isValid() { return valid; }

    }

}
