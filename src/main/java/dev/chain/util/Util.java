package dev.chain.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class Util {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private Util() {
    }

    /** Подходит ли сущность под выбранные типы целей. */
    public static boolean isValidTarget(Entity e, boolean players, boolean mobs, boolean animals) {
        if (e == mc.player || !(e instanceof LivingEntity) || !e.isAlive() || e.isSpectator()) {
            return false;
        }
        if (e instanceof ArmorStandEntity) {
            return false;
        }
        if (e instanceof PlayerEntity) {
            return players;
        }
        if (e instanceof Monster) {
            return mobs;
        }
        if (e instanceof AnimalEntity) {
            return animals;
        }
        return false;
    }

    /** Расстояние от глаз игрока до ближайшей точки хитбокса сущности. */
    public static double distanceToBox(Entity e) {
        Vec3d eye = mc.player.getEyePos();
        Box b = e.getBoundingBox();
        double x = MathHelper.clamp(eye.x, b.minX, b.maxX);
        double y = MathHelper.clamp(eye.y, b.minY, b.maxY);
        double z = MathHelper.clamp(eye.z, b.minZ, b.maxZ);
        return eye.distanceTo(new Vec3d(x, y, z));
    }

    /** Ближайшая подходящая цель в радиусе range или null. */
    public static LivingEntity nearestTarget(double range, boolean players, boolean mobs, boolean animals) {
        if (mc.player == null || mc.world == null) {
            return null;
        }
        LivingEntity best = null;
        double bestDist = range;
        for (Entity e : mc.world.getEntities()) {
            if (!isValidTarget(e, players, mobs, animals)) {
                continue;
            }
            double d = distanceToBox(e);
            if (d <= bestDist) {
                bestDist = d;
                best = (LivingEntity) e;
            }
        }
        return best;
    }

    /** Поворот головы к точке. maxStep — максимум градусов за тик (180 = мгновенно). */
    public static void lookAt(Vec3d target, float maxStep) {
        ClientPlayerEntity p = mc.player;
        if (p == null) {
            return;
        }
        Vec3d eye = p.getEyePos();
        double dx = target.x - eye.x;
        double dy = target.y - eye.y;
        double dz = target.z - eye.z;
        double hor = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, hor));

        float dYaw = MathHelper.clamp(MathHelper.wrapDegrees(yaw - p.getYaw()), -maxStep, maxStep);
        float dPitch = MathHelper.clamp(pitch - p.getPitch(), -maxStep, maxStep);
        p.setYaw(p.getYaw() + dYaw);
        p.setPitch(MathHelper.clamp(p.getPitch() + dPitch, -90.0f, 90.0f));
    }

    public static void lookAt(Vec3d target) {
        lookAt(target, 180.0f);
    }

    public static void attack(Entity target) {
        if (mc.player == null || mc.interactionManager == null) {
            return;
        }
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    /** Заряд атаки восстановлен (оружие готово к полному удару). */
    public static boolean attackReady() {
        return mc.player != null && mc.player.getAttackCooldownProgress(0.5f) >= 0.95f;
    }

    /** Игрок падает и сможет нанести критический удар. */
    public static boolean canCrit() {
        ClientPlayerEntity p = mc.player;
        return p.fallDistance > 0.0f && !p.isOnGround() && !p.isClimbing() && !p.isTouchingWater();
    }

    /** Слот хотбара (0-8) с предметом или -1. */
    public static int findHotbar(Item item) {
        if (mc.player == null) {
            return -1;
        }
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) {
                return i;
            }
        }
        return -1;
    }

    public static void selectSlot(int slot) {
        if (mc.player != null && slot >= 0 && slot < 9) {
            mc.player.getInventory().selectedSlot = slot;
        }
    }
}
