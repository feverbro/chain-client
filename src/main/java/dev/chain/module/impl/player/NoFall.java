package dev.chain.module.impl.player;

import dev.chain.module.Category;
import dev.chain.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module {
    public NoFall() {
        super("NoFall", "Cancels fall damage", Category.PLAYER, -1);
    }

    @Override
    public void onTick() {
        if (mc.player.fallDistance > 2.5f && !mc.player.isOnGround() && !mc.player.getAbilities().flying) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
        }
    }
}
