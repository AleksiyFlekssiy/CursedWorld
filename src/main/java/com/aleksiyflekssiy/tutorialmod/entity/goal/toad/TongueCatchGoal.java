package com.aleksiyflekssiy.tutorialmod.entity.goal.toad;

import com.aleksiyflekssiy.tutorialmod.entity.ToadEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.EnumSet;

public class TongueCatchGoal extends Goal {
    public static final float IMMOBILIZATION_TICKS = 60F;
    private final ToadEntity toad;
    private LivingEntity caughtEntity;
    private long lastUseTime = 0;
    private int catchTick = 0;
    private float initialSpeed;

    public TongueCatchGoal(ToadEntity toad) {
        this.toad = toad;
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public boolean canUse() {
        if (toad.getTarget() != null && !toad.getTarget().isSpectator()) {
            if (!toad.isTamed()) return toad.isCooldownOff(); // 30 блоков в квадрате
            else return toad.getOrder() == ToadEntity.ToadOrder.NONE || toad.getOrder() == ToadEntity.ToadOrder.IMMOBILIZE;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return caughtEntity != null && !caughtEntity.isSpectator() && catchTick <= IMMOBILIZATION_TICKS; // Продолжаем, пока цель дальше 1 блока
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
            toad.lookAt(EntityAnchorArgument.Anchor.FEET, caughtEntity.position());
            initialSpeed = (float) caughtEntity.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();
            caughtEntity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        }
    }

    @Override
    public void tick() {
        if (toad.level().isClientSide() || caughtEntity == null) return;
        toad.lookAt(EntityAnchorArgument.Anchor.EYES, caughtEntity.position());
        System.out.println("Tick: " + caughtEntity.getClass().getSimpleName());
        catchTick++;
    }

    @SubscribeEvent
    public void disableMovement(LivingEvent.LivingJumpEvent event) {
        //Вся система - ебучий костыль.
        //Необходимо написать систему управления вводом игрока
        if (event.getEntity().equals(caughtEntity)) {
            event.getEntity().setDeltaMovement(0, 0, 0);
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
        lastUseTime = toad.level().getGameTime();
        caughtEntity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(initialSpeed);
        caughtEntity = null;
        catchTick = 0;
        toad.setLastTickUse();
        toad.clearOrder();
    }
}
