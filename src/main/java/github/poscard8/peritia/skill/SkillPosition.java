package github.poscard8.peritia.skill;

import github.poscard8.peritia.util.serialization.StringSerializable;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class SkillPosition implements Comparable<SkillPosition>, StringSerializable<SkillPosition>
{
    public static final int MIN_ROW = 0;
    public static final int MAX_ROW = 2;
    public static final int MIN_COLUMN = 0;
    public static final int MAX_COLUMN = 8;

    static final int COLUMN_OFFSET = 4;

    protected int row;
    protected int column;

    public SkillPosition(int row, int column)
    {
        this.row = Mth.clamp(row, MIN_ROW, MAX_ROW);
        this.column = Mth.clamp(column, MIN_COLUMN, MAX_COLUMN);
    }

    public static SkillPosition empty() { return new SkillPosition(1, 4); }

    public static SkillPosition tryLoad(String data) { return empty().loadWithFallback(data); }

    public int row() { return row; }

    public int column() { return column; }

    public int index() { return row() * 9 + column(); }

    @Override
    public int compareTo(@NotNull SkillPosition other) { return index() - other.index(); }

    @Override
    public SkillPosition fallback() { return empty(); }

    @Override
    public SkillPosition load(String data)
    {
        String[] split = data.split(",");

        int newColumn = Integer.parseInt(split[0]) + COLUMN_OFFSET;
        int newRow = 1 - Integer.parseInt(split[1]);

        return new SkillPosition(newRow, newColumn);
    }

    @Override
    public String save()
    {
        int newColumn = column - COLUMN_OFFSET;
        int newRow = 1 - row;

        return String.format("%d,%d", newColumn, newRow);
    }

}
