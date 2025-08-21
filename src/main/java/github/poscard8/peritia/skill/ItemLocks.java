package github.poscard8.peritia.skill;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import github.poscard8.peritia.util.serialization.ArraySerializable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ItemLocks implements SkillAssignable, Iterable<ItemLock>, ArraySerializable<ItemLocks>
{
    protected final List<ItemLock> locks = new ArrayList<>();

    public ItemLocks() {}

    public static ItemLocks empty() { return new ItemLocks(); }

    public static ItemLocks tryLoad(JsonArray data) { return empty().loadWithFallback(data); }

    public List<ItemLock> locks() { return locks; }

    @Override
    public void assignSkill(Skill skill)
    {
        locks().forEach(lock -> lock.assignSkill(skill));
        locks().removeIf(ItemLock::isInvalid);
    }

    public void validateConditions()
    {
        locks().removeIf(ItemLock::doesNotMeetConditions);
    }

    @Override
    @NotNull
    public Iterator<ItemLock> iterator() { return locks().iterator(); }

    @Override
    public ItemLocks fallback() { return empty(); }

    @Override
    public ItemLocks load(JsonArray data)
    {
        for (JsonElement element : data)
        {
            ItemLock lock = ItemLock.tryLoad(element);
            locks.add(lock);
        }
        return this;
    }

    @Override
    public JsonArray save()
    {
        JsonArray data = new JsonArray();
        for (ItemLock lock : this) data.add(lock.save());

        return data;
    }

}
