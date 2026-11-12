package dev.chain.gui;

import dev.chain.ChainClient;
import dev.chain.config.ConfigManager;
import dev.chain.module.Category;
import dev.chain.module.Module;
import dev.chain.setting.BoolSetting;
import dev.chain.setting.ModeSetting;
import dev.chain.setting.NumberSetting;
import dev.chain.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Меню модулей в стиле панелей:
 * ЛКМ по модулю - включить/выключить, ПКМ - открыть настройки, СКМ - назначить клавишу.
 * Заголовок панели можно перетаскивать, ПКМ по нему сворачивает панель.
 */
public class ClickGuiScreen extends Screen {
    private static final int PANEL_W = 130;
    private static final int HEADER_H = 18;
    private static final int ROW_H = 15;
    private static final int SET_H = 13;
    private static final int NUM_H = 19;

    // Положение и состояние панелей помним между открытиями меню
    private static final Map<Category, int[]> POSITIONS = new EnumMap<>(Category.class);
    private static final Set<Category> COLLAPSED = EnumSet.noneOf(Category.class);

    private final List<Panel> panels = new ArrayList<>();

    private Panel draggingPanel;
    private int dragOffX;
    private int dragOffY;

    private NumberSetting draggingSlider;
    private int sliderX;
    private int sliderW;

    private Module binding;

    private static final class Panel {
        final Category category;
        final List<Module> modules;
        int x;
        int y;

        Panel(Category category, List<Module> modules, int x, int y) {
            this.category = category;
            this.modules = modules;
            this.x = x;
            this.y = y;
        }

        boolean isOpen() {
            return !COLLAPSED.contains(category);
        }
    }

    /** Одна кликабельная строка: заголовок панели, модуль или настройка. */
    private static final class Item {
        final Panel panel;
        final Module module;
        final Setting setting;
        final int x;
        final int y;
        final int w;
        final int h;

        Item(Panel panel, Module module, Setting setting, int x, int y, int w, int h) {
            this.panel = panel;
            this.module = module;
            this.setting = setting;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        boolean contains(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    public ClickGuiScreen() {
        super(Text.literal("Chain Client"));
        int index = 0;
        for (Category category : Category.values()) {
            List<Module> modules = ChainClient.modules.byCategory(category);
            if (modules.isEmpty()) {
                continue;
            }
            int[] pos = POSITIONS.get(category);
            if (pos == null) {
                pos = new int[]{12 + index * (PANEL_W + 8), 24};
                POSITIONS.put(category, pos);
            }
            panels.add(new Panel(category, modules, pos[0], pos[1]));
            index++;
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        ConfigManager.save(ChainClient.modules);
    }

    // ---------- раскладка ----------

    private static int settingHeight(Setting s) {
        return s instanceof NumberSetting ? NUM_H : SET_H;
    }

    private List<Item> layout() {
        List<Item> list = new ArrayList<>();
        for (Panel p : panels) {
            list.add(new Item(p, null, null, p.x, p.y, PANEL_W, HEADER_H));
            if (!p.isOpen()) {
                continue;
            }
            int cy = p.y + HEADER_H;
            for (Module m : p.modules) {
                list.add(new Item(p, m, null, p.x, cy, PANEL_W, ROW_H));
                cy += ROW_H;
                if (m.guiExpanded) {
                    for (Setting s : m.settings) {
                        int h = settingHeight(s);
                        list.add(new Item(p, m, s, p.x, cy, PANEL_W, h));
                        cy += h;
                    }
                }
            }
        }
        return list;
    }

    /** Верхний (нарисованный последним) элемент под курсором. */
    private Item itemAt(double mx, double my) {
        List<Item> items = layout();
        for (int i = items.size() - 1; i >= 0; i--) {
            if (items.get(i).contains(mx, my)) {
                return items.get(i);
            }
        }
        return null;
    }

    // ---------- отрисовка ----------

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        int accent = Theme.accent();

        ctx.fill(0, 0, width, height, 0x78000000);

        Item top = itemAt(mouseX, mouseY);
        Module hoveredModule = null;

        for (Item it : layout()) {
            boolean hover = it == top;
            if (it.module == null) {
                drawHeader(ctx, font, it, accent);
            } else if (it.setting == null) {
                drawModule(ctx, font, it, hover, accent);
                if (hover) {
                    hoveredModule = it.module;
                }
            } else {
                drawSetting(ctx, font, it, accent);
            }
        }

        if (hoveredModule != null && binding == null) {
            drawTooltip(ctx, font, hoveredModule.description, mouseX, mouseY);
        }

        String hint = binding != null
                ? "Press a key for " + binding.name + "  (Esc - cancel, Del - unbind)"
                : "LMB - toggle   RMB - settings   MMB - bind key   RShift - close";
        ctx.drawCenteredTextWithShadow(font, hint, width / 2, height - 14, Theme.TEXT_DIM);
    }

    private void drawHeader(DrawContext ctx, TextRenderer font, Item it, int accent) {
        ctx.fillGradient(it.x, it.y, it.x + it.w, it.y + it.h, Theme.HEADER_TOP, Theme.HEADER_BOTTOM);
        ctx.fill(it.x, it.y + it.h - 1, it.x + it.w, it.y + it.h, accent);
        ctx.drawTextWithShadow(font, it.panel.category.title, it.x + 6, it.y + 5, Theme.TEXT);
        String mark = it.panel.isOpen() ? "-" : "+";
        ctx.drawTextWithShadow(font, mark, it.x + it.w - 6 - font.getWidth(mark), it.y + 5, Theme.TEXT_DIM);
    }

    private void drawModule(DrawContext ctx, TextRenderer font, Item it, boolean hover, int accent) {
        Module m = it.module;
        boolean on = m.isEnabled();
        ctx.fill(it.x, it.y, it.x + it.w, it.y + it.h, hover ? Theme.ROW_HOVER : Theme.ROW);
        if (on) {
            ctx.fill(it.x, it.y, it.x + it.w, it.y + it.h, Theme.withAlpha(accent, 0x40));
            ctx.fill(it.x, it.y, it.x + 2, it.y + it.h, accent);
        }
        ctx.drawTextWithShadow(font, m.name, it.x + 6, it.y + 4, on ? Theme.TEXT : Theme.TEXT_DIM);

        String right = "";
        if (binding == m) {
            right = "...";
        } else if (m.keyCode > 0) {
            right = "[" + keyName(m.keyCode) + "]";
        }
        if (!m.settings.isEmpty()) {
            right = right + (right.isEmpty() ? "" : " ") + (m.guiExpanded ? "-" : "+");
        }
        if (!right.isEmpty()) {
            int color = binding == m ? accent : Theme.TEXT_DIM;
            ctx.drawTextWithShadow(font, right, it.x + it.w - 5 - font.getWidth(right), it.y + 4, color);
        }
    }

    private void drawSetting(DrawContext ctx, TextRenderer font, Item it, int accent) {
        Setting s = it.setting;
        ctx.fill(it.x, it.y, it.x + it.w, it.y + it.h, Theme.SETTING_BG);
        ctx.fill(it.x, it.y, it.x + 1, it.y + it.h, Theme.withAlpha(accent, 0x90));

        if (s instanceof BoolSetting) {
            BoolSetting b = (BoolSetting) s;
            ctx.drawTextWithShadow(font, b.name, it.x + 7, it.y + 3, Theme.TEXT);
            int bx = it.x + it.w - 15;
            int by = it.y + 2;
            ctx.fill(bx, by, bx + 9, by + 9, Theme.TRACK);
            if (b.get()) {
                ctx.fill(bx + 1, by + 1, bx + 8, by + 8, accent);
            }
        } else if (s instanceof ModeSetting) {
            ModeSetting m = (ModeSetting) s;
            ctx.drawTextWithShadow(font, m.name, it.x + 7, it.y + 3, Theme.TEXT);
            String value = m.get();
            ctx.drawTextWithShadow(font, value, it.x + it.w - 6 - font.getWidth(value), it.y + 3, accent);
        } else if (s instanceof NumberSetting) {
            NumberSetting n = (NumberSetting) s;
            ctx.drawTextWithShadow(font, n.name, it.x + 7, it.y + 2, Theme.TEXT);
            String value = n.display();
            ctx.drawTextWithShadow(font, value, it.x + it.w - 6 - font.getWidth(value), it.y + 2, accent);
            int tx = it.x + 6;
            int tw = it.w - 12;
            int ty = it.y + it.h - 6;
            ctx.fill(tx, ty, tx + tw, ty + 3, Theme.TRACK);
            int filled = (int) (tw * (n.get() - n.min) / (n.max - n.min));
            ctx.fill(tx, ty, tx + filled, ty + 3, accent);
            ctx.fill(tx + filled - 1, ty - 1, tx + filled + 1, ty + 4, Theme.TEXT);
        }
    }

    private void drawTooltip(DrawContext ctx, TextRenderer font, String text, int mouseX, int mouseY) {
        int w = font.getWidth(text) + 8;
        int x = Math.min(mouseX + 8, width - w - 2);
        int y = mouseY + 10;
        ctx.fill(x, y, x + w, y + 13, 0xEE0B0B0F);
        ctx.fill(x, y, x + w, y + 1, Theme.accent());
        ctx.drawTextWithShadow(font, text, x + 4, y + 3, Theme.TEXT);
    }

    private static String keyName(int keyCode) {
        String name = InputUtil.fromKeyCode(keyCode, 0).getLocalizedText().getString();
        return name.length() > 6 ? name.substring(0, 6) : name;
    }

    // ---------- ввод ----------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (binding != null) {
            binding = null;
            return true;
        }
        Item it = itemAt(mouseX, mouseY);
        if (it == null) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        // Панель, по которой кликнули, рисуется поверх остальных
        panels.remove(it.panel);
        panels.add(it.panel);

        if (it.module == null) {
            if (button == 0) {
                draggingPanel = it.panel;
                dragOffX = (int) mouseX - it.panel.x;
                dragOffY = (int) mouseY - it.panel.y;
            } else if (button == 1) {
                if (COLLAPSED.contains(it.panel.category)) {
                    COLLAPSED.remove(it.panel.category);
                } else {
                    COLLAPSED.add(it.panel.category);
                }
            }
            return true;
        }

        if (it.setting == null) {
            Module m = it.module;
            if (button == 0) {
                m.toggle();
            } else if (button == 1) {
                if (!m.settings.isEmpty()) {
                    m.guiExpanded = !m.guiExpanded;
                }
            } else if (button == 2) {
                binding = m;
            }
            return true;
        }

        Setting s = it.setting;
        if (s instanceof BoolSetting) {
            if (button == 0) {
                ((BoolSetting) s).toggle();
            }
        } else if (s instanceof ModeSetting) {
            if (button == 0) {
                ((ModeSetting) s).next();
            } else if (button == 1) {
                ((ModeSetting) s).prev();
            }
        } else if (s instanceof NumberSetting && button == 0) {
            draggingSlider = (NumberSetting) s;
            sliderX = it.x + 6;
            sliderW = it.w - 12;
            updateSlider(mouseX);
        }
        return true;
    }

    private void updateSlider(double mouseX) {
        double t = MathHelper.clamp((mouseX - sliderX) / sliderW, 0.0, 1.0);
        draggingSlider.set(draggingSlider.min + t * (draggingSlider.max - draggingSlider.min));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && draggingPanel != null) {
            draggingPanel.x = MathHelper.clamp((int) mouseX - dragOffX, 0, Math.max(0, width - PANEL_W));
            draggingPanel.y = MathHelper.clamp((int) mouseY - dragOffY, 0, Math.max(0, height - HEADER_H));
            POSITIONS.put(draggingPanel.category, new int[]{draggingPanel.x, draggingPanel.y});
            return true;
        }
        if (button == 0 && draggingSlider != null) {
            updateSlider(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingPanel = null;
        draggingSlider = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding != null) {
            if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                binding.keyCode = -1;
            } else if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                binding.keyCode = keyCode;
            }
            binding = null;
            return true;
        }
        if (keyCode == ChainClient.modules.clickGui.keyCode) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
