package com.aleksiyflekssiy.tutorialmod.entity.goal.toad;

import com.aleksiyflekssiy.tutorialmod.entity.ToadEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.EnumSet;

public class TongueSwingGoal extends Goal {
    private final ToadEntity toad;
    private LivingEntity caughtEntity;
    private int swingTick = 0;
    private final int SWING_DURATION = 80; // 2 секунды
    private final float SWING_SPEED = 0.25F; // Уменьшена угловая скорость
    private Vec3 centerPosition; // Центр вращения (позиция лягушки)
    private float angle = 0.0F; // Текущий угол
    private boolean isReleased = false;
    private float initialDistance = 0.0F; // Изначальная дистанция до цели
    private int releaseTick = 0;

    public TongueSwingGoal(ToadEntity toad) {
        this.toad = toad;
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public boolean canUse() {
        if (toad.getTarget() != null && !toad.getTarget().isSpectator()) {
            if (!toad.isTamed()) {
                if (toad.isCooldownOff()) return toad.position().subtract(toad.getTarget().position()).length() > 4;
            } else return toad.getOrder() == ToadEntity.Order.NONE || toad.getOrder() == ToadEntity.Order.SWING;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return caughtEntity != null && caughtEntity.isAlive();
    }

    @Override
    public void start() {
        toad.getNavigation().stop();
        toad.lookAt(EntityAnchorArgument.Anchor.EYES, toad.getTarget().position());
        Vec3 startPos = toad.getEyePosition();
        Vec3 endPos = startPos.add(toad.getViewVector(1).scale(50));
        EntityHitResult result = ProjectileUtil.getEntityHitResult(
                toad.level(), toad, startPos, endPos,
                new AABB(startPos, endPos),
                entity -> entity instanceof LivingEntity && !entity.equals(toad)
        );

        if (result != null && result.getEntity() instanceof LivingEntity entity) {
            caughtEntity = entity;
            centerPosition = toad.position(); // Центр вращения
            initialDistance = (float) centerPosition.distanceTo(caughtEntity.position()); // Изначальная дистанция
            angle = (float) Math.atan2(caughtEntity.getZ() - centerPosition.z, caughtEntity.getX() - centerPosition.x);
            swingTick = 0;
            toad.setDistance(initialDistance);
            System.out.println("START: " + caughtEntity.getClass().getSimpleName());
        }
    }

    @Override
    public void tick() {
        if (caughtEntity == null) return;
        if (!toad.level().isClientSide()) { // Только сервер
            if (!isReleased) {
                // Увеличиваем угол вращения
                angle += SWING_SPEED;

                // Вычисляем целевую позицию
                double targetX = centerPosition.x + initialDistance * Math.cos(angle);
                double targetZ = centerPosition.z + initialDistance * Math.sin(angle);

                // Задаём движение с увеличенным шагом
                Vec3 movementVector = new Vec3(
                        (targetX - caughtEntity.getX()) * 0.5, // Увеличили с 0.5 до 1.0
                        caughtEntity.getDeltaMovement().y,
                        (targetZ - caughtEntity.getZ()) * 0.5
                );

                // Применяем движение
                caughtEntity.setDeltaMovement(movementVector);
                caughtEntity.move(MoverType.SELF, movementVector);

                // Синхронизация для игроков
                if (caughtEntity instanceof Player) {
                    caughtEntity.hurtMarked = true;
                }

                // Проверяем столкновение
                if (caughtEntity.horizontalCollision || caughtEntity.minorHorizontalCollision) {
                    caughtEntity.hurt(toad.level().damageSources().flyIntoWall(), 2);
                    System.out.println("HURT");
                    // Не обнуляем скорость, чтобы сущность осталась у стены
                }

                // Поворачиваем жабу
                toad.lookAt(EntityAnchorArgument.Anchor.EYES, caughtEntity.position());

                swingTick++;
                if (swingTick >= SWING_DURATION) {
                    releaseEntity();
                }
            } else {
                if (releaseTick >= 60) {
                    this.stop();
                    return;
                } else releaseTick++;
            }

            System.out.println("Tick: " + caughtEntity.getClass().getSimpleName() +
                    " Speed: " + caughtEntity.getDeltaMovement().length() +
                    " Distance: " + centerPosition.distanceTo(caughtEntity.position()));
            toad.setLastTickUse();
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void releaseEntity() {
        if (caughtEntity == null) return;

        float releaseSpeed = SWING_SPEED * initialDistance * 4.0F;
        double velocityX = releaseSpeed * Math.cos(angle);
        double velocityZ = releaseSpeed * Math.sin(angle);
        Vec3 releaseMotion = new Vec3(velocityX, 0.5, velocityZ);
        caughtEntity.setDeltaMovement(releaseMotion);
        caughtEntity.move(MoverType.SELF, releaseMotion);

        swingTick = 0;
        toad.setDistance(0);
        System.out.println("Released: " + caughtEntity.getClass().getSimpleName());
        isReleased = true;
    }

    @Override
    public void stop() {
        if (caughtEntity != null) {
            caughtEntity = null;
            isReleased = false;
            releaseTick = 0;
            toad.setLastTickUse();
            toad.clearOrder();
        }
    }
}
