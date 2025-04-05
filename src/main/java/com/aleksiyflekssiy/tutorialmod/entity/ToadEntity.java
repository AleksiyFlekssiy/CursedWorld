package com.aleksiyflekssiy.tutorialmod.entity;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class ToadEntity extends Shikigami {
    private static final EntityDataAccessor<Float> DISTANCE = SynchedEntityData.defineId(ToadEntity.class, EntityDataSerializers.FLOAT);
    public AnimationState mouthOpen = new AnimationState();
    float targetYaw;

    public ToadEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new JumpingMoveControl(this);
        setDistance(0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 100)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.ATTACK_DAMAGE, 10f)
                .add(Attributes.FOLLOW_RANGE, 100)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 2.5)
                .add(Attributes.ARMOR_TOUGHNESS, 2.5)
                .add(Attributes.JUMP_STRENGTH, 1);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DISTANCE, 0F);
    }

    public float getDistance() {
        return this.entityData.get(DISTANCE);
    }

    private void setDistance(float distance) {
        this.entityData.set(DISTANCE, distance);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new TongueSwingGoal(this));
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, LivingEntity.class, false));
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity target = this.level().getNearestPlayer(this, 20);
        if (target == null) return;
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
    }

    protected int getJumpDelay() {
        return this.random.nextInt(20) + 10;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!player.level().isClientSide()) {
            player.startRiding(this);
        }
        return InteractionResult.SUCCESS;
    }


    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (entity instanceof Player player) {
            mouthOpen.startIfStopped(tickCount);
            return player;
        }
        mouthOpen.stop();
        return null;
    }

    static class TonguePullGoal extends Goal {
        public static final float ANIMATION_DELAY_TICKS = 20F;
        private final ToadEntity toad;
        private LivingEntity catchedEntity;
        private long lastUseTime = 0;
        private int catchTick = 0;

        public TonguePullGoal(ToadEntity toad) {
            this.toad = toad;
            this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            long time = toad.level().getGameTime();
            if (time - lastUseTime < 40) return false; // Кулдаун 2 секунды (40 тиков)
            return toad.getTarget() != null && !toad.getTarget().isSpectator() && toad.distanceToSqr(toad.getTarget()) < 900; // 30 блоков в квадрате
        }

        @Override
        public boolean canContinueToUse() {
            return catchedEntity != null && !catchedEntity.isSpectator() && toad.distanceToSqr(catchedEntity) > 1.0; // Продолжаем, пока цель дальше 1 блока
        }

        @Override
        public void start() {
            System.out.println("Start");
            toad.navigation.stop(); // Останавливаем движение жабы
            Vec3 startPos = toad.getEyePosition(); // Позиция глаз жабы
            Vec3 endPos = startPos.add(toad.getViewVector(1).scale(50)); // Дальность языка (30 блоков)
            EntityHitResult result = ProjectileUtil.getEntityHitResult(toad.level(), toad, startPos, endPos, new AABB(startPos, endPos), entity -> entity instanceof LivingEntity && !entity.equals(toad));

            if (result != null && result.getEntity() instanceof LivingEntity entity) {
                catchedEntity = entity;
                toad.setDistance((float) toad.position().subtract(catchedEntity.position()).length());
                toad.lookAt(EntityAnchorArgument.Anchor.FEET, catchedEntity.position());
            }
        }

        @Override
        public void tick() {
            if (catchedEntity == null) return;

            // Целевая позиция: 1 блок от жабы в направлении взгляда
            Vec3 targetPos = toad.position().add(toad.getLookAngle().scale(2));
            Vec3 currentPos = catchedEntity.position();

            // Вектор притягивания
            Vec3 pullVector = targetPos.subtract(currentPos);
            double distance = pullVector.length();
            toad.setDistance((float) distance);
            if (distance > 1) { // Если цель дальше 1 блока
                // Нормализуем вектор и задаём скорость притягивания
                double speed = 5;
                if (distance < 3.0) {
                    speed = 0; // Замедление перед остановкой
                } else if (distance < 5) speed *= 0.5;
                Vec3 normalizedPull = pullVector.normalize(); // Скорость 0.5 блока/тик

                catchedEntity.setDeltaMovement(normalizedPull.scale(speed));
                catchedEntity.hurtMarked = true; // Обновляем движение

                toad.lookAt(EntityAnchorArgument.Anchor.FEET, catchedEntity.position());
                System.out.println("Tick: " + catchedEntity.getClass().getSimpleName());
                catchTick++;
                catchedEntity.hasImpulse = false;
            } else {
                // Если цель уже близко, останавливаем её
                catchedEntity.setDeltaMovement(Vec3.ZERO);
                catchedEntity.setPos(targetPos.x, targetPos.y, targetPos.z);
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
            if (catchedEntity == null) return;
            System.out.println("Stop");
            toad.setDistance(0);
            lastUseTime = toad.level().getGameTime();
            catchedEntity = null;
            catchTick = 0;
        }
    }

    static class TongueCatchGoal extends Goal {
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
            long time = toad.level().getGameTime();
            if (time - lastUseTime < 40) return false; // Кулдаун 2 секунды (40 тиков)
            return toad.getTarget() != null && !toad.getTarget().isSpectator(); // 30 блоков в квадрате
        }

        @Override
        public boolean canContinueToUse() {
            return caughtEntity != null && !caughtEntity.isSpectator() && catchTick <= IMMOBILIZATION_TICKS; // Продолжаем, пока цель дальше 1 блока
        }

        @Override
        public void start() {
            System.out.println("Start");
            toad.navigation.stop(); // Останавливаем движение жабы
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
            toad.lookAt(EntityAnchorArgument.Anchor.FEET, caughtEntity.position());
            System.out.println("Tick: " + caughtEntity.getClass().getSimpleName());
            catchTick++;
        }

        @SubscribeEvent
        public void disableMovement(LivingEvent.LivingJumpEvent event){
            //Вся система - ебучий костыль.
            //Необходимо написать систему управления вводом игрока
            if (event.getEntity().equals(caughtEntity)) {
                event.getEntity().setDeltaMovement(0,0,0);
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
        }
    }

    public class TongueSwingGoal extends Goal {
        private final ToadEntity toad;
        private LivingEntity caughtEntity;
        private long lastUseTime = 0;
        private int swingTick = 0;
        private final int SWING_DURATION = 40; // 2 секунды
        private final float SWING_RADIUS = 3.0F; // Радиус вращения
        private final float SWING_SPEED = 0.2F; // Скорость вращения (радианы/тик)
        private Vec3 centerPosition; // Центр вращения (позиция лягушки)
        private float angle = 0.0F; // Текущий угол
        private float initialSpeed = 0.1F; // Исходная скорость цели

        public TongueSwingGoal(ToadEntity toad) {
            this.toad = toad;
            this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            long time = toad.level().getGameTime();
            if (time - lastUseTime < 60) return false; // Кулдаун 3 секунды
            return toad.getTarget() != null && !toad.getTarget().isSpectator();
        }

        @Override
        public boolean canContinueToUse() {
            return caughtEntity != null && caughtEntity.isAlive() && swingTick <= SWING_DURATION;
        }

        @Override
        public void start() {
            toad.navigation.stop();
            Vec3 startPos = toad.getEyePosition();
            Vec3 endPos = startPos.add(toad.getViewVector(1).scale(50));
            EntityHitResult result = ProjectileUtil.getEntityHitResult(
                    toad.level(), toad, startPos, endPos,
                    new AABB(startPos, endPos),
                    entity -> entity instanceof LivingEntity && !entity.equals(toad)
            );

            if (result != null && result.getEntity() instanceof LivingEntity entity) {
                caughtEntity = entity;
                centerPosition = toad.position(); // Центр вращения — позиция лягушки
                angle = (float) Math.atan2(caughtEntity.getZ() - centerPosition.z, caughtEntity.getX() - centerPosition.x);
                toad.lookAt(EntityAnchorArgument.Anchor.FEET, caughtEntity.position());

                // Отключаем гравитацию и фиксируем скорость
                caughtEntity.setNoGravity(true);
                AttributeInstance speedAttribute = caughtEntity.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speedAttribute != null) {
                    initialSpeed = (float) speedAttribute.getBaseValue();
                    speedAttribute.setBaseValue(0.0); // Блокируем движение цели
                }
            }
        }

        @Override
        public void tick() {
            if (caughtEntity == null) {
                // Отслеживание столкновений после отпускания
                LivingEntity target = toad.getTarget();
                if (target != null && (target.horizontalCollision || target.verticalCollision)) {
                    float speed = (float) target.getDeltaMovement().length();
                    if (speed > 0.5F) {
                        target.hurt(level().damageSources().generic(), speed * 2.0F);
                        target.setDeltaMovement(target.getDeltaMovement().scale(0.5));
                        toad.setTarget(null); // Сбрасываем цель после удара
                    }
                }
                return;
            }

            // Обновляем угол вращения
            angle += SWING_SPEED;

            // Вычисляем новую позицию цели
            double offsetX = SWING_RADIUS * Math.cos(angle);
            double offsetZ = SWING_RADIUS * Math.sin(angle);
            Vec3 newPos = centerPosition.add(offsetX, caughtEntity.getY() - centerPosition.y, offsetZ);

            // Перемещаем цель
            caughtEntity.teleportTo(newPos.x, newPos.y, newPos.z);
            caughtEntity.setDeltaMovement(Vec3.ZERO); // Обнуляем движение
            caughtEntity.hurtMarked = true;

            // Поворачиваем лягушку
            toad.setYRot((float) Math.toDegrees(angle));
            toad.yBodyRot = toad.getYRot();
            toad.lookAt(EntityAnchorArgument.Anchor.FEET, caughtEntity.position());

            swingTick++;

            // Отпускаем цель
            if (swingTick >= SWING_DURATION) {
                releaseEntity();
            }
        }

        private void releaseEntity() {
            if (caughtEntity == null) return;

            // Рассчитываем вектор скорости для отпускания
            float releaseSpeed = SWING_SPEED * SWING_RADIUS * 2.0F; // Увеличиваем импульс
            double velocityX = releaseSpeed * Math.cos(angle); // Направление наружу
            double velocityZ = releaseSpeed * Math.sin(angle);
            caughtEntity.setDeltaMovement(velocityX, 0.5, velocityZ);
            caughtEntity.hurtMarked = true;

            // Восстанавливаем гравитацию и скорость
            caughtEntity.setNoGravity(false);
            AttributeInstance speedAttribute = caughtEntity.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttribute != null) {
                speedAttribute.setBaseValue(initialSpeed);
            }

            // Переводим цель в статус "отпущенной"
            toad.setTarget(caughtEntity);
            caughtEntity = null;
            swingTick = 0;
            lastUseTime = toad.level().getGameTime();
        }

        @Override
        public void stop() {
            if (caughtEntity != null) {
                releaseEntity();
            }
        }
    }


    static class JumpingMoveControl extends MoveControl {
        private final ToadEntity toad;
        private float yRot;
        private int jumpDelay;
        private boolean isAggressive;
        private float jumpHeight = 0.5F;

        public JumpingMoveControl(ToadEntity toad) {
            super(toad);
            this.toad = toad;
            this.yRot = 180.0F * toad.getYRot() / (float) Math.PI;
        }

        public void setDirection(float pYRot, boolean pAggressive) {
            this.yRot = pYRot;
            this.isAggressive = pAggressive;
        }

        public void setWantedMovement(double pSpeed) {
            this.speedModifier = pSpeed;
            this.operation = MoveControl.Operation.MOVE_TO;
        }

        public void tick() {
            this.setDirection(this.toad.targetYaw, true);
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), this.yRot, 90.0F));
            this.mob.yHeadRot = this.mob.getYRot();
            this.mob.yBodyRot = this.mob.getYRot();
            if (this.operation != MoveControl.Operation.MOVE_TO) {
                this.mob.setZza(0.0F);
            } else {
                this.operation = MoveControl.Operation.WAIT;
                if (this.mob.onGround()) {
                    this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                    if (this.jumpDelay-- <= 0) {
                        this.jumpDelay = this.toad.getJumpDelay();
                        if (this.isAggressive) {
                            this.jumpDelay /= 3;
                        }
                        Vec3 direction = Vec3.ZERO;
                        if (toad.getTarget() != null)
                            direction = toad.getTarget().position().subtract(toad.position()).normalize();


                    } else {
                        this.toad.xxa = 0.0F;
                        this.toad.zza = 0.0F;
                        this.mob.setSpeed(0.0F);
                    }
                } else {
                    this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                }
            }
        }
    }

    static class ToadJumpControl extends JumpControl {

        public ToadJumpControl(Mob pMob) {
            super(pMob);
        }
    }

    static class ToadKeepOnJumpingGoal extends Goal {
        private final ToadEntity toad;

        public ToadKeepOnJumpingGoal(ToadEntity toad) {
            this.toad = toad;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            return !this.toad.isPassenger();
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            MoveControl movecontrol = this.toad.getMoveControl();
            if (movecontrol instanceof JumpingMoveControl control) {
                control.setWantedMovement(1.0D);
            }

        }
    }
}
