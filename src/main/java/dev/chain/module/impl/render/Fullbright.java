package dev.chain.module.impl.render;

import dev.chain.module.Category;
import dev.chain.module.Module;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/** Полная яркость: накладывает ночное зрение только на клиенте. */
public class Fullbright extends Module {
    public Fullbright() {
        super("Fullbright", "Makes everything bright", Category.RENDER, -1);
    }

    @Override
    public void onTick() {
        if (!mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 20000, 0, false, false));
        }
    }

    @Override
    protected void onDisable() {
        if (mc.player != null) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }
}
