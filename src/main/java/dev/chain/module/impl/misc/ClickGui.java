package dev.chain.module.impl.misc;

import dev.chain.gui.ClickGuiScreen;
import dev.chain.module.Category;
import dev.chain.module.Module;
import org.lwjgl.glfw.GLFW;

/** Не переключатель: клавиша просто открывает окно ClickGUI. */
public class ClickGui extends Module {
    public ClickGui() {
        super("ClickGUI", "Opens the module menu", Category.MISC, GLFW.GLFW_KEY_RIGHT_SHIFT);
        this.hidden = true;
    }

    @Override
    public void toggle() {
        mc.setScreen(new ClickGuiScreen());
    }
}
