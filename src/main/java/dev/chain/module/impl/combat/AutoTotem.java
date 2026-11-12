package dev.chain.module.impl.combat;

import dev.chain.module.Category;
import dev.chain.module.Module;
import dev.chain.setting.NumberSetting;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Держит тотем бессмертия в левой руке: берёт его из инвентаря или хотбара. */
public class AutoTotem extends Module {
    private final NumberSetting delay = add(new NumberSetting("Delay", 2, 0, 10, 1));

    private int timer;

    public AutoTotem() {
        super("AutoTotem", "Keeps a totem of undying in your offhand", Category.COMBAT, -1);
    }

    @Override
    public void onTick() {
        if (mc.currentScreen != null || mc.interactionManager == null) {
            return;
        }
        if (timer > 0) {
            timer--;
            return;
        }
        if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
            return;
        }
        // Слоты playerScreenHandler: 9-35 инвентарь, 36-44 хотбар
        for (int slot = 9; slot <= 44; slot++) {
            if (mc.player.playerScreenHandler.getSlot(slot).getStack().isOf(Items.TOTEM_OF_UNDYING)) {
                mc.interactionManager.clickSlot(
                        mc.player.playerScreenHandler.syncId, slot, 40, SlotActionType.SWAP, mc.player);
                timer = delay.getInt();
                return;
            }
        }
    }
}
