package dev.chain.setting;

import com.google.gson.JsonObject;

public class BoolSetting extends Setting {
    private boolean value;

    public BoolSetting(String name, boolean value) {
        super(name);
        this.value = value;
    }

    public boolean get() {
        return value;
    }

    public void set(boolean value) {
        this.value = value;
    }

    public void toggle() {
        value = !value;
    }

    @Override
    public void save(JsonObject out) {
        out.addProperty(name, value);
    }

    @Override
    public void load(JsonObject in) {
        if (in.has(name)) {
            try {
                value = in.get(name).getAsBoolean();
            } catch (Exception ignored) {
            }
        }
    }
}
