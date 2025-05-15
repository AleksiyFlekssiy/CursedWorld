package com.aleksiyflekssiy.tutorialmod.entity.behavior.toad;

import com.aleksiyflekssiy.tutorialmod.entity.ToadEntity;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMemoryModuleTypes;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

import java.util.Map;

public class TonguePull extends Behavior<ToadEntity> {
    private LivingEntity caughtEntity;
    private byte durationTicks = 0;

    public TonguePull(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition) {
        super(pEntryCondition, 0, 72000);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, ToadEntity toad) {
        boolean bool = false;
        if (toad.getBrain().hasMemoryValue(CustomMemoryModuleTypes.GRABBED_ENTITY.get())) {
            LivingEntity target = toad.getBrain().getMemory(CustomMemoryModuleTypes.GRABBED_ENTITY.get()).get();
            if (toad.getOrder() == ToadEntity.ToadOrder.PULL) bool = true;
            else bool = toad.isCooldownOff() && toad.getBrain().getMemory(CustomMemoryModuleTypes.ATTACK_TYPE.get()).get().equals("PULL");
        }
        System.out.println("CHECK PULL: " + bool);
        return bool;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, ToadEntity toad, long pGameTime) {
        if (caughtEntity != null && !caughtEntity.isSpectator()) {
            if (!toad.isTamed()) return toad.distanceTo(caughtEntity) > 1.0 && durationTicks <= 60; // Продолжаем, пока цель дальше 1 блока
            else {
                if (toad.getOrder() == ToadEntity.ToadOrder.NONE){
                    return toad.distanceTo(caughtEntity) > 1.0 && durationTicks <= 60;
                }
                else return toad.getOrder() == ToadEntity.ToadOrder.PULL;
            }
        }
        return false;
    }

    @Override
    protected void start(ServerLevel level, ToadEntity toad, long pGameTime) {
        toad.setLastTickUse();
        System.out.println("START PULL");
        caughtEntity = toad.getBrain().getMemory(CustomMemoryModuleTypes.GRABBED_ENTITY.get()).get();
    }

    @Override
    protected void tick(ServerLevel level, ToadEntity toad, long pGameTime) {
        if (caughtEntity == null) return;
        toad.setLastTickUse();
        // Целевая позиция: 1 блок от жабы в направлении взгляда
        Vec3 targetPos = toad.position().add(toad.getLookAngle().scale(2));
        Vec3 currentPos = caughtEntity.position();

        // Вектор притягивания
        Vec3 pullVector = targetPos.subtract(currentPos);
        double distance = pullVector.lengthSqr();
        toad.setDistance((float) distance);
        if (distance > 1) { // Если цель дальше 1 блока
            // Нормализуем вектор и задаём скорость притягивания
            double speed = 5;
            if (distance < 3) {
                speed = 0; // Замедление перед остановкой
                caughtEntity.fallDistance = 0;
            } else if (distance < 5) speed *= 0.5;
            Vec3 normalizedPull = pullVector.normalize(); // Скорость 0.5 блока/тик

            caughtEntity.setDeltaMovement(normalizedPull.scale(speed));
            caughtEntity.hurtMarked = true; // Обновляем движение

            toad.lookAt(EntityAnchorArgument.Anchor.EYES, caughtEntity.position());
            System.out.println("PULL: " + caughtEntity.getClass().getSimpleName() + " Distance: " + distance);
            caughtEntity.hasImpulse = false;
        } else {
            // Если цель уже близко, останавливаем её
            caughtEntity.setDeltaMovement(Vec3.ZERO);
            caughtEntity.hurtMarked = true;
            System.out.println("Positioned");
            stop(level, toad, pGameTime);
        }
        durationTicks++;
    }

    @Override
    protected void stop(ServerLevel pLevel, ToadEntity toad, long pGameTime) {
        if (caughtEntity == null) return;
        System.out.println("STOP PULL");
        toad.setDistance(0);
        durationTicks = 0;
        caughtEntity = null;
        toad.setLastTickUse();
        toad.getBrain().eraseMemory(CustomMemoryModuleTypes.GRABBED_ENTITY.get());
    }
}
