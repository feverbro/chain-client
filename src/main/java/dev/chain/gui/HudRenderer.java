package dev.chain.gui;

import dev.chain.ChainClient;
import dev.chain.module.Module;
import dev.chain.module.impl.render.Hud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayList;
import java.util.List;

/** Экранный интерфейс: ватермарк, координаты и список включённых модулей. */
public final class HudRenderer {
    private HudRenderer() {
    }

    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Hud hud = ChainClient.modules.hud;
        if (!hud.isEnabled() || mc.options.hudHidden || mc.player == null) {
            return;
        }
        TextRenderer font = mc.textRenderer;
        int accent = Theme.accent();

        int y = 4;
        if (hud.watermark.get()) {
            StringBuilder info = new StringBuilder();
            info.append(mc.getCurrentFps()).append(" fps");
            if (mc.getNetworkHandler() != null) {
                PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                if (entry != null) {
                    info.append("  |  ").append(entry.getLatency()).append(" ms");
                }
            }
            String brand = "Chain";
            String rest = " Client  |  " + mc.getSession().getUsername() + "  |  " + info;
            int w = font.getWidth(brand) + font.getWidth(rest) + 10;
            ctx.fill(4, y, 4 + w, y + 15, 0xB4101015);
            ctx.fill(4, y, 4 + w, y + 1, accent);
            ctx.drawTextWithShadow(font, brand, 9, y + 4, accent);
            ctx.drawTextWithShadow(font, rest, 9 + font.getWidth(brand), y + 4, Theme.TEXT);
            y += 18;
        }

        if (hud.coords.get()) {
            String text = String.format("XYZ: %d %d %d",
                    (int) Math.floor(mc.player.getX()),
                    (int) Math.floor(mc.player.getY()),
                    (int) Math.floor(mc.player.getZ()));
            int w = font.getWidth(text) + 10;
            ctx.fill(4, y, 4 + w, y + 14, 0xB4101015);
            ctx.fill(4, y, 5, y + 14, accent);
            ctx.drawTextWithShadow(font, text, 9, y + 3, Theme.TEXT);
        }

        if (hud.arrayList.get()) {
            List<Module> active = new ArrayList<>();
            for (Module m : ChainClient.modules.all()) {
                if (m.isEnabled() && !m.hidden && m != hud) {
                    active.add(m);
                }
            }
            active.sort((a, b) -> font.getWidth(b.name) - font.getWidth(a.name));

            int screenW = mc.getWindow().getScaledWidth();
            int ry = 4;
            int index = 0;
            for (Module m : active) {
                int w = font.getWidth(m.name);
                int color = Theme.accent(index);
                int x2 = screenW - 4;
                ctx.fill(x2 - w - 8, ry, x2, ry + 12, 0xAA101015);
                ctx.fill(x2 - 2, ry, x2, ry + 12, color);
                ctx.drawTextWithShadow(font, m.name, x2 - w - 5, ry + 2, color);
                ry += 12;
                index++;
            }
        }
    }
}
