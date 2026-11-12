package dev.chain.module.impl.combat;

import dev.chain.module.Category;
import dev.chain.module.Module;
import dev.chain.setting.BoolSetting;
import dev.chain.setting.NumberSetting;
import dev.chain.util.Util;
import net.minecraft.entity.LivingEntity;
import org.lwjgl.glfw.GLFW;

public class KillAura extends Module {
    private final NumberSetting range = add(new NumberSetting("Range", 3.5, 2.0, 6.0, 0.1));
    private final BoolSetting players = add(new BoolSetting("Players", true));
    private final BoolSetting mobs = add(new BoolSetting("Mobs", false));
    private final BoolSetting animals = add(new BoolSetting("Animals", false));
    private final BoolSetting rotate = add(new BoolSetting("Rotate", true));
    private final NumberSetting rotSpeed = add(new NumberSetting("Rotation speed", 60, 10, 180, 5));
    private final BoolSetting walls = add(new BoolSetting("Through walls", false));
    private final BoolSetting critOnly = add(new BoolSetting("Only crits", false));

    public KillAura() {
        super("KillAura", "Automatically attacks nearby targets", Category.COMBAT, GLFW.GLFW_KEY_R);
    }

    @Override
    public void onTick() {
        if (mc.currentScreen != null) {
            return;
        }
        LivingEntity target = Util.nearestTarget(range.get(), players.get(), mobs.get(), animals.get());
        if (target == null) {
            return;
        }
        if (!walls.get() && !mc.player.canSee(target)) {
            return;
        }
        if (rotate.get()) {
            Util.lookAt(target.getBoundingBox().getCenter(), rotSpeed.getFloat());
        }
        if (!Util.attackReady()) {
            return;
        }
        if (critOnly.get() && !Util.canCrit()) {
            return;
        }
        Util.attack(target);
    }
}
