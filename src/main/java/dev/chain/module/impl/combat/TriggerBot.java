package dev.chain.module.impl.combat;

import dev.chain.module.Category;
import dev.chain.module.Module;
import dev.chain.setting.BoolSetting;
import dev.chain.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public class TriggerBot extends Module {
    private final BoolSetting players = add(new BoolSetting("Players", true));
    private final BoolSetting mobs = add(new BoolSetting("Mobs", false));
    private final BoolSetting animals = add(new BoolSetting("Animals", false));
    private final BoolSetting critOnly = add(new BoolSetting("Only crits", false));

    public TriggerBot() {
        super("TriggerBot", "Attacks when your crosshair is on a target", Category.COMBAT, GLFW.GLFW_KEY_G);
    }

    @Override
    public void onTick() {
        if (mc.currentScreen != null) {
            return;
        }
        HitResult hit = mc.crosshairTarget;
        if (!(hit instanceof EntityHitResult)) {
            return;
        }
        Entity target = ((EntityHitResult) hit).getEntity();
        if (!Util.isValidTarget(target, players.get(), mobs.get(), animals.get())) {
            return;
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
