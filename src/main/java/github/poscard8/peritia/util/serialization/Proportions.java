package github.poscard8.peritia.util.serialization;

public record Proportions(int x, int y) implements StringSerializable<Proportions>
{
    public static Proportions empty() { return new Proportions(0, 0); }

    public static Proportions tryLoad(String data) { return empty().loadWithFallback(data); }

    @Override
    public Proportions fallback() { return empty(); }

    @Override
    public Proportions load(String data)
    {
        String[] split = data.split(",");

        switch (split.length)
        {
            case 1 ->
            {
                int x = Integer.parseInt(split[0]);
                return new Proportions(x, 0);
            }
            case 2 ->
            {
                int x = Integer.parseInt(split[0]);
                int y = Integer.parseInt(split[1]);
                return new Proportions(x, y);
            }
        }
        return this;
    }

    @Override
    public String save() { return x + "," + y; }

}
