package com.aleksiyflekssiy.tutorialmod.entity;

import com.aleksiyflekssiy.tutorialmod.entity.ai.DivineDogAI;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMemoryModuleTypes;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomSensorTypes;
import com.aleksiyflekssiy.tutorialmod.entity.navigation.CustomGroundNavigation;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class DivineDogEntity extends Shikigami{

    public enum Color{
        WHITE,
        BLACK
    }

    public float targetYaw = 0.0F;
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.WALK_TARGET, CustomMemoryModuleTypes.OWNER.get(), CustomMemoryModuleTypes.OWNER_HURT.get(), CustomMemoryModuleTypes.OWNER_HURT_BY_ENTITY.get(), MemoryModuleType.LOOK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    protected static final ImmutableList<SensorType<? extends Sensor<? super DivineDogEntity>>> SENSOR_TYPES = ImmutableList.of(CustomSensorTypes.SHIKIGAMI_OWNER_HURT.get(), CustomSensorTypes.SHIKIGAMI_OWNER_HURT_BY.get(), SensorType.NEAREST_LIVING_ENTITIES);
    private static final EntityDataAccessor<Float> REAL_SPEED = SynchedEntityData.defineId(DivineDogEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(DivineDogEntity.class, EntityDataSerializers.INT);

    public DivineDogEntity(EntityType<? extends Shikigami> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        //this.moveControl = new CustomMoveControl(this, this);
        this.entityData.set(REAL_SPEED, 0.33F);
        this.setMaxUpStep(1);
        entityData.set(COLOR, Color.WHITE.ordinal());
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (getBrain().getMemory(MemoryModuleType.WALK_TARGET).isPresent()) entityData.set(REAL_SPEED, 0.33f * 2.5f);
            else entityData.set(REAL_SPEED, 0.33F);
        }
    }

    @Override
    public Brain<DivineDogEntity> getBrain() {
        return (Brain<DivineDogEntity>) super.getBrain();
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
        Brain<DivineDogEntity> brain = this.brainProvider().makeBrain(pDynamic);
        return DivineDogAI.makeBrain(brain); // Инициализируем через NueAI
    }

    @Override
    protected Brain.Provider<DivineDogEntity> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        DivineDogAI.updateActivity(this.getBrain());
        this.getBrain().tick((ServerLevel)this.level(), this);
    }

    @Override
    public boolean followOrder(LivingEntity target, BlockPos blockPos, IOrder order) {
        if (super.followOrder(target, blockPos, order)) {
            this.getBrain().stopAll((ServerLevel) this.level(), this);
            if (order == DivineDogOrder.NONE) {}
            else if (order == DivineDogOrder.ATTACK) this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);
            else if (order == DivineDogOrder.MOVE) this.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockPos, 1, 1));
            return true;
        }
        return false;
    }

    @Override
    public void clearOrder() {
        this.setOrder(DivineDogOrder.NONE);
        this.getBrain().stopAll((ServerLevel) this.level(), this);
    }

    @Override
    public void tame(Player owner) {
        super.tame(owner);
        this.getBrain().setMemory(CustomMemoryModuleTypes.OWNER.get(), owner);
    }

    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        return new CustomGroundNavigation(this, level());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(REAL_SPEED, 0.7F);
        entityData.define(COLOR, Color.WHITE.ordinal());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("color", this.entityData.get(COLOR));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(COLOR, tag.getInt("color"));
    }

    public float getRealSpeed() {
        return entityData.get(REAL_SPEED);
    }

    public void setColor(Color color){
        this.entityData.set(COLOR, color.ordinal());
    }

    public Color getColor(){
        return Color.values()[entityData.get(COLOR)];
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        if (fallDistance <= 5.0F) { // Безопасная высота 10 блоков
            this.fallDistance = 0.0F; // Сбрасываем дистанцию падения
            return false; // Отменяем урон
        }
        // Для падений больше 10 блоков считаем урон как обычно, но с вычетом 10 блоков
        float reducedDistance = fallDistance - 5.0F;
        if (reducedDistance > 0) {
            return super.causeFallDamage(reducedDistance, damageMultiplier, damageSource);
        }
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 60)
                .add(Attributes.MOVEMENT_SPEED, 0.33)
                .add(Attributes.ATTACK_DAMAGE, 5f)
                .add(Attributes.FOLLOW_RANGE, 100)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 1)
                .add(Attributes.ARMOR_TOUGHNESS, 2.5)
                .add(Attributes.JUMP_STRENGTH, 1);
    }

    public enum DivineDogOrder implements IOrder{
        NONE,
        ATTACK,
        MOVE
    }
}