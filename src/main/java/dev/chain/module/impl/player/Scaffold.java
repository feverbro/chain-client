package dev.chain.module.impl.player;

import dev.chain.module.Category;
import dev.chain.module.Module;
import dev.chain.setting.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.FallingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Ставит блок под ногами, пока вы идёте. */
public class Scaffold extends Module {
    private final NumberSetting delay = add(new NumberSetting("Delay", 0, 0, 5, 1));

    private int timer;

    public Scaffold() {
        super("Scaffold", "Places blocks under your feet", Category.PLAYER, -1);
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
        BlockPos below = mc.player.getBlockPos().down();
        if (!mc.world.getBlockState(below).isAir()) {
            return;
        }
        int slot = findBlockSlot();
        if (slot == -1) {
            return;
        }

        // Ищем соседний твёрдый блок, к которому можно «прислонить» новый
        for (Direction d : Direction.values()) {
            if (d == Direction.UP) {
                continue;
            }
            BlockPos neighbor = below.offset(d);
            if (mc.world.getBlockState(neighbor).isAir()) {
                continue;
            }
            Direction face = d.getOpposite();
            Vec3d hitVec = Vec3d.ofCenter(neighbor).add(Vec3d.of(face.getVector()).multiply(0.5));
            BlockHitResult hit = new BlockHitResult(hitVec, face, neighbor, false);

            int previous = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = slot;
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = previous;
            timer = delay.getInt();
            return;
        }
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!(stack.getItem() instanceof BlockItem)) {
                continue;
            }
            Block block = ((BlockItem) stack.getItem()).getBlock();
            if (block instanceof FallingBlock || block instanceof BlockWithEntity) {
                continue;
            }
            if (!block.getDefaultState().isFullCube(mc.world, BlockPos.ORIGIN)) {
                continue;
            }
            return i;
        }
        return -1;
    }
}
