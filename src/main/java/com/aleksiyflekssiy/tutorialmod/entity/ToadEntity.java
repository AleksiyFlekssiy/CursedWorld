package com.aleksiyflekssiy.tutorialmod.entity;

import com.aleksiyflekssiy.tutorialmod.entity.ai.ToadAI;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMemoryModuleTypes;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomSensorTypes;
import com.aleksiyflekssiy.tutorialmod.entity.control.CustomBodyRotation;
import com.aleksiyflekssiy.tutorialmod.entity.control.CustomLookControl;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ToadEntity extends Shikigami {
    private static final EntityDataAccessor<Float> DISTANCE = SynchedEntityData.defineId(ToadEntity.class, EntityDataSerializers.FLOAT);
    public AnimationState mouthOpen = new AnimationState();
    public float targetYaw;
    private long lastTickUse;
    protected static final ImmutableList<SensorType<? extends Sensor<? super ToadEntity>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_PLAYERS, CustomSensorTypes.SHIKIGAMI_OWNER_HURT.get(), CustomSensorTypes.SHIKIGAMI_OWNER_HURT_BY.get());
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.WALK_TARGET, CustomMemoryModuleTypes.OWNER.get(), CustomMemoryModuleTypes.OWNER_HURT.get(), CustomMemoryModuleTypes.OWNER_HURT_BY_ENTITY.get(), CustomMemoryModuleTypes.GRABBED_ENTITY.get(), CustomMemoryModuleTypes.ATTACK_TYPE.get(), MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);

    public ToadEntity(EntityType<? extends Shikigami> entityType, Level level) {
        super(entityType, level);
        //this.moveControl = new JumpingMoveControl(this);
        this.lookControl = new CustomLookControl(this, true);
        setDistance(0);
        this.currentOrder = ToadOrder.NONE;
    }

    public ToadEntity(EntityType<? extends Shikigami> entityType, Level level, Player owner) {
        super(entityType, level, owner);
        //this.moveControl = new JumpingMoveControl(this);
        this.lookControl = new CustomLookControl(this, true);
        setDistance(0);
        this.currentOrder = ToadOrder.NONE;
    }

    @Override
    public int getMaxHeadYRot() {
        return 360;
    }

    @Override
    public int getMaxHeadXRot() {
        return 360;
    }

    @Override
    public int getHeadRotSpeed() {
        return 360;
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new CustomBodyRotation(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 100)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.ATTACK_DAMAGE, 10f)
                .add(Attributes.FOLLOW_RANGE, 50)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 2.5)
                .add(Attributes.ARMOR_TOUGHNESS, 2.5)
                .add(Attributes.JUMP_STRENGTH, 1);
    }

    @Override
    public void tame(Player owner) {
        super.tame(owner);
        this.getBrain().setMemory(CustomMemoryModuleTypes.OWNER.get(), owner);
    }

    @Override
    protected Brain.Provider<ToadEntity> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
        Brain<ToadEntity> brain = this.brainProvider().makeBrain(pDynamic);
        return ToadAI.makeBrain(brain); // Инициализируем через NueAI
    }

    @Override
    public Brain<ToadEntity> getBrain() {
        return (Brain<ToadEntity>) super.getBrain();
    }

    @Override
    protected void customServerAiStep() {
        this.getBrain().tick((ServerLevel)this.level(), this);
        ToadAI.updateActivity(this.getBrain());
        super.customServerAiStep();
    }

    @Override
    public boolean followOrder(LivingEntity target, BlockPos blockPos, IOrder order) {
        if (super.followOrder(target, blockPos, order)) {
            this.getBrain().stopAll((ServerLevel) this.level(), this);
            if (order == ToadOrder.NONE) {}
            else if (order == ToadOrder.PULL || order == ToadOrder.SWING || order == ToadOrder.IMMOBILIZE) this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);
            return true;
        }
        return false;
    }

    @Override
    public void clearOrder() {
        this.setOrder(ToadOrder.NONE);
        this.getBrain().stopAll((ServerLevel) this.level(), this);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DISTANCE, 0F);
    }

    public float getDistance() {
        return this.entityData.get(DISTANCE);
    }

    public void setDistance(float distance) {
        this.entityData.set(DISTANCE, distance);
    }

    public void setLastTickUse() {
        this.lastTickUse = level().getGameTime();
    }
    //Нужно поменять на память с КД
    public boolean isCooldownOff(){
        return this.level().getGameTime() - this.lastTickUse > 100;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                this.getBrain().setActiveActivityIfPossible(Activity.FIGHT);
            } else {
                this.getBrain().setActiveActivityIfPossible(Activity.IDLE);
            }
        }
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

    protected Vec3 getRiddenInput(Player player, Vec3 travelVector) {
        return super.getRiddenInput(player, travelVector);
    }

    protected float getRiddenSpeed(Player pPlayer) {
        return (float)(this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.5);
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {
        callback.accept(passenger, this.getX(), this.getY() + this.getBbHeight() / 2, this.getZ());
    }

    protected void tickRidden(Player pPlayer, Vec3 pTravelVector) {
        super.tickRidden(pPlayer, pTravelVector);
        this.setRot(pPlayer.getYRot(), pPlayer.getXRot() * 0.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
    }

    public enum ToadOrder implements IOrder{
        NONE,
        PULL,
        SWING,
        IMMOBILIZE,
        MOVE
    }
}
