package dev.chain.module.impl.combat;

import dev.chain.module.Category;
import dev.chain.module.Module;
import dev.chain.setting.NumberSetting;
import dev.chain.util.Util;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

/**
 * Цикл: поставить якорь рядом с целью -> зарядить глоустоуном -> взорвать любым другим предметом.
 * Якорь взрывается только в Верхнем мире и Энде (в Незере не работает).
 */
public class AnchorAura extends Module {
    private final NumberSetting targetRange = add(new NumberSetting("Target range", 6.0, 3.0, 10.0, 0.5));
    private final NumberSetting placeRange = add(new NumberSetting("Place range", 4.5, 2.0, 6.0, 0.1));
    private final NumberSetting minSelf = add(new NumberSetting("Min self dist", 3.0, 0.0, 6.0, 0.5));
    private final NumberSetting delay = add(new NumberSetting("Delay", 2, 0, 10, 1));

    private int timer;

    public AnchorAura() {
        super("AnchorAura", "Places, charges and explodes respawn anchors", Category.COMBAT, GLFW.GLFW_KEY_V);
    }

    @Override
    public void onTick() {
        if (mc.currentScreen != null || mc.interactionManager == null) {
            return;
        }
        if (mc.world.getRegistryKey() == World.NETHER) {
            toggle();
            return;
        }
        if (timer > 0) {
            timer--;
            return;
        }
        LivingEntity target = Util.nearestTarget(targetRange.get(), true, false, false);
        if (target == null) {
            return;
        }

        BlockPos anchor = findAnchorNear(target);
        if (anchor != null) {
            int charges = mc.world.getBlockState(anchor).get(RespawnAnchorBlock.CHARGES);
            if (charges == 0) {
                int glow = Util.findHotbar(Items.GLOWSTONE);
                if (glow == -1) {
                    return;
                }
                Util.selectSlot(glow);
            } else {
                int other = findNonChargeSlot();
                if (other == -1) {
                    return;
                }
                Util.selectSlot(other);
            }
            useBlock(anchor);
            timer = delay.getInt();
            return;
        }

        // Якоря рядом нет, ставим новый
        int slot = Util.findHotbar(Items.RESPAWN_ANCHOR);
        if (slot == -1) {
            return;
        }
        BlockPos place = findPlaceSpot(target);
        if (place == null) {
            return;
        }
        Util.selectSlot(slot);
        BlockPos below = place.down();
        Util.lookAt(Vec3d.ofCenter(place));
        BlockHitResult hit = new BlockHitResult(
                Vec3d.ofCenter(below).add(0.0, 0.5, 0.0), Direction.UP, below, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
        timer = delay.getInt();
    }

    private BlockPos findAnchorNear(LivingEntity target) {
        BlockPos t = target.getBlockPos();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos p = t.add(x, y, z);
                    if (!mc.world.getBlockState(p).isOf(Blocks.RESPAWN_ANCHOR)) {
                        continue;
                    }
                    Vec3d c = Vec3d.ofCenter(p);
                    if (mc.player.getEyePos().distanceTo(c) > placeRange.get()) {
                        continue;
                    }
                    if (mc.player.getPos().distanceTo(c) < minSelf.get()) {
                        continue;
                    }
                    double d = c.distanceTo(target.getPos());
                    if (d < bestDist) {
                        bestDist = d;
                        best = p;
                    }
                }
            }
        }
        return best;
    }

    private BlockPos findPlaceSpot(LivingEntity target) {
        BlockPos t = target.getBlockPos();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos p = t.add(x, y, z);
                    if (!mc.world.isAir(p)) {
                        continue;
                    }
                    BlockPos below = p.down();
                    if (!mc.world.getBlockState(below).isSolidBlock(mc.world, below)) {
                        continue;
                    }
                    if (p.equals(t) || p.equals(t.up())) {
                        continue;
                    }
                    Vec3d c = Vec3d.ofCenter(p);
                    if (mc.player.getEyePos().distanceTo(c) > placeRange.get()) {
                        continue;
                    }
                    if (mc.player.getPos().distanceTo(c) < minSelf.get()) {
                        continue;
                    }
                    double d = c.distanceTo(target.getPos());
                    if (d < bestDist) {
                        bestDist = d;
                        best = p;
                    }
                }
            }
        }
        return best;
    }

    /** Слот хотбара без глоустоуна и без якоря (им взрываем заряженный якорь). */
    private int findNonChargeSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (!s.isOf(Items.GLOWSTONE) && !s.isOf(Items.RESPAWN_ANCHOR)) {
                return i;
            }
        }
        return -1;
    }

    private void useBlock(BlockPos pos) {
        Util.lookAt(Vec3d.ofCenter(pos));
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
