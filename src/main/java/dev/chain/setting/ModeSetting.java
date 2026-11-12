package dev.chain.setting;

import com.google.gson.JsonObject;

public class ModeSetting extends Setting {
    private final String[] modes;
    private int index;

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name);
        this.modes = modes;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(defaultMode)) {
                index = i;
            }
        }
    }

    public String get() {
        return modes[index];
    }

    public boolean is(String mode) {
        return modes[index].equals(mode);
    }

    public void next() {
        index = (index + 1) % modes.length;
    }

    public void prev() {
        index = (index - 1 + modes.length) % modes.length;
    }

    @Override
    public void save(JsonObject out) {
        out.addProperty(name, modes[index]);
    }

    @Override
    public void load(JsonObject in) {
        if (in.has(name)) {
            try {
                String saved = in.get(name).getAsString();
                for (int i = 0; i < modes.length; i++) {
                    if (modes[i].equals(saved)) {
                        index = i;
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }
}
