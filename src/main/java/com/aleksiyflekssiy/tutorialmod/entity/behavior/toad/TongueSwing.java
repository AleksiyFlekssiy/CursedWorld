package com.aleksiyflekssiy.tutorialmod.entity.behavior.toad;

import com.aleksiyflekssiy.tutorialmod.entity.ToadEntity;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMemoryModuleTypes;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

import java.util.Map;

public class TongueSwing extends Behavior<ToadEntity> {
    private LivingEntity caughtEntity;
    private int swingTick = 0;
    private final int SWING_DURATION = 80; // 2 секунды
    private final float SWING_SPEED = 0.25F; // Уменьшена угловая скорость
    private Vec3 centerPosition; // Центр вращения (позиция лягушки)
    private float angle = 0.0F; // Текущий угол
    private boolean isReleased = false;
    private float initialDistance = 0.0F; // Изначальная дистанция до цели
    private int releaseTick = 0;

    public TongueSwing(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition) {
        super(pEntryCondition, 0, 72000);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, ToadEntity toad) {
        boolean bool = false;
        if (toad.getBrain().hasMemoryValue(CustomMemoryModuleTypes.GRABBED_ENTITY.get())) {
            LivingEntity target = toad.getBrain().getMemory(CustomMemoryModuleTypes.GRABBED_ENTITY.get()).get();
            if (toad.getOrder() == ToadEntity.ToadOrder.SWING) bool = true;
            else bool = toad.isCooldownOff() && toad.getBrain().getMemory(CustomMemoryModuleTypes.ATTACK_TYPE.get()).get().equals("SWING");
        }
        System.out.println("CHECK SWING: " + bool);
        return bool;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, ToadEntity toad, long pGameTime) {
        if (caughtEntity != null && caughtEntity.isAlive() && releaseTick < 60){
            return toad.getOrder() == ToadEntity.ToadOrder.NONE || toad.getOrder() == ToadEntity.ToadOrder.SWING;
        }
        return false;
    }

    @Override
    protected void start(ServerLevel level, ToadEntity toad, long pGameTime) {
        toad.setLastTickUse();
                caughtEntity = toad.getBrain().getMemory(CustomMemoryModuleTypes.GRABBED_ENTITY.get()).get();
                centerPosition = toad.position(); // Центр вращения
                initialDistance = (float) centerPosition.distanceTo(caughtEntity.position()); // Изначальная дистанция
                angle = (float) Math.atan2(caughtEntity.getZ() - centerPosition.z, caughtEntity.getX() - centerPosition.x);
                swingTick = 0;
                toad.setDistance(initialDistance);
                System.out.println("START SWING");
    }


    @Override
    protected void tick(ServerLevel level, ToadEntity toad, long pGameTime) {
        if (caughtEntity == null) return;
        if (!level.isClientSide()) { // Только сервер
            toad.setLastTickUse();
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


                swingTick++;
                if (swingTick >= SWING_DURATION) {
                    releaseEntity(toad);
                }
            } else {
                if (releaseTick >= 60) {
                    this.stop(level, toad, pGameTime);
                    return;
                } else releaseTick++;
            }

            System.out.println("SWING: " + caughtEntity.getClass().getSimpleName() +
                    " Speed: " + caughtEntity.getDeltaMovement().length() +
                    " Distance: " + centerPosition.distanceTo(caughtEntity.position()));
            toad.setLastTickUse();
        }
    }

    private void releaseEntity(ToadEntity toad) {
        if (caughtEntity == null) return;

        float releaseSpeed = SWING_SPEED * initialDistance * 1.5F;
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
    protected void stop(ServerLevel level, ToadEntity toad, long pGameTime) {
        if (caughtEntity != null) {
            System.out.println("STOP SWING");
            caughtEntity = null;
            isReleased = false;
            releaseTick = 0;
            toad.setLastTickUse();
            toad.getBrain().eraseMemory(CustomMemoryModuleTypes.GRABBED_ENTITY.get());
        }
    }
}
