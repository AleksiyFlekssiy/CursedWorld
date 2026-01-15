package com.aleksiyflekssiy.tutorialmod.entity;

import com.aleksiyflekssiy.tutorialmod.entity.ai.GreatSerpentAI;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMemoryModuleTypes;
import com.aleksiyflekssiy.tutorialmod.entity.control.GreatSerpentMoveControl;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class GreatSerpentSegment extends PathfinderMob {
    public int index;
    private GreatSerpentEntity parent;
    private UUID parentUUID;
    protected static final ImmutableList<SensorType<? extends Sensor<? super GreatSerpentSegment>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_PLAYERS);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.WALK_TARGET, CustomMemoryModuleTypes.GRABBED_ENTITY.get(), MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);

    public GreatSerpentSegment(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.refreshDimensions();
        index = 0;
        parent = null;
        parentUUID = null;
        this.navigation = new FlyingPathNavigation(this, pLevel);
        this.moveControl = new GreatSerpentMoveControl(this);
    }

    public GreatSerpentSegment(EntityType<? extends PathfinderMob> pEntityType, Level pLevel, GreatSerpentEntity parent, int index) {
        super(pEntityType, pLevel);
        this.refreshDimensions();
        this.index = index;
        this.parent = parent;
        this.parentUUID = parent.getUUID();
        this.noPhysics = true;
        this.navigation = new FlyingPathNavigation(this, pLevel);
        this.moveControl = new GreatSerpentMoveControl(this);
    }

    public GreatSerpentEntity getParent() {
        return parent;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide() && parent == null && parentUUID != null){
            parent = (GreatSerpentEntity) ((ServerLevel) level()).getEntity(parentUUID);
        }
    }

    @Override
    protected void setRot(float pYRot, float pXRot) {
        super.setRot(pYRot, pXRot);
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
    public boolean canBeCollidedWith() {
        if (this.parent != null) return parent.canBeCollidedWith();
        return false;
    }

    public boolean isPushable() {
        if (this.parent != null) return parent.isPushable();
        return false;
    }

    @Override
    public boolean is(Entity pEntity) {
        return this.equals(pEntity) || parent.equals(pEntity);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 60)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.ATTACK_DAMAGE, 5f)
                .add(Attributes.FOLLOW_RANGE, 100)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 1)
                .add(Attributes.ARMOR_TOUGHNESS, 2.5)
                .add(Attributes.JUMP_STRENGTH, 1)
                .add(Attributes.FLYING_SPEED, 5);
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
