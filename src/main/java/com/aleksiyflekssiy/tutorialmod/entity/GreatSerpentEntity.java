package com.aleksiyflekssiy.tutorialmod.entity;

import com.aleksiyflekssiy.tutorialmod.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.TenShadowsTechnique;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.tenshadows.GreatSerpent;
import com.aleksiyflekssiy.tutorialmod.entity.ai.GreatSerpentAI;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMemoryModuleTypes;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomSensorTypes;
import com.aleksiyflekssiy.tutorialmod.entity.control.CustomFlyingMoveControl;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class GreatSerpentEntity extends Shikigami {

    private int supposedSegmentCount;
    private final List<GreatSerpentSegment> segments = new ArrayList<>();
    private final List<UUID> uuids = new ArrayList<>();
    private int emergeTicks = 0;
    private static final EntityDataAccessor<Integer> SEGMENT_COUNT = SynchedEntityData.defineId(GreatSerpentEntity.class, EntityDataSerializers.INT);
    protected static final ImmutableList<SensorType<? extends Sensor<? super GreatSerpentEntity>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_PLAYERS, CustomSensorTypes.SHIKIGAMI_OWNER_HURT.get(), CustomSensorTypes.SHIKIGAMI_OWNER_HURT_BY.get());
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.WALK_TARGET, CustomMemoryModuleTypes.OWNER.get(), CustomMemoryModuleTypes.OWNER_HURT.get(), CustomMemoryModuleTypes.OWNER_HURT_BY_ENTITY.get(), CustomMemoryModuleTypes.GRABBED_ENTITY.get(), CustomMemoryModuleTypes.ATTACK_TYPE.get(), MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    private BlockPos spawnPos;

    public GreatSerpentEntity(EntityType<? extends Shikigami> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.noCulling = true;
        this.entityData.set(SEGMENT_COUNT, 0);
        this.navigation = new FlyingPathNavigation(this, pLevel);
        this.moveControl = new CustomFlyingMoveControl(this);
    }

    public GreatSerpentEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel, Player owner) {
        super(pEntityType, pLevel, owner);
        this.noCulling = true;
        this.entityData.set(SEGMENT_COUNT, 0);
        this.navigation = new FlyingPathNavigation(this, pLevel);
        this.moveControl = new CustomFlyingMoveControl(this);
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    public void setSpawnPos(BlockPos spawnPos) {
        this.spawnPos = spawnPos;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SEGMENT_COUNT, 0);
    }

    public void calculateAndSetSegmentCount(BlockPos targetPos){
        int xDistance = targetPos.getX() - this.spawnPos.getX();
        int yDistance = targetPos.getY() - this.spawnPos.getY();
        int zDistance = targetPos.getZ() - this.spawnPos.getZ();

        double totalDistance = Math.sqrt(xDistance * xDistance + yDistance * yDistance + zDistance * zDistance);
        double outerVerticesMargin = Math.sqrt(this.getBbHeight() * this.getBbHeight() + this.getBbWidth() * this.getBbWidth());
        this.supposedSegmentCount = Math.toIntExact(Math.round(totalDistance / outerVerticesMargin));
        System.out.println(supposedSegmentCount);
    }

    @Override
    protected int calculateFallDamage(float pFallDistance, float pDamageMultiplier) {
        return 0;
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
    }

    public List<GreatSerpentSegment> getSegments() {
        return segments;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        emergeTicks++;
        if (!level().isClientSide()) {
            if (!uuids.isEmpty() && segments.size() < entityData.get(SEGMENT_COUNT)) {
                for (UUID uuid : uuids) {
                    GreatSerpentSegment segment = (GreatSerpentSegment) ((ServerLevel) level()).getEntity(uuid);
                    segments.add(segment);
                }
            }
            if (emergeTicks % 20 == 0 && segments.size() < supposedSegmentCount) {
                createSegment();
            }
        }

        Vec3[] avec3 = new Vec3[this.segments.size()];

        for(int j = 0; j < this.segments.size(); ++j) {
            avec3[j] = new Vec3(this.segments.get(j).getX(), this.segments.get(j).getY(), this.segments.get(j).getZ());
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

    private void createSegment() {
        GreatSerpentSegment segment = new GreatSerpentSegment(ModEntities.GREAT_SERPENT_SEGMENT.get(), this.level(), this, segments.size());
        segment.setPos(spawnPos.getCenter());
        addSegment(segment);
        level().addFreshEntity(segment);
    }

    public void addSegment(GreatSerpentSegment segment) {
        segments.add(segment);
        this.entityData.set(SEGMENT_COUNT, segments.size() + 1);
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
                .add(Attributes.JUMP_STRENGTH, 1)
                .add(Attributes.FLYING_SPEED, 0.5);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!segments.isEmpty()) {
            ListTag segmentsUUID = new ListTag();
            for (int i = 0; i < segments.size(); i++) {
                CompoundTag segmentUUID = new CompoundTag();
                segmentUUID.putUUID("segment" + i, segments.get(i).getUUID());
                segmentsUUID.add(segmentUUID);
            }
            tag.put("segments", segmentsUUID);
            System.out.println(segmentsUUID.size() + " segments saved");
        }
        if (getOwner() != null) System.out.println("Set Owner: " + getOwner());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ListTag segmentsUUID = tag.getList("segments", Tag.TAG_COMPOUND);
        for (int i = 0; i < segmentsUUID.size(); ++i) {
            CompoundTag segmentUUID = segmentsUUID.getCompound(i);
            uuids.add(segmentUUID.getUUID("segment" + i));
        }
        this.entityData.set(SEGMENT_COUNT, segmentsUUID.size());
        this.supposedSegmentCount = segmentsUUID.size();
        System.out.println(segmentsUUID.size() + " segments loaded");
    }

    @Override
    public void remove(RemovalReason pReason) {
        super.remove(pReason);
        segments.forEach(segment -> segment.remove(pReason));
    }

    @Override
    public void tame(Player owner) {
        super.tame(owner);
        this.getBrain().setMemory(CustomMemoryModuleTypes.OWNER.get(), owner);
        owner.getCapability(CursedTechniqueCapability.CURSED_TECHNIQUE).ifPresent(technique -> {
            if (technique instanceof TenShadowsTechnique tenShadowsTechnique) {
                System.out.println("Technique exists");
                GreatSerpent greatSerpent = (GreatSerpent) tenShadowsTechnique.getSkillSet().stream().filter(skill -> skill instanceof GreatSerpent).findFirst().orElse(null);
                greatSerpent.setShikigami(this);
            }
        });
        System.out.println("Owner: " + owner);
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

    @Override
    public boolean followOrder(LivingEntity target, BlockPos blockPos, IOrder order) {
        if (super.followOrder(target, blockPos, order)) {
            this.getBrain().stopAll((ServerLevel) this.level(), this);
            segments.forEach(segment -> {
                segment.getBrain().stopAll((ServerLevel) level(), segment);
            });
            if (order == GreatSerpentOrder.MOVE) this.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockPos, 1, 1));
            return true;
        }
        return false;
    }

    @Override
    public void clearOrder() {
        this.setOrder(GreatSerpentOrder.NONE);
        this.getBrain().stopAll((ServerLevel) this.level(), this);
        segments.forEach(segment -> {
            segment.getBrain().stopAll((ServerLevel) level(), segment);
        });
    }

    public enum GreatSerpentOrder implements IOrder{
        NONE,
        MOVE
    }
}