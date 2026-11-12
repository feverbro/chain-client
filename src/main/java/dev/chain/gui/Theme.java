package dev.chain.gui;

import dev.chain.ChainClient;
import net.minecraft.util.math.MathHelper;

/** Цвета интерфейса (формат ARGB). */
public final class Theme {
    public static final int PANEL_BG = 0xE6121217;
    public static final int HEADER_TOP = 0xF020202A;
    public static final int HEADER_BOTTOM = 0xF016161D;
    public static final int ROW = 0xC81A1A22;
    public static final int ROW_HOVER = 0xC8262631;
    public static final int SETTING_BG = 0xD2101015;
    public static final int TRACK = 0xFF2C2C38;
    public static final int TEXT = 0xFFEDEDF2;
    public static final int TEXT_DIM = 0xFF8D8D9C;

    private Theme() {
    }

    public static int accent() {
        return accent(0);
    }

    /** Цвет акцента; offset сдвигает фазу для радужной темы (волна по списку). */
    public static int accent(int offset) {
        String theme = ChainClient.modules.hud.theme.get();
        switch (theme) {
            case "Blue":
                return 0xFF3B82F6;
            case "Green":
                return 0xFF22C55E;
            case "Red":
                return 0xFFEF4444;
            case "Rainbow":
                float hue = ((System.currentTimeMillis() / 15 + offset * 12L) % 360L) / 360.0f;
                return 0xFF000000 | MathHelper.hsvToRgb(hue, 0.6f, 1.0f);
            default:
                return 0xFF8B5CF6;
        }
    }

    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
}
