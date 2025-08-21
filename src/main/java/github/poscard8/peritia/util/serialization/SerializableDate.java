package github.poscard8.peritia.util.serialization;

import java.util.Calendar;
import java.util.TimeZone;

public class SerializableDate implements StringSerializable<SerializableDate>
{
    protected int day;
    protected int month;
    protected int year;
    protected int week;
    protected int hour;
    protected int minute;
    protected int second;

    public SerializableDate() { this(calendar()); }

    public SerializableDate(Calendar calendar)
    {
        this.day = calendar.get(Calendar.DAY_OF_MONTH);
        this.month = calendar.get(Calendar.MONTH);
        this.year = calendar.get(Calendar.YEAR);
        this.week = calendar.get(Calendar.WEEK_OF_YEAR);
        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
        this.minute = calendar.get(Calendar.MINUTE);
        this.second = calendar.get(Calendar.SECOND);
    }

    public static SerializableDate empty() { return new SerializableDate(); }

    public static SerializableDate tryLoad(String data) { return empty().loadWithFallback(data); }

    public static Calendar calendar() { return Calendar.getInstance(TimeZone.getTimeZone("GMT+2")); }

    public void refresh()
    {
        Calendar calendar = calendar();

        this.day = calendar.get(Calendar.DAY_OF_MONTH);
        this.month = calendar.get(Calendar.MONTH);
        this.year = calendar.get(Calendar.YEAR);
        this.week = calendar.get(Calendar.WEEK_OF_YEAR);
        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
        this.minute = calendar.get(Calendar.MINUTE);
        this.second = calendar.get(Calendar.SECOND);
    }

    public Calendar toCalendar()
    {
        Calendar calendar = calendar();
        calendar.set(year, month, day, hour, minute, second);
        return calendar;
    }

    public long offsetFromNow()
    {
        Calendar current = calendar();
        return toCalendar().getTimeInMillis() - current.getTimeInMillis();
    }

    public long offsetFrom(SerializableDate other)
    {
        return toCalendar().getTimeInMillis() - other.toCalendar().getTimeInMillis();
    }

    public SerializableDate nextYear()
    {
        Calendar calendar = toCalendar();
        calendar.set(year + 1, Calendar.JANUARY, 1, 0, 0, 0);

        return new SerializableDate(calendar);
    }

    public SerializableDate nextMonth()
    {
        Calendar calendar = toCalendar();
        calendar.set(year, month, 1, 0, 0, 0);
        calendar.add(Calendar.MONTH, 1);

        return new SerializableDate(calendar);
    }

    public SerializableDate nextWeek()
    {
        Calendar calendar = toCalendar();
        calendar.set(year, month, day, 0, 0, 0);

        calendar.add(Calendar.WEEK_OF_YEAR, 1);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        return new SerializableDate(calendar);
    }

    public SerializableDate nextDay()
    {
        Calendar calendar = toCalendar();
        calendar.set(year, month, day, 0, 0, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        return new SerializableDate(calendar);
    }

    public SerializableDate nextHour()
    {
        Calendar calendar = toCalendar();
        calendar.set(year, month, day, hour, 0, 0);
        calendar.add(Calendar.HOUR_OF_DAY, 1);

        return new SerializableDate(calendar);
    }

    public SerializableDate nextMinute()
    {
        Calendar calendar = toCalendar();
        calendar.set(year, month, day, hour, minute, 0);
        calendar.add(Calendar.MINUTE, 1);

        return new SerializableDate(calendar);
    }

    public SerializableDate add(int field, int amount)
    {
        Calendar calendar = toCalendar();
        calendar.add(field, amount);
        return new SerializableDate(calendar);
    }

    public int day() { return day; }

    public int month() { return month; }

    public int year() { return year; }

    public int week() { return week; }

    public int hour() { return hour; }

    public int minute() { return minute; }

    public int second() { return second; }

    public boolean isSameYear(SerializableDate other) { return year() == other.year(); }

    public boolean isSameMonth(SerializableDate other) { return isSameYear(other) && month() == other.month(); }

    public boolean isSameWeek(SerializableDate other) { return isSameYear(other) && week() == other.week(); }

    public boolean isSameDay(SerializableDate other) { return isSameMonth(other) && day() == other.day(); }

    public boolean isSameHour(SerializableDate other) { return isSameDay(other) && hour() == other.hour(); }

    public boolean isSameMinute(SerializableDate other) { return isSameHour(other) && minute() == other.minute(); }

    public boolean isSameSecond(SerializableDate other) { return isSameMinute(other) && second() == other.second(); }

    @Override
    public SerializableDate fallback() { return empty(); }

    @Override
    public SerializableDate load(String data)
    {
        String[] split = data.split("-");

        this.day = Integer.parseInt(split[0]);
        this.month = Integer.parseInt(split[1]) - 1;
        this.year = Integer.parseInt(split[2]);
        this.week = Integer.parseInt(split[3]);
        this.hour = Integer.parseInt(split[4]);
        this.minute = Integer.parseInt(split[5]);
        this.second = Integer.parseInt(split[6]);

        return this;
    }

    @Override
    public String save() { return String.format("%d-%d-%d-%d-%d-%d-%d", day, month + 1, year, week, hour, minute, second); }

    @Override
    public boolean equals(Object object) { return object instanceof SerializableDate date && isSameSecond(date); }

    @Override
    public String toString()
    {
        return String.format("%d-%d-%d (Week %d) %d:%d:%d", day, month, year, week, hour, minute, second);
    }

}
