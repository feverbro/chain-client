package dev.chain.module.impl.render;

import dev.chain.module.Category;
import dev.chain.module.Module;
import dev.chain.setting.BoolSetting;
import dev.chain.setting.NumberSetting;
import dev.chain.util.Util;
import net.minecraft.entity.Entity;

/** Подсветка сущностей через встроенное свечение (видно сквозь стены). */
public class ESP extends Module {
    private final BoolSetting players = add(new BoolSetting("Players", true));
    private final BoolSetting mobs = add(new BoolSetting("Mobs", true));
    private final BoolSetting animals = add(new BoolSetting("Animals", false));
    private final NumberSetting range = add(new NumberSetting("Range", 64, 16, 256, 8));

    public ESP() {
        super("ESP", "Highlights entities through walls", Category.RENDER, -1);
    }

    @Override
    public void onTick() {
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) {
                continue;
            }
            boolean show = Util.isValidTarget(e, players.get(), mobs.get(), animals.get())
                    && mc.player.distanceTo(e) <= range.get();
            if (e.isGlowing() != show) {
                e.setGlowing(show);
            }
        }
    }

    @Override
    protected void onDisable() {
        if (mc.world == null) {
            return;
        }
        for (Entity e : mc.world.getEntities()) {
            if (e != mc.player && e.isGlowing()) {
                e.setGlowing(false);
            }
        }
    }
}
