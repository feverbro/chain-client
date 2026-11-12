package dev.chain.module.impl.render;

import dev.chain.module.Category;
import dev.chain.module.Module;
import dev.chain.setting.BoolSetting;
import dev.chain.setting.ModeSetting;

/** Настройки экранного интерфейса (ватермарк, список модулей, координаты, цветовая тема). */
public class Hud extends Module {
    public final BoolSetting watermark = add(new BoolSetting("Watermark", true));
    public final BoolSetting arrayList = add(new BoolSetting("Module list", true));
    public final BoolSetting coords = add(new BoolSetting("Coordinates", true));
    public final ModeSetting theme = add(new ModeSetting("Theme", "Purple", "Purple", "Blue", "Green", "Red", "Rainbow"));

    public Hud() {
        super("HUD", "On-screen interface and color theme", Category.RENDER, -1);
    }
}
