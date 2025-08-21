package github.poscard8.peritia.skill;

import github.poscard8.peritia.util.serialization.StringSerializable;
import github.poscard8.peritia.util.skill.LevelLayoutHelper;

import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public enum LevelLayout implements StringSerializable<LevelLayout>
{
    LINE("line", LevelLayoutHelper::getLineTextureId, LevelLayoutHelper::getLineXOffset, LevelLayoutHelper::getLineYOffset),
    SNAKE("snake", LevelLayoutHelper::getSnakeTextureId, LevelLayoutHelper::getSnakeXOffset, LevelLayoutHelper::getSnakeYOffset),
    STAR("star", LevelLayoutHelper::getStarTextureId, LevelLayoutHelper::getStarXOffset, LevelLayoutHelper::getStarYOffset);

    private final String name;
    private final BinaryOperator<Integer> textureIdFunction;
    private final UnaryOperator<Integer> xOffsetFunction;
    private final UnaryOperator<Integer> yOffsetFunction;

    LevelLayout(String name, BinaryOperator<Integer> textureIdFunction, UnaryOperator<Integer> xOffsetFunction, UnaryOperator<Integer> yOffsetFunction)
    {
        this.name = name;
        this.textureIdFunction = textureIdFunction;
        this.xOffsetFunction = xOffsetFunction;
        this.yOffsetFunction = yOffsetFunction;
    }

    public static LevelLayout empty() { return LevelLayout.SNAKE; }

    public static LevelLayout tryLoad(String data) { return empty().loadWithFallback(data); }

    public String getName() { return name; }

    public int getTextureId(Skill skill, int level) { return textureIdFunction.apply(skill.maxLevel(), level); }

    public int getXTexStart(Skill skill, int level) { return getTextureId(skill, level) * LevelLayoutHelper.BUTTON_SIZE; }

    public int getYTexStart(SkillInstance instance, int level) { return instance.milestoneStatus(level).yTexOffset(); }

    public int getXOffset(int level) { return xOffsetFunction.apply(level); }

    public int getYOffset(int level) { return yOffsetFunction.apply(level); }

    @Override
    public LevelLayout fallback() { return empty(); }

    @Override
    public LevelLayout load(String data)
    {
        return Arrays.stream(values()).filter(levelLayout -> levelLayout.getName().equals(data)).findFirst().orElse(fallback());
    }

    @Override
    public String save() { return getName(); }

}
