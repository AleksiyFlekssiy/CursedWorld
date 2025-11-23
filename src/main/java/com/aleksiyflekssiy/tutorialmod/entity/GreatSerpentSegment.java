package com.aleksiyflekssiy.tutorialmod.entity;

import com.aleksiyflekssiy.tutorialmod.entity.ai.GreatSerpentAI;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMemoryModuleTypes;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomSensorTypes;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class GreatSerpentSegment extends Mob {
    public int index;
    private GreatSerpentEntity parent;
    private UUID parentUUID;
    protected static final ImmutableList<SensorType<? extends Sensor<? super GreatSerpentSegment>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_PLAYERS, CustomSensorTypes.SHIKIGAMI_OWNER_HURT.get(), CustomSensorTypes.SHIKIGAMI_OWNER_HURT_BY.get());
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.WALK_TARGET, CustomMemoryModuleTypes.OWNER.get(), CustomMemoryModuleTypes.OWNER_HURT.get(), CustomMemoryModuleTypes.OWNER_HURT_BY_ENTITY.get(), CustomMemoryModuleTypes.GRABBED_ENTITY.get(), CustomMemoryModuleTypes.ATTACK_TYPE.get(), MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);

    public GreatSerpentSegment(EntityType<? extends Mob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        //this.noPhysics = true;
        this.refreshDimensions();
        index = 0;
        parent = null;
        parentUUID = null;
    }

    public GreatSerpentSegment(EntityType<? extends Mob> pEntityType, Level pLevel, GreatSerpentEntity parent, int index) {
        super(pEntityType, pLevel);
        //this.noPhysics = true;
        this.refreshDimensions();
        this.parent = parent;
        this.parentUUID = parent.getUUID();
        this.index = index;
    }

    public void moveToTarget() {
        this.getBrain().getMemory(MemoryModuleType.WALK_TARGET).ifPresent(walkTarget -> {
            BlockPos targetPos = walkTarget.getTarget().currentBlockPosition();
            double speed = this.getAttributeValue(Attributes.MOVEMENT_SPEED);
            Vec3 currentPos = this.position();
            Vec3 targetVec = new Vec3(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
            Vec3 direction = targetVec.subtract(currentPos).normalize();
            Vec3 motionVec = direction.scale(speed);
            this.setDeltaMovement(motionVec);
        });
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide() && parent == null && parentUUID != null){
            parent = (GreatSerpentEntity) ((ServerLevel) level()).getEntity(parentUUID);
        }
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(2.0F, 2.0F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.parent != null) this.parent.hurt(source, amount);
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected int calculateFallDamage(float pFallDistance, float pDamageMultiplier) {
        return 0;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean is(Entity pEntity) {
        return this.equals(pEntity) || parent.equals(pEntity);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 60)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 5f)
                .add(Attributes.FOLLOW_RANGE, 100)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 1)
                .add(Attributes.ARMOR_TOUGHNESS, 2.5)
                .add(Attributes.JUMP_STRENGTH, 1);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("index", index);
        tag.putUUID("parentUUID", parent.getUUID());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.index = tag.getInt("index");
        this.parentUUID = tag.getUUID("parentUUID");
    }

    @Override
    protected Brain.Provider<GreatSerpentSegment> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
        Brain<GreatSerpentSegment> brain = this.brainProvider().makeBrain(pDynamic);
        return GreatSerpentAI.makeSegmentBrain(brain);
    }

    @Override
    public Brain<GreatSerpentSegment> getBrain() {
        return (Brain<GreatSerpentSegment>) super.getBrain();
    }

    @Override
    public void aiStep() {
        super.aiStep();
    }

    @Override
    protected void customServerAiStep() {
        this.getBrain().tick((ServerLevel)this.level(), this);
        super.customServerAiStep();
    }
}
