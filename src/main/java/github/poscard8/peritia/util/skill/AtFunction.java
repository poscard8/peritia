package github.poscard8.peritia.util.skill;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.poscard8.peritia.util.serialization.ElementSerializable;
import github.poscard8.peritia.util.serialization.JsonHelper;
import net.minecraft.util.Mth;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public abstract class AtFunction implements Predicate<Integer>, ElementSerializable<AtFunction>
{
    static final String ARROW_STRING = "->";

    public static AtFunction empty() { return List.empty(); }

    public static AtFunction tryLoad(JsonElement data)
    {
        if (data.isJsonPrimitive())
        {
            JsonPrimitive primitive = data.getAsJsonPrimitive();
            if (primitive.isNumber())
            {
                return Single.tryLoad(data);
            }
            else if (primitive.isString())
            {
                String string = primitive.getAsString();

                if (string.startsWith(ARROW_STRING)) return To.tryLoad(data);
                if (string.endsWith(ARROW_STRING)) return From.tryLoad(data);
                return Range.tryLoad(data);
            }
            return empty();
        }
        else if (data.isJsonObject()) return Periodic.tryLoad(data);
        else if (data.isJsonArray()) return List.tryLoad(data);

        return empty();
    }

    public int count(int fromInclusive, int toExclusive)
    {
        int counter = 0;

        for (int i = fromInclusive; i < toExclusive; i++)
        {
            if (test(i)) counter++;
        }
        return counter;
    }

    public int countInclusive(int from, int to) { return count(from, to + 1); }

    public int countFrom0(int toInclusive) { return count(0, toInclusive + 1); }

    public int countFrom1(int toInclusive) { return count(1, toInclusive + 1); }

    @Override
    public AtFunction fallback() { return empty(); }


    public static class Periodic extends AtFunction
    {
        protected int period;
        protected int offset;
        protected int startFrom;

        public Periodic(int period, int offset, int startFrom)
        {
            this.period = Math.max(1, period);
            this.offset = Mth.clamp(offset, -(period - 1), period - 1);
            this.startFrom = startFrom;
        }

        public static Periodic empty() { return new Periodic(1, 0, 0); }

        public static Periodic tryLoad(JsonElement data)
        {
            return empty().loadWithFallback(data) instanceof Periodic periodicAtFunction ? periodicAtFunction : empty();
        }

        public int period() { return period; }

        public int offset() { return offset; }

        @Override
        public boolean test(Integer integer) { return integer >= startFrom && ((integer - offset) % period == 0); }

        @Override
        public AtFunction load(JsonElement data)
        {
            JsonObject jsonObject = data.getAsJsonObject();
            int period = JsonHelper.readInt(jsonObject, "period", this.period);
            int offset = JsonHelper.readInt(jsonObject, "offset", this.offset);
            int startFrom = JsonHelper.readInt(jsonObject, "startFrom", this.startFrom);

            return new Periodic(period, offset, startFrom);
        }

        @Override
        public JsonElement save()
        {
            JsonObject data = new JsonObject();
            JsonHelper.write(data, "period", period);
            JsonHelper.write(data, "offset", offset);
            JsonHelper.write(data, "startFrom", startFrom);

            return data;
        }
    }

    public static class Range extends AtFunction
    {
        protected int from;
        protected int to;

        public Range(int from, int to)
        {
            this.from = Math.max(0, from);
            this.to = Math.max(from, to);
        }

        public static Range empty() { return new Range(Integer.MIN_VALUE, Integer.MAX_VALUE); }

        public static Range tryLoad(JsonElement data)
        {
            return empty().loadWithFallback(data) instanceof Range rangeAtFunction ? rangeAtFunction : empty();
        }

        public int from() { return from; }

        public int to() { return to; }

        @Override
        public boolean test(Integer integer) { return integer >= from() && integer <= to(); }

        @Override
        public AtFunction load(JsonElement data)
        {
            String string = data.getAsString();
            if (!string.contains(ARROW_STRING)) throw new RuntimeException("Invalid string for at function: " + string);

            String[] split = string.split(ARROW_STRING);
            int from = Integer.parseInt(split[0]);
            int to = Integer.parseInt(split[1]);

            return new Range(from, to);
        }

        @Override
        public JsonPrimitive save() { return new JsonPrimitive(from + ARROW_STRING + to); }

    }

    /**
     * Naming it {@code List} feels more convenient.
     */
    public static class List extends AtFunction
    {
        protected Set<Integer> set;

        public List(Set<Integer> set) { this.set = set; }

        public static List empty() { return new List(new HashSet<>()); }

        public static List tryLoad(JsonElement data)
        {
            return empty().loadWithFallback(data) instanceof List listAtFunction ? listAtFunction : empty();
        }

        public Set<Integer> numbers() { return set; }

        public java.util.List<Integer> sortedNumbers() { return numbers().stream().sorted(Integer::compareTo).toList(); }

        @Override
        public boolean test(Integer integer) { return numbers().contains(integer); }

        @Override
        public AtFunction load(JsonElement data)
        {
            JsonArray jsonArray = data.getAsJsonArray();
            for (JsonElement element : jsonArray)
            {
                if (element.isJsonPrimitive()) set.add(element.getAsInt());
            }
            return this;
        }

        @Override
        public JsonElement save()
        {
            JsonArray data = new JsonArray();
            for (Integer integer : set) data.add(integer);

            return data;
        }
    }

    public static class From extends AtFunction
    {
        protected int from;

        public From(int from) { this.from = from; }

        public static From empty() { return new From(Integer.MIN_VALUE); }

        public static From tryLoad(JsonElement data)
        {
            return empty().loadWithFallback(data) instanceof From fromAtFunction ? fromAtFunction : empty();
        }

        public int number() { return from; }

        @Override
        public boolean test(Integer integer) { return integer >= number(); }

        @Override
        public AtFunction load(JsonElement data)
        {
            String string = data.getAsString();
            if (!string.endsWith(ARROW_STRING)) throw new RuntimeException("Invalid string for at function: " + string);

            String subString = string.substring(0, string.length() - 2);
            this.from = Integer.parseInt(subString);
            return this;
        }

        @Override
        public JsonElement save() { return new JsonPrimitive(from + ARROW_STRING); }

    }

    public static class To extends AtFunction
    {
        protected int to;

        public To(int to) { this.to = to; }

        public static To empty() { return new To(Integer.MAX_VALUE); }

        public static To tryLoad(JsonElement data)
        {
            return empty().loadWithFallback(data) instanceof To toAtFunction ? toAtFunction : empty();
        }

        public int number() { return to; }

        @Override
        public boolean test(Integer integer) { return integer <= number(); }

        @Override
        public AtFunction load(JsonElement data)
        {
            String string = data.getAsString();
            if (!string.startsWith(ARROW_STRING)) throw new RuntimeException("Invalid string for at function: " + string);

            String subString = string.substring(2);
            this.to = Integer.parseInt(subString);
            return this;
        }

        @Override
        public JsonElement save() { return new JsonPrimitive(ARROW_STRING + to); }

    }

    public static class Single extends AtFunction
    {
        protected int single;

        public Single(int single) { this.single = Math.max(0, single); }

        public static Single empty() { return new Single(1); }

        public static Single tryLoad(JsonElement data)
        {
            return empty().loadWithFallback(data) instanceof Single singleAtFunction ? singleAtFunction : empty();
        }

        public int number() { return single; }

        @Override
        public boolean test(Integer integer) { return integer == number(); }

        @Override
        public AtFunction load(JsonElement data)
        {
            int single = data.getAsInt();
            return new Single(single);
        }

        @Override
        public JsonElement save() { return new JsonPrimitive(single); }
    }

}
