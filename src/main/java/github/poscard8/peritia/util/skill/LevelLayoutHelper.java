package github.poscard8.peritia.util.skill;

@SuppressWarnings("unused")
public class LevelLayoutHelper
{
    public static final int BUTTON_SIZE = 22;
    public static final int EDGE_THICKNESS = 4;

    static final int[] SNAKE_TEXTURE_IDS = new int[]{5, 11, 7, 7, 9, 5, 10, 6, 6, 8};
    static final int[] SNAKE_TEXTURE_IDS_END = new int[]{4, 4, 3, 3, 3, 4, 4, 0, 0, 0};
    static final int[] SNAKE_X_OFFSETS = new int[]{-22, 0, 0, 0, 0, 22, 44, 44, 44, 44};
    static final int[] SNAKE_Y_OFFSETS = new int[]{0, 0, 22, 44, 66, 66, 66, 44, 22, 0};
    static final int[] STAR_TEXTURE_IDS = new int[]{4, 2, 12, 0, 3, 12, 12, 4, 10, 13};
    static final int[] STAR_X_OFFSETS = new int[]{-22, 0, 22, 22, 22, 44, 66, 88, 88, 88};
    static final int[] STAR_Y_OFFSETS = new int[]{44, 22, 22, 0, 44, 22, 44, 44, 22, 66};

    public static int getLineTextureId(int maxLevel, int level) { return level == 1 ? 2 : level == maxLevel ? 4 : 5; }

    public static int getLineXOffset(int level) { return (level - 1) * BUTTON_SIZE + EDGE_THICKNESS; }

    public static int getLineYOffset(int level) { return 37; }

    public static int getSnakeTextureId(int maxLevel, int level)
    {
        if (level == 1) return 1;
        int remainder = level % 10;
        return level == maxLevel ? SNAKE_TEXTURE_IDS_END[remainder] : SNAKE_TEXTURE_IDS[remainder];
    }

    public static int getSnakeXOffset(int level)
    {
        int startOffset = (level / 10) * BUTTON_SIZE * 4;
        return startOffset + SNAKE_X_OFFSETS[level % 10] + EDGE_THICKNESS;
    }

    public static int getSnakeYOffset(int level) { return SNAKE_Y_OFFSETS[level % 10] + EDGE_THICKNESS; }

    public static int getStarTextureId(int maxLevel, int level)
    {
        int mod = level % 5;
        int maxMod = maxLevel % 5;
        if (mod == 1 && level == maxLevel) return 14;

        boolean isLastStar = (maxLevel - 1) / 5 == (level - 1) / 5;
        int index = isLastStar && mod == 2 ? maxMod + 5 : mod;

        return STAR_TEXTURE_IDS[index];
    }

    public static int getStarXOffset(int level)
    {
        int startOffset = (level / 10) * BUTTON_SIZE * 6;
        return startOffset + STAR_X_OFFSETS[level % 10] + EDGE_THICKNESS;
    }

    public static int getStarYOffset(int level) { return STAR_Y_OFFSETS[level % 10] + EDGE_THICKNESS; }


}
