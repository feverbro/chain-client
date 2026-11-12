package dev.chain.module;

import dev.chain.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    public final String name;
    public final String description;
    public final Category category;
    public final List<Setting> settings = new ArrayList<>();

    /** Код клавиши GLFW, -1 = не назначена. */
    public int keyCode;
    /** Скрытые модули не показываются в панелях и в списке активных модулей. */
    public boolean hidden;
    /** Раскрыты ли настройки в ClickGUI. */
    public boolean guiExpanded;
    /** Служебное поле для отслеживания нажатия клавиши. */
    boolean keyDown;

    private boolean enabled;

    protected Module(String name, String description, Category category, int defaultKey) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.keyCode = defaultKey;
    }

    protected <T extends Setting> T add(T setting) {
        settings.add(setting);
        return setting;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** Включить/выключить без сообщения на экране. */
    public void setEnabled(boolean value) {
        if (enabled == value) {
            return;
        }
        enabled = value;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void toggle() {
        setEnabled(!enabled);
        if (!hidden && mc.player != null) {
            mc.player.sendMessage(Text.literal("[Chain] " + name + (enabled ? " §aON" : " §cOFF")), true);
        }
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    /** Вызывается каждый тик, пока модуль включён и игрок в мире. */
    public void onTick() {
    }
}
