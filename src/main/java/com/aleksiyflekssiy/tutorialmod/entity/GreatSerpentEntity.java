package com.aleksiyflekssiy.tutorialmod.entity;

import com.aleksiyflekssiy.tutorialmod.entity.ai.GreatSerpentAI;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMemoryModuleTypes;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomSensorTypes;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class GreatSerpentEntity extends Shikigami {
    public static final float MAX_TURN_ANGLE = 12.25F;
    public static final int MAX_SEGMENT_COUNT = 15;
    private final List<GreatSerpentEntity> segments =  new ArrayList<>();
    private int emergeTicks = 0;
    private static final EntityDataAccessor<Integer> SEGMENT_COUNT = SynchedEntityData.defineId(GreatSerpentEntity.class, EntityDataSerializers.INT);
    protected static final ImmutableList<SensorType<? extends Sensor<? super GreatSerpentEntity>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_PLAYERS, CustomSensorTypes.SHIKIGAMI_OWNER_HURT.get(), CustomSensorTypes.SHIKIGAMI_OWNER_HURT_BY.get());
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.WALK_TARGET, CustomMemoryModuleTypes.OWNER.get(), CustomMemoryModuleTypes.OWNER_HURT.get(), CustomMemoryModuleTypes.OWNER_HURT_BY_ENTITY.get(), CustomMemoryModuleTypes.GRABBED_ENTITY.get(), CustomMemoryModuleTypes.ATTACK_TYPE.get(), MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    private BlockPos spawnPos;
    private Vec3 motionVec;
    private boolean spawn;

    public GreatSerpentEntity(EntityType<? extends Shikigami> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.noCulling = true;
        this.entityData.set(SEGMENT_COUNT, 5);
        this.spawn = true;
    }

    public GreatSerpentEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel, boolean spawn) {
        super(pEntityType, pLevel);
        this.spawn = spawn;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SEGMENT_COUNT, 0);
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        this.spawnPos = new BlockPos((int) x, (int) y, (int) z);
    }

    public List<GreatSerpentEntity> getSegments() {
        return segments;
    }

    public int getSegmentCount() {
        return this.entityData.get(SEGMENT_COUNT);
    }

    private void moveToTarget() {
        this.getBrain().getMemory(MemoryModuleType.WALK_TARGET).ifPresent(walkTarget -> {
            BlockPos targetPos = walkTarget.getTarget().currentBlockPosition();
            double speed = this.getAttributeValue(Attributes.MOVEMENT_SPEED);
            Vec3 currentPos = this.position();
            Vec3 targetVec = new Vec3(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
            Vec3 direction = targetVec.subtract(currentPos).normalize();
            motionVec = direction.scale(speed);
            this.setDeltaMovement(motionVec);
            for (GreatSerpentEntity segment : segments) {
                segment.setDeltaMovement(motionVec);
            }
        });
    }

    @Override
    public void tick() {
        super.tick();
        emergeTicks++;
        if (!level().isClientSide()) {
            if (emergeTicks % 20 == 0 && getSegmentCount() < MAX_SEGMENT_COUNT && this.spawn) {
                GreatSerpentEntity segment = new GreatSerpentEntity(ModEntities.GREAT_SERPENT.get(), this.level(), false);
                segment.setPos(spawnPos.getCenter());
                segments.add(segment);
                level().addFreshEntity(segment);
                this.entityData.set(SEGMENT_COUNT, getSegmentCount() + 1);
            }
            moveToTarget();
        }

        Vec3[] avec3 = new Vec3[this.segments.size()];

        for(int j = 0; j < this.segments.size(); ++j) {
            avec3[j] = new Vec3(this.segments.get(j).getX(), this.segments.get(j).getY(), this.segments.get(j).getZ());
        }
        for (int i = 0; i < this.segments.size(); i++) {
            segments.get(i).setPos(this.getX(), this.getY() + (i == 0 ? 2 : i * 2 + 2), this.getZ());
        }

        for(int l = 0; l < this.segments.size(); ++l) {
            this.segments.get(l).xo = avec3[l].x;
            this.segments.get(l).yo = avec3[l].y;
            this.segments.get(l).zo = avec3[l].z;
            this.segments.get(l).xOld = avec3[l].x;
            this.segments.get(l).yOld = avec3[l].y;
            this.segments.get(l).zOld = avec3[l].z;
        }
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
    public void tame(Player owner) {
        super.tame(owner);
        this.getBrain().setMemory(CustomMemoryModuleTypes.OWNER.get(), owner);
    }

    @Override
    protected Brain.Provider<GreatSerpentEntity> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
        Brain<GreatSerpentEntity> brain = this.brainProvider().makeBrain(pDynamic);
        return GreatSerpentAI.makeBrain(brain);
    }

    @Override
    public Brain<GreatSerpentEntity> getBrain() {
        return (Brain<GreatSerpentEntity>) super.getBrain();
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