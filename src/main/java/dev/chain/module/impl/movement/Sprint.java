package dev.chain.module.impl.movement;

import dev.chain.module.Category;
import dev.chain.module.Module;
import dev.chain.setting.BoolSetting;
import org.lwjgl.glfw.GLFW;

public class Sprint extends Module {
    private final BoolSetting omni = add(new BoolSetting("Omni sprint", false));

    public Sprint() {
        super("Sprint", "Sprints automatically", Category.MOVEMENT, GLFW.GLFW_KEY_B);
    }

    @Override
    public void onTick() {
        if (mc.player.isSneaking() || mc.player.horizontalCollision || mc.player.isUsingItem()) {
            return;
        }
        if (mc.player.getHungerManager().getFoodLevel() <= 6) {
            return;
        }
        float forward = mc.player.input.movementForward;
        float sideways = mc.player.input.movementSideways;
        boolean moving = omni.get() ? (forward != 0.0f || sideways != 0.0f) : forward > 0.0f;
        if (moving) {
            mc.player.setSprinting(true);
        }
    }
}
