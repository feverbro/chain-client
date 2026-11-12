package dev.chain.setting;

import com.google.gson.JsonObject;

public class NumberSetting extends Setting {
    public final double min;
    public final double max;
    public final double step;
    private double value;

    public NumberSetting(String name, double value, double min, double max, double step) {
        super(name);
        this.min = min;
        this.max = max;
        this.step = step;
        set(value);
    }

    public double get() {
        return value;
    }

    public float getFloat() {
        return (float) value;
    }

    public int getInt() {
        return (int) Math.round(value);
    }

    public void set(double v) {
        if (step > 0) {
            v = Math.round(v / step) * step;
        }
        value = Math.max(min, Math.min(max, v));
    }

    /** Значение для отображения: целое без точки, дробное с одним-двумя знаками. */
    public String display() {
        if (step >= 1.0) {
            return Integer.toString((int) Math.round(value));
        }
        return step >= 0.1 ? String.format("%.1f", value) : String.format("%.2f", value);
    }

    @Override
    public void save(JsonObject out) {
        out.addProperty(name, value);
    }

    @Override
    public void load(JsonObject in) {
        if (in.has(name)) {
            try {
                set(in.get(name).getAsDouble());
            } catch (Exception ignored) {
            }
        }
    }
}
