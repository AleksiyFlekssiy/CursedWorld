package com.aleksiyflekssiy.tutorialmod.entity;

import com.aleksiyflekssiy.tutorialmod.client.model.NueAnimations;
import com.aleksiyflekssiy.tutorialmod.entity.ai.NueAI;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMemoryModuleTypes;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomSensorTypes;
import com.aleksiyflekssiy.tutorialmod.entity.navigation.FlyingMoveControl;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class NueEntity extends Shikigami {
    private LivingEntity grabbedEntity;
    private NueEntity.AttackPhase attackPhase = AttackPhase.ASCEND;
    public AnimationState idleAnimation = new AnimationState();
    public AnimationState flyAnimation = new AnimationState();
    protected static final ImmutableList<SensorType<? extends Sensor<? super NueEntity>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_PLAYERS, CustomSensorTypes.SHIKIGAMI_OWNER_HURT.get(), CustomSensorTypes.SHIKIGAMI_OWNER_HURT_BY.get());
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.WALK_TARGET, CustomMemoryModuleTypes.OWNER.get(), CustomMemoryModuleTypes.OWNER_HURT.get(), CustomMemoryModuleTypes.OWNER_HURT_BY_ENTITY.get());
    private static final EntityDataAccessor<Integer> ANIMATION = SynchedEntityData.defineId(NueEntity.class, EntityDataSerializers.INT);
    public static final int IDLE = 0;
    public static final int FLY = 1;
    private final EntityDimensions defaultDimensions = EntityDimensions.scalable(5F, 6F); // Обычный размер
    private final EntityDimensions flyingDimensions = EntityDimensions.scalable(4F, 5F); // Размер в полёте

    public NueEntity(EntityType<? extends Shikigami> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new FlyingMoveControl(this);
        this.navigation = new FlyingPathNavigation(this, level());
        this.entityData.set(ANIMATION, 0);

    }

    public enum AttackPhase {
        SWOOP,
        ASCEND
    }

    @Override
    public void tame(Player owner) {
        super.tame(owner);
        this.getBrain().setMemory(CustomMemoryModuleTypes.OWNER.get(), owner);
    }

    @Override
    protected Brain.Provider<NueEntity> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
        Brain<NueEntity> brain = this.brainProvider().makeBrain(pDynamic);
        return NueAI.makeBrain(brain); // Инициализируем через NueAI
    }

    @Override
    public Brain<NueEntity> getBrain() {
        return (Brain<NueEntity>) super.getBrain();
    }

    @Override
    protected void customServerAiStep() {
        this.getBrain().tick((ServerLevel)this.level(), this);
        NueAI.updateActivity(this.getBrain());
        super.customServerAiStep();
    }

    @Override
    public void followOrder(LivingEntity target, IOrder order) {
        if (order == NueOrder.SIT) return;
        super.followOrder(target, order);
        if (this.isTamed() && this.owner != null) {
            this.getBrain().stopAll((ServerLevel) this.level(), this);
            this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);
        }
    }

    @Override
    public void followOrder(BlockPos target, IOrder order) {
        if (order == NueOrder.SIT) {
            super.followOrder(target, order);
            if (this.isTamed() && this.owner != null) {
                this.getBrain().stopAll((ServerLevel) this.level(), this);
                this.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, 1, 1));
            }
        }
    }

    @Override
    public void clearOrder() {
        super.clearOrder();
        this.getBrain().stopAll((ServerLevel) this.level(), this);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.onGround()) {
                setAnimation(IDLE);
            } else {
                setAnimation(FLY);
            }
            if (this.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)){
                System.out.println("TARGET:" + this.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get().getTarget().currentPosition());
            }
            // Переключение активности
            if (this.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                this.getBrain().setActiveActivityIfPossible(Activity.FIGHT);
            } else {
              this.getBrain().setActiveActivityIfPossible(Activity.CORE);
            }
            this.getBrain().getRunningBehaviors().forEach(System.out::println);
        } else updateAnimation();
        this.refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return onGround() ? defaultDimensions : flyingDimensions;
    }

    @Override
    public void refreshDimensions() {
        super.refreshDimensions();
        this.setBoundingBox(getDimensions(this.getPose()).makeBoundingBox(this.position()));
    }

    private void updateAnimation() {
        int state = getAnimation();
        switch (state) {
            case IDLE -> idleAnimation.startIfStopped(this.tickCount);
            case FLY -> flyAnimation.startIfStopped(this.tickCount);
        }
    }

    public AnimationState getAnimationState(AnimationDefinition definition) {
        if (definition == NueAnimations.idle) return idleAnimation;
        if (definition == NueAnimations.fly) return flyAnimation;
        return idleAnimation; // По умолчанию
    }

    public void setAnimation(int animation) {
        this.entityData.set(ANIMATION, animation);
    }

    public int getAnimation() {
        return this.entityData.get(ANIMATION);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!player.level().isClientSide() && player.equals(this.owner)) {
            player.startRiding(this);
        }
        return InteractionResult.SUCCESS;
    }


    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (entity instanceof Player player) {
            return player;
        }
        return null;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ANIMATION, 0);
    }

    public void tryGrabEntityBelow() {
        if (this.level().isClientSide()) return;
        if (grabbedEntity == null) {
            // Определяем область поиска под сущностью
            double grabRange = 2.0; // Радиус захвата по горизонтали
            double grabHeight = 5.0; // Высота поиска вниз
            AABB grabBox = new AABB(
                    this.getX() - grabRange, this.getY() - grabHeight, this.getZ() - grabRange,
                    this.getX() + grabRange, this.getY(), this.getZ() + grabRange
            );

            // Ищем живые сущности в области
            List<LivingEntity> entitiesBelow = this.level().getEntitiesOfClass(
                    LivingEntity.class,
                    grabBox,
                    entity -> entity != this && entity != this.getControllingPassenger() && !entity.isPassenger()
            );

            // Если есть сущности, пытаемся схватить ближайшую
            if (!entitiesBelow.isEmpty()) {
                LivingEntity target = entitiesBelow.get(0); // Берём первую подходящую сущность
                if (this.getPassengers().isEmpty() || this.getPassengers().size() < 2) {
                    // Прикрепляем сущность как пассажира
                    boolean isWork = target.startRiding(this, true);
                    if (isWork) grabbedEntity = target;
                    this.getBrain().setMemory(CustomMemoryModuleTypes.GRABBED_ENTITY.get(), grabbedEntity);
                    this.playSound(SoundEvents.PHANTOM_BITE, 1.0F, 1.0F); // Звук захвата
                }
            }
        } else {
            dropGrabbedEntity();
        }
    }

    public void dropGrabbedEntity() {
        if (grabbedEntity != null) {
            grabbedEntity.stopRiding();
            grabbedEntity = null;
            this.getBrain().eraseMemory(CustomMemoryModuleTypes.GRABBED_ENTITY.get());
        }
    }

    protected void tickRidden(Player pPlayer, Vec3 pTravelVector) {
        super.tickRidden(pPlayer, pTravelVector);
        this.setRot(pPlayer.getYRot(), pPlayer.getXRot() * 0.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
//        if (!onGround()) {
//            List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.5));
//            entities.forEach(entity -> {
//                if (!entity.equals(this) && !entity.equals(grabbedEntity) && !entity.equals(getControllingPassenger()))
//                    this.doHurtTarget(entity);
//            });
//        }
    }

    protected Vec3 getRiddenInput(Player player, Vec3 travelVector) {
       return super.getRiddenInput(player, travelVector);
    }

    protected float getRiddenSpeed(Player pPlayer) {
        return (float) (this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.5);
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {
        int passengerIndex = this.getPassengers().indexOf(passenger);
        if (passengerIndex == 0) { // Первый пассажир (игрок)
            super.positionRider(passenger, callback);
        } else { // Схваченная сущность
            double nueHeight = this.getBbHeight(); // Высота NueEntity
            double offsetY = -nueHeight / 2; // Смещение до нижней границы NueEntity
            callback.accept(passenger, this.getX(), this.getY() + offsetY, this.getZ());
        }
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

    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
        //Nue is unable to be damaged by falling
    }

    public void travel(Vec3 pTravelVector) {
        if (this.isControlledByLocalInstance()) {
            if (this.isInWater()) {
                this.moveRelative(0.02F, pTravelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale((double) 0.8F));
            } else if (this.isInLava()) {
                this.moveRelative(0.02F, pTravelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
            } else {
                BlockPos ground = getBlockPosBelowThatAffectsMyMovement();
                float f = 0.91F;
                if (this.onGround()) {
                    f = this.level().getBlockState(ground).getFriction(this.level(), ground, this) * 0.91F;
                }

                float f1 = 0.16277137F / (f * f * f);
                f = 0.91F;
                if (this.onGround()) {
                    f = this.level().getBlockState(ground).getFriction(this.level(), ground, this) * 0.91F;
                }

                this.moveRelative(this.onGround() ? 0.1F * f1 : 0.02F, pTravelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());

                if (this.onGround()) {
                    this.setDeltaMovement(this.getDeltaMovement().scale((double) f));
                }
            }
        }

        this.calculateEntityAnimation(false);
    }

    public boolean onClimbable() {
        return false;
    }

    public AttackPhase getAttackPhase() {
        return attackPhase;
    }

    public void setAttackPhase(AttackPhase attackPhase) {
        this.attackPhase = attackPhase;
    }

    public enum NueOrder implements IOrder{
        NONE,
        ATTACK,
        GRAB,
        SIT
    }
}

