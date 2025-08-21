package github.poscard8.peritia.util.skill;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import github.poscard8.peritia.util.serialization.ArraySerializable;

import java.util.function.UnaryOperator;

public class Polynomial implements UnaryOperator<Double>, ArraySerializable<Polynomial>
{
    protected double[] coefficients;

    public Polynomial() { this.coefficients = new double[0]; }

    public static Polynomial empty() { return new Polynomial(); }

    public static Polynomial tryLoad(JsonArray data) { return empty().loadWithFallback(data); }

    public static Polynomial of(double... coefficients)
    {
        Polynomial polynomial = empty();
        polynomial.coefficients = coefficients;
        return polynomial;
    }

    @Override
    public Double apply(Double x)
    {
        if (x == 0 || coefficients.length == 0) return 0.0D;
        double result = 0;

        for (int i = 0; i < coefficients.length; i++)
        {
            double coefficient = coefficients[i];
            int power = coefficients.length - i - 1;
            result += coefficient * Math.pow(x, power);
        }
        return result;
    }

    public double evaluate(double x) { return apply(x); }

    public int evaluateRounded(double x) { return (int) Math.round(evaluate(x)); }

    @Override
    public Polynomial fallback() { return empty(); }

    @Override
    public Polynomial load(JsonArray data)
    {
        this.coefficients = new double[data.size()];
        for (int i = 0; i < data.size(); i++)
        {
            double coefficient = data.get(i).getAsDouble();
            coefficients[i] = coefficient;
        }
        return this;
    }

    @Override
    public JsonArray save()
    {
        JsonArray data = new JsonArray();
        for (double coefficient : coefficients) data.add(new JsonPrimitive(coefficient));
        return data;
    }

}
