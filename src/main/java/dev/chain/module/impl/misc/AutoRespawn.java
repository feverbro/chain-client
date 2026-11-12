package dev.chain.module.impl.misc;

import dev.chain.module.Category;
import dev.chain.module.Module;

public class AutoRespawn extends Module {
    public AutoRespawn() {
        super("AutoRespawn", "Respawns instantly after death", Category.MISC, -1);
    }

    @Override
    public void onTick() {
        if (mc.player.isDead()) {
            mc.player.requestRespawn();
        }
    }
}
