package com.aleksiyflekssiy.tutorialmod.entity.goal.toad;

import com.aleksiyflekssiy.tutorialmod.entity.ToadEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class TonguePullGoal extends Goal {
    private final ToadEntity toad;
    private LivingEntity caughtEntity;

    public TonguePullGoal(ToadEntity toad) {
        this.toad = toad;
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (toad.getTarget() != null && !toad.getTarget().isSpectator()) {
            if (!toad.isTamed())
                return toad.isCooldownOff() && toad.distanceTo(toad.getTarget()) > 20; // 30 блоков в квадрате
            else return toad.getOrder() == ToadEntity.Order.NONE || toad.getOrder() == ToadEntity.Order.PULL;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return caughtEntity != null && !caughtEntity.isSpectator() && toad.distanceTo(caughtEntity) > 1.0; // Продолжаем, пока цель дальше 1 блока
    }

    @Override
    public void start() {
        System.out.println("Start");
        toad.getNavigation().stop(); // Останавливаем движение жабы
        toad.lookAt(EntityAnchorArgument.Anchor.EYES, toad.getTarget().position());
        Vec3 startPos = toad.getEyePosition(); // Позиция глаз жабы
        Vec3 endPos = startPos.add(toad.getViewVector(1).scale(50)); // Дальность языка (30 блоков)
        EntityHitResult result = ProjectileUtil.getEntityHitResult(toad.level(), toad, startPos, endPos, new AABB(startPos, endPos), entity -> entity instanceof LivingEntity && !entity.equals(toad));

        if (result != null && result.getEntity() instanceof LivingEntity entity) {
            caughtEntity = entity;
            toad.setDistance((float) toad.position().subtract(caughtEntity.position()).length());
        }
    }

    @Override
    public void tick() {
        if (caughtEntity == null) return;

        // Целевая позиция: 1 блок от жабы в направлении взгляда
        Vec3 targetPos = toad.position().add(toad.getLookAngle().scale(2));
        Vec3 currentPos = caughtEntity.position();

        // Вектор притягивания
        Vec3 pullVector = targetPos.subtract(currentPos);
        double distance = pullVector.length();
        toad.setDistance((float) distance);
        if (distance > 1) { // Если цель дальше 1 блока
            // Нормализуем вектор и задаём скорость притягивания
            double speed = 5;
            if (distance < 3.0) {
                speed = 0; // Замедление перед остановкой
                caughtEntity.fallDistance = 0;
            } else if (distance < 5) speed *= 0.5;
            Vec3 normalizedPull = pullVector.normalize(); // Скорость 0.5 блока/тик

            caughtEntity.setDeltaMovement(normalizedPull.scale(speed));
            caughtEntity.hurtMarked = true; // Обновляем движение

            toad.lookAt(EntityAnchorArgument.Anchor.EYES, caughtEntity.position());
            System.out.println("Tick: " + caughtEntity.getClass().getSimpleName());
            caughtEntity.hasImpulse = false;
        } else {
            // Если цель уже близко, останавливаем её
            caughtEntity.setDeltaMovement(Vec3.ZERO);
            caughtEntity.hurtMarked = true;
            System.out.println("Positioned");
            stop();
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true; // Обновляем каждый тик
    }

    @Override
    public void stop() {
        if (caughtEntity == null) return;
        System.out.println("Stop");
        toad.setDistance(0);
        caughtEntity = null;
        toad.setLastTickUse();
        toad.clearOrder();
    }
}
