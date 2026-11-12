package dev.chain.module.impl.movement;

import dev.chain.module.Category;
import dev.chain.module.Module;
import dev.chain.setting.ModeSetting;
import dev.chain.setting.NumberSetting;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Flight extends Module {
    private final ModeSetting mode = add(new ModeSetting("Mode", "Motion", "Motion", "Creative"));
    private final NumberSetting speed = add(new NumberSetting("Speed", 0.8, 0.1, 3.0, 0.1));

    public Flight() {
        super("Flight", "Lets you fly", Category.MOVEMENT, GLFW.GLFW_KEY_X);
    }

    @Override
    public void onTick() {
        if (mode.is("Creative")) {
            PlayerAbilities abilities = mc.player.getAbilities();
            abilities.allowFlying = true;
            abilities.flying = true;
            abilities.setFlySpeed((float) (speed.get() * 0.05));
            return;
        }

        // Режим Motion: свободное перемещение по направлению взгляда и клавиш
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;
        double len = Math.sqrt(forward * forward + strafe * strafe);
        double x = 0.0;
        double z = 0.0;
        if (len > 0.0) {
            forward /= len;
            strafe /= len;
            double yaw = Math.toRadians(mc.player.getYaw());
            double sin = Math.sin(yaw);
            double cos = Math.cos(yaw);
            x = (-sin * forward + cos * strafe) * speed.get();
            z = (cos * forward + sin * strafe) * speed.get();
        }
        double y = 0.0;
        if (mc.options.jumpKey.isPressed()) {
            y = speed.get();
        } else if (mc.options.sneakKey.isPressed()) {
            y = -speed.get();
        }
        mc.player.setVelocity(new Vec3d(x, y, z));
        mc.player.fallDistance = 0;
    }

    @Override
    protected void onDisable() {
        if (mc.player == null) {
            return;
        }
        if (mode.is("Creative") && !mc.player.isCreative() && !mc.player.isSpectator()) {
            PlayerAbilities abilities = mc.player.getAbilities();
            abilities.flying = false;
            abilities.allowFlying = false;
            abilities.setFlySpeed(0.05f);
        }
    }
}
