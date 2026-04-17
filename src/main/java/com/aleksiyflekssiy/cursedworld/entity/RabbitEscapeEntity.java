package com.aleksiyflekssiy.cursedworld.entity;

import com.aleksiyflekssiy.cursedworld.entity.ai.RabbitEscapeAI;
import com.aleksiyflekssiy.cursedworld.entity.behavior.CustomMemoryModuleTypes;
import com.aleksiyflekssiy.cursedworld.entity.behavior.CustomSensorTypes;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class RabbitEscapeEntity extends Shikigami{
    protected static final ImmutableList<SensorType<? extends Sensor<? super RabbitEscapeEntity>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_PLAYERS,
            CustomSensorTypes.SHIKIGAMI_OWNER_HURT.get(),
            CustomSensorTypes.SHIKIGAMI_OWNER_HURT_BY.get());

    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH,
            MemoryModuleType.ATTACK_TARGET, MemoryModuleType.WALK_TARGET,
            CustomMemoryModuleTypes.OWNER.get(), CustomMemoryModuleTypes.OWNER_HURT.get(),
            CustomMemoryModuleTypes.OWNER_HURT_BY_ENTITY.get(), MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.ATTACK_COOLING_DOWN);


    public RabbitEscapeEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected int calculateFallDamage(float pFallDistance, float pDamageMultiplier) {
        return 0;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.FOLLOW_RANGE, 100)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 1)
                .add(Attributes.ARMOR_TOUGHNESS, 1)
                .add(Attributes.JUMP_STRENGTH, 1);
    }

    @Override
    public Brain<RabbitEscapeEntity> getBrain() {
        return (Brain<RabbitEscapeEntity>) super.getBrain();
    }

    @Override
    public boolean followOrder(LivingEntity target, BlockPos blockPos, IOrder order) {
        if (super.followOrder(target, blockPos, order)){
            this.getBrain().stopAll((ServerLevel) this.level(), this);
            if (order == RabbitEscapeOrder.ATTACK){
                this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);
            }
            else if (order == RabbitEscapeOrder.MOVE){
                this.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockPos, 1, 1));
            }
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(target -> {
                if (target.position().distanceToSqr(this.position()) <= 1) target.hurt(this.damageSources().mobAttack(this), 1);
            });
        }
    }

    @Override
    public void tame(Player owner) {
        super.tame(owner);
        this.getBrain().setMemory(CustomMemoryModuleTypes.OWNER.get(), owner);
    }

    @Override
    protected Brain.Provider<RabbitEscapeEntity> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<RabbitEscapeEntity> makeBrain(Dynamic<?> pDynamic) {
        Brain<RabbitEscapeEntity> brain = this.brainProvider().makeBrain(pDynamic);
        return RabbitEscapeAI.makeBrain(brain);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        RabbitEscapeAI.updateActivity(this.getBrain());
        this.getBrain().tick((ServerLevel) this.level(), this);
    }

    @Override
    public void clearOrder() {
        this.setOrder(RabbitEscapeOrder.NONE);
        this.getBrain().stopAll((ServerLevel) this.level(), this);
    }

    public enum RabbitEscapeOrder implements IOrder{
        NONE,
        ATTACK,
        SURROUND,
        MOVE
    }
}
