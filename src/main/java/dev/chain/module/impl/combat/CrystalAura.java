package dev.chain.module.impl.combat;

import dev.chain.module.Category;
import dev.chain.module.Module;
import dev.chain.setting.NumberSetting;
import dev.chain.util.Util;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class CrystalAura extends Module {
    private final NumberSetting targetRange = add(new NumberSetting("Target range", 8.0, 3.0, 12.0, 0.5));
    private final NumberSetting breakRange = add(new NumberSetting("Break range", 4.5, 2.0, 6.0, 0.1));
    private final NumberSetting placeRange = add(new NumberSetting("Place range", 4.5, 2.0, 6.0, 0.1));
    private final NumberSetting minSelf = add(new NumberSetting("Min self dist", 3.0, 0.0, 6.0, 0.5));
    private final NumberSetting placeDelay = add(new NumberSetting("Place delay", 2, 0, 10, 1));
    private final NumberSetting breakDelay = add(new NumberSetting("Break delay", 1, 0, 10, 1));

    private int placeTimer;
    private int breakTimer;

    public CrystalAura() {
        super("CrystalAura", "Places and breaks end crystals near enemies", Category.COMBAT, GLFW.GLFW_KEY_C);
    }

    @Override
    public void onTick() {
        if (mc.currentScreen != null || mc.interactionManager == null) {
            return;
        }
        if (placeTimer > 0) placeTimer--;
        if (breakTimer > 0) breakTimer--;

        LivingEntity target = Util.nearestTarget(targetRange.get(), true, false, false);
        if (target == null) {
            return;
        }

        // 1) Ломаем кристалл рядом с целью
        if (breakTimer == 0) {
            EndCrystalEntity crystal = null;
            double best = Double.MAX_VALUE;
            for (Entity e : mc.world.getEntities()) {
                if (!(e instanceof EndCrystalEntity) || !e.isAlive()) {
                    continue;
                }
                double dSelf = mc.player.distanceTo(e);
                if (dSelf > breakRange.get() || dSelf < minSelf.get()) {
                    continue;
                }
                double dTarget = e.distanceTo(target);
                if (dTarget < best) {
                    best = dTarget;
                    crystal = (EndCrystalEntity) e;
                }
            }
            if (crystal != null) {
                Util.lookAt(crystal.getBoundingBox().getCenter());
                Util.attack(crystal);
                breakTimer = breakDelay.getInt();
                return;
            }
        }

        // 2) Ставим новый кристалл
        if (placeTimer > 0) {
            return;
        }
        Hand hand = null;
        if (mc.player.getMainHandStack().isOf(Items.END_CRYSTAL)) {
            hand = Hand.MAIN_HAND;
        } else if (mc.player.getOffHandStack().isOf(Items.END_CRYSTAL)) {
            hand = Hand.OFF_HAND;
        } else {
            int slot = Util.findHotbar(Items.END_CRYSTAL);
            if (slot == -1) {
                return;
            }
            Util.selectSlot(slot);
            hand = Hand.MAIN_HAND;
        }

        BlockPos bestPos = null;
        double bestDist = Double.MAX_VALUE;
        BlockPos origin = mc.player.getBlockPos();
        int r = (int) Math.ceil(placeRange.get());
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = origin.add(x, y, z);
                    if (!canPlace(pos)) {
                        continue;
                    }
                    Vec3d crystalPos = Vec3d.ofCenter(pos.up());
                    if (mc.player.getEyePos().distanceTo(crystalPos) > placeRange.get()) {
                        continue;
                    }
                    if (mc.player.getPos().distanceTo(crystalPos) < minSelf.get()) {
                        continue;
                    }
                    double d = target.getPos().distanceTo(crystalPos);
                    if (d < bestDist) {
                        bestDist = d;
                        bestPos = pos;
                    }
                }
            }
        }
        if (bestPos == null) {
            return;
        }

        Util.lookAt(Vec3d.ofCenter(bestPos));
        BlockHitResult hit = new BlockHitResult(
                Vec3d.ofCenter(bestPos).add(0.0, 0.5, 0.0), Direction.UP, bestPos, false);
        mc.interactionManager.interactBlock(mc.player, hand, hit);
        mc.player.swingHand(hand);
        placeTimer = placeDelay.getInt();
    }

    /** Кристалл ставится на обсидиан/бедрок, сверху должно быть 2 свободных блока и нет сущностей. */
    private boolean canPlace(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        if (!state.isOf(Blocks.OBSIDIAN) && !state.isOf(Blocks.BEDROCK)) {
            return false;
        }
        if (!mc.world.isAir(pos.up()) || !mc.world.isAir(pos.up(2))) {
            return false;
        }
        Box box = new Box(pos.getX(), pos.getY() + 1, pos.getZ(),
                pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1);
        return mc.world.getOtherEntities(null, box).isEmpty();
    }
}
