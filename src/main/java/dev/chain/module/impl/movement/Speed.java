package dev.chain.module.impl.movement;

import dev.chain.module.Category;
import dev.chain.module.Module;
import dev.chain.setting.BoolSetting;
import dev.chain.setting.NumberSetting;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Speed extends Module {
    private final NumberSetting speed = add(new NumberSetting("Speed", 0.35, 0.1, 1.5, 0.05));
    private final BoolSetting bhop = add(new BoolSetting("Auto jump", false));

    public Speed() {
        super("Speed", "Increases your movement speed", Category.MOVEMENT, GLFW.GLFW_KEY_Z);
    }

    @Override
    public void onTick() {
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;
        double len = Math.sqrt(forward * forward + strafe * strafe);
        if (len == 0.0 || mc.player.isSneaking() || mc.player.isTouchingWater()) {
            return;
        }
        forward /= len;
        strafe /= len;
        double yaw = Math.toRadians(mc.player.getYaw());
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
        double x = (-sin * forward + cos * strafe) * speed.get();
        double z = (cos * forward + sin * strafe) * speed.get();

        Vec3d v = mc.player.getVelocity();
        double y = v.y;
        if (bhop.get() && mc.player.isOnGround()) {
            y = 0.42;
        }
        mc.player.setVelocity(new Vec3d(x, y, z));
    }
}
