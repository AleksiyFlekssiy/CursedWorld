package com.aleksiyflekssiy.tutorialmod.entity;

import com.aleksiyflekssiy.tutorialmod.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.TenShadowsTechnique;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.tenshadows.GreatSerpent;
import com.aleksiyflekssiy.tutorialmod.entity.ai.GreatSerpentAI;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMemoryModuleTypes;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomSensorTypes;
import com.aleksiyflekssiy.tutorialmod.entity.control.GreatSerpentMoveControl;
import com.aleksiyflekssiy.tutorialmod.entity.navigation.CustomFlyingPathNavigation;
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

    public boolean isSpawning = true;
    private int supposedSegmentCount;
    private final List<GreatSerpentSegment> segments = new ArrayList<>();
    private final List<UUID> uuids = new ArrayList<>();
    private int emergeTicks = 0;
    private static final EntityDataAccessor<Integer> SEGMENT_COUNT = SynchedEntityData.defineId(GreatSerpentEntity.class, EntityDataSerializers.INT);
    protected static final ImmutableList<SensorType<? extends Sensor<? super GreatSerpentEntity>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_PLAYERS, CustomSensorTypes.SHIKIGAMI_OWNER_HURT.get(), CustomSensorTypes.SHIKIGAMI_OWNER_HURT_BY.get());
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.WALK_TARGET, CustomMemoryModuleTypes.OWNER.get(), CustomMemoryModuleTypes.OWNER_HURT.get(), CustomMemoryModuleTypes.OWNER_HURT_BY_ENTITY.get(), CustomMemoryModuleTypes.GRABBED_ENTITY.get(), CustomMemoryModuleTypes.GRAB_TARGET.get() , CustomMemoryModuleTypes.ATTACK_TYPE.get(), MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, CustomMemoryModuleTypes.ORDER.get());
    private BlockPos spawnPos;
    public boolean positionSet = false;

    private final Deque<Vec3> positionHistory = new ArrayDeque<>();
    private static final int HISTORY_SIZE = 400; // Подбери под длину змеи (больше = длиннее тело без разрывов)
    private static final double SEGMENT_SPACING = 2D; // <<< КЛЮЧ! Для хитбокса 0.8×0.8 — 0.79-0.80 идеально (границы касаются)


    public GreatSerpentEntity(EntityType<? extends Shikigami> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.noCulling = true;
        this.entityData.set(SEGMENT_COUNT, 0);
        this.navigation = new FlyingPathNavigation(this, pLevel);
        this.moveControl = new GreatSerpentMoveControl(this);
    }

    public GreatSerpentEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel, Player owner) {
        super(pEntityType, pLevel, owner);
        this.noCulling = true;
        this.entityData.set(SEGMENT_COUNT, 0);
        this.navigation = new CustomFlyingPathNavigation(this, pLevel);
        this.noPhysics = true;

        //Причина застреваний, мой кастомный контрол
        this.moveControl = new GreatSerpentMoveControl(this);
    }

    @Override
    public boolean isInWall() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    public void setSpawnPos(BlockPos spawnPos) {
        this.spawnPos = spawnPos;
        this.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
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
        return false;
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
                    if (segment != null) segments.add(segment);
                }
            }
            if (emergeTicks % 4 == 0 && segments.size() < supposedSegmentCount) {
                createSegment();
            }
        }
        if (segments.size() == supposedSegmentCount) this.isSpawning = false;
        //if (getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)) System.out.println("There is target");
        //System.out.println(getBrain().getRunningBehaviors());
        if (this.getNavigation().getPath() == null && !segments.isEmpty() && this.position().distanceToSqr(segments.get(0).position()) <= 4) return;
        moveSegments();
    }

    private void moveSegments() {
        // 1. Записываем позицию головы
        positionHistory.addLast(this.position());
        if (positionHistory.size() > HISTORY_SIZE) {
            positionHistory.removeFirst();
        }

        // 2. Расставляем сегменты по траектории на фиксированном расстоянии

        List<Vec3> trail = new ArrayList<>(positionHistory);
        Vec3 headPos = this.position();

        Vec3 prevPos = headPos;
        for (int i = 0; i < segments.size(); i++) {
            GreatSerpentSegment seg = segments.get(i);

            double desiredDist = (i + 1) * SEGMENT_SPACING;

            Vec3 targetPos = findPointOnTrail(trail, headPos, desiredDist);

            if (targetPos == null) {
                // Если истории мало, отступаем назад по направлению
                Vec3 dir = this.getForward().normalize();
                targetPos = prevPos.subtract(dir.scale(SEGMENT_SPACING));
            }

            // Плавный lerp к цели
            Vec3 current = seg.position();
            Vec3 newPos = current.lerp(targetPos, 0.5D);

            seg.setPos(newPos);

            // Поворот по траектории
            Vec3 dir = prevPos.subtract(newPos).normalize();
            if (dir.lengthSqr() > 0.001) {
                float yaw = (float) Mth.atan2(dir.z, dir.x) * Mth.RAD_TO_DEG - 90F;
                float pitch = (float) Mth.atan2(dir.y, dir.horizontalDistance()) * Mth.RAD_TO_DEG;
                seg.setYRot(yaw);
                seg.setXRot(pitch);
                seg.yRotO = yaw;
                seg.xRotO = pitch;
            }

            prevPos = newPos; // Для следующего сегмента
        }
    }

    // Новая функция: Ищет точку на траектории на заданном расстоянии от головы
    private Vec3 findPointOnTrail(List<Vec3> trail, Vec3 headPos, double distance) {
        double accumulated = 0.0D;
        Vec3 prev = headPos;

        for (int i = trail.size() - 2; i >= 0; i--) { // От текущей к старой
            Vec3 current = trail.get(i);
            double segLen = prev.distanceTo(current);
            if (accumulated + segLen >= distance) {
                double t = (distance - accumulated) / segLen;
                return prev.lerp(current, t);
            }
            accumulated += segLen;
            prev = current;
        }
        return null;
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
        GreatSerpentAI.updateActivity(this.getBrain());
        super.customServerAiStep();
    }

    @Override
    public void setDeltaMovement(double pX, double pY, double pZ) {
        //System.out.println(Thread.currentThread().getStackTrace()[3].toString() + " " + pX + " " + pY + " " + pZ);  // Кто именно вызвал мой метод
        super.setDeltaMovement(pX, pY, pZ);
    }

    @Override
    public boolean followOrder(LivingEntity target, BlockPos blockPos, IOrder order) {
        if (super.followOrder(target, blockPos, order)) {
            this.getBrain().stopAll((ServerLevel) this.level(), this);
            segments.forEach(segment -> {
                segment.getBrain().stopAll((ServerLevel) level(), segment);
            });
            if (order == GreatSerpentOrder.MOVE) this.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockPos, 1, 1));
            else if (order == GreatSerpentOrder.CATCH) {
                if (!this.isAddedToWorld()) {
                    if (spawnPos == null && blockPos != null) {
                        System.out.println("Position set");
                        setSpawnPos(blockPos);
                    } else if (spawnPos != null && target != null) {
                        Vec3 targetPos = target.position();
                        this.calculateAndSetSegmentCount(BlockPos.containing(targetPos));
                        Vec3 vec = position().subtract(targetPos).reverse();
                        this.setDeltaMovement(vec.normalize().scale(2.5));
                        this.getBrain().setMemory(CustomMemoryModuleTypes.GRAB_TARGET.get(), target);
                        this.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos.add(vec.scale(1)), 1, 1));
                        System.out.println("Catch");
                        level().addFreshEntity(this);
                    }
                }
                else {
                    if (target != null){
                        Vec3 targetPos = target.position();
                        Vec3 vec = position().subtract(targetPos).reverse();
                        this.setDeltaMovement(vec.normalize().scale(0.5));
                        this.getBrain().setMemory(CustomMemoryModuleTypes.GRAB_TARGET.get(), target);
                        this.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, 1, 1));
                        System.out.println("Catch Alive");
                    }
                }
            }
            else if (order == GreatSerpentOrder.SMASH){
                Optional<LivingEntity> grabTarget = getBrain().getMemory(CustomMemoryModuleTypes.GRAB_TARGET.get());
                if (!this.isAddedToWorld()) {
                    if (grabTarget.isEmpty() && spawnPos == null && target != null) {
                        this.getBrain().setMemory(CustomMemoryModuleTypes.GRAB_TARGET.get(), target);
                        System.out.println("Target set");
                    }
                    else if (blockPos != null && spawnPos == null && grabTarget.isPresent()) {
                        LivingEntity entity = grabTarget.get();
                        Vec3 vec = entity.position().subtract(blockPos.getCenter()).reverse();
                        Vec3 spawnVec = new Vec3(
                                blockPos.getCenter().x > entity.position().x ? -1 : 1,
                                blockPos.getCenter().y > entity.position().y ? -1 : 1,
                                blockPos.getCenter().z > entity.position().z ? -1 : 1
                        );
                        setSpawnPos(BlockPos.containing(entity.position().add(spawnVec)));
                        this.calculateAndSetSegmentCount(BlockPos.containing(vec.normalize()));
                        this.setDeltaMovement(vec.normalize().scale(2.5));
                        this.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockPos, 1, 1));
                        System.out.println("Catch");
                        level().addFreshEntity(this);
                    }
                }
                else {
                    Optional<LivingEntity> grabbedEntity = getBrain().getMemory(CustomMemoryModuleTypes.GRABBED_ENTITY.get());
                    LivingEntity entity = null;
                    if (grabTarget.isPresent()) entity = grabTarget.get();
                    else if (grabbedEntity.isPresent()) entity = grabbedEntity.get();
                    if (entity != null && blockPos != null) {
                        Vec3 vec = entity.position().subtract(blockPos.getCenter()).reverse();
                        this.setDeltaMovement(vec.normalize().scale(2.5));
                        this.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockPos, 1, 1));
                        System.out.println("Catch");
                    }
                }
            }
            else if (order == GreatSerpentOrder.THROW){
                if (getBrain().hasMemoryValue(CustomMemoryModuleTypes.GRABBED_ENTITY.get()) && blockPos != null){
                    LivingEntity grabbedEntity = getBrain().getMemory(CustomMemoryModuleTypes.GRABBED_ENTITY.get()).get();
                    grabbedEntity.stopRiding();
                    Vec3 vec = grabbedEntity.position().subtract(blockPos.getCenter()).normalize().reverse().scale((double) segments.size() / 5);
                    grabbedEntity.setDeltaMovement(vec);
                    getBrain().eraseMemory(CustomMemoryModuleTypes.GRABBED_ENTITY.get());
                }
            }
            getBrain().setMemory(CustomMemoryModuleTypes.ORDER.get(), order);
            return true;
        }
        return false;
    }

    @Override
    public void clearOrder() {
        this.setOrder(GreatSerpentOrder.NONE);
        this.getBrain().stopAll((ServerLevel) this.level(), this);
        getBrain().eraseMemory(CustomMemoryModuleTypes.ORDER.get());
        segments.forEach(segment -> {
            segment.getBrain().stopAll((ServerLevel) level(), segment);
        });
    }

    public enum GreatSerpentOrder implements IOrder{
        NONE,
        MOVE,
        CATCH,
        SMASH,
        THROW
    }
}