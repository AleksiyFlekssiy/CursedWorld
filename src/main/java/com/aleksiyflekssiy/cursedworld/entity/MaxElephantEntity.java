package com.aleksiyflekssiy.cursedworld.entity;

import com.aleksiyflekssiy.cursedworld.entity.ai.MaxElephantAI;
import com.aleksiyflekssiy.cursedworld.entity.ai.RabbitEscapeAI;
import com.aleksiyflekssiy.cursedworld.entity.behavior.CustomMemoryModuleTypes;
import com.aleksiyflekssiy.cursedworld.entity.behavior.CustomSensorTypes;
import com.aleksiyflekssiy.cursedworld.util.RotationUtil;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class MaxElephantEntity extends Shikigami{

    protected static final ImmutableList<SensorType<? extends Sensor<? super MaxElephantEntity>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_PLAYERS,
            CustomSensorTypes.SHIKIGAMI_OWNER_HURT.get(),
            CustomSensorTypes.SHIKIGAMI_OWNER_HURT_BY.get());

    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH,
            MemoryModuleType.ATTACK_TARGET, MemoryModuleType.WALK_TARGET,
            CustomMemoryModuleTypes.OWNER.get(), CustomMemoryModuleTypes.OWNER_HURT.get(),
            CustomMemoryModuleTypes.OWNER_HURT_BY_ENTITY.get(), MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.ATTACK_COOLING_DOWN);

    public MaxElephantEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        AABB damageZone = this.getBoundingBox().inflate(Math.max(10, pFallDistance * pMultiplier), 0, Math.max(10, pFallDistance * pMultiplier));
        int damage = (int) (this.getAttributeValue(Attributes.MAX_HEALTH) * (pFallDistance / 10));
        List<LivingEntity> entities = this.level().getEntities(this, damageZone)
                .stream()
                .takeWhile(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .toList();
        entities.forEach(entity -> entity.hurt(damageSources().generic(), damage));
        List<BlockPos> blockPosList = new ArrayList<>();
        for (int i = -5; i < 5; i++){
            for (int j = -5; j < 5; j++) {
                blockPosList.add(new BlockPos(this.getBlockX() + i, this.getBlockY(), this.getBlockZ() + j));
            }
        }
        blockPosList.forEach(blockPos -> ((ServerLevel)level()).sendParticles(
                ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                blockPos.getX(),
                blockPos.getY(),
                blockPos.getZ(),
                10,
                0, 0, 0, 1));
        return true;
    }



    @Override
    public void tame(Player owner) {
        super.tame(owner);
        this.getBrain().setMemory(CustomMemoryModuleTypes.OWNER.get(), owner);
    }

    @Override
    public Brain<MaxElephantEntity> getBrain() {
        return (Brain<MaxElephantEntity>) super.getBrain();
    }

    @Override
    protected Brain.Provider<MaxElephantEntity> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<MaxElephantEntity> makeBrain(Dynamic<?> pDynamic) {
        Brain<MaxElephantEntity> brain = this.brainProvider().makeBrain(pDynamic);
        return MaxElephantAI.makeBrain(brain);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        MaxElephantAI.updateActivity(this.getBrain());
        this.getBrain().tick((ServerLevel) this.level(), this);
    }

    @Override
    public boolean followOrder(LivingEntity target, BlockPos blockPos, IOrder order) {
        if (super.followOrder(target, blockPos, order)){
            this.getBrain().stopAll((ServerLevel) this.level(), this);
            if (order == MaxElephantOrder.PUSH){
                this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);
            }
            else if (order == MaxElephantOrder.MOVE){
                this.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockPos, 1, 1));
            }
            return true;
        }
        return false;
    }

    @Override
    public void clearOrder() {
        this.setOrder(MaxElephantOrder.NONE);
        this.getBrain().stopAll((ServerLevel) this.level(), this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 300)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 10)
                .add(Attributes.FOLLOW_RANGE, 100)
                .add(Attributes.ATTACK_SPEED, 0.2)
                .add(Attributes.ATTACK_KNOCKBACK, 5)
                .add(Attributes.ARMOR_TOUGHNESS, 2.5)
                .add(Attributes.JUMP_STRENGTH, 0.5);
    }

    public enum MaxElephantOrder implements IOrder{
        NONE,
        PUSH,
        MOVE
    }
}
