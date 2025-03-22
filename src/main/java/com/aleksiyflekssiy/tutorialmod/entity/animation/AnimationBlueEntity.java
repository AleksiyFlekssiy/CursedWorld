package com.aleksiyflekssiy.tutorialmod.entity.animation;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import static com.aleksiyflekssiy.tutorialmod.util.RotationUtil.getOffsetLookPosition;

public class AnimationBlueEntity extends Entity {
    private final Player owner;
    private static final EntityDataAccessor<Boolean> START_FUSION = SynchedEntityData.defineId(AnimationBlueEntity.class, EntityDataSerializers.BOOLEAN);
    public AnimationBlueEntity(EntityType<?> pEntityType, Level pLevel) {
        this(pEntityType, pLevel, null);
    }

    public AnimationBlueEntity(EntityType<?> pEntityType, Level level, Player player) {
        super(pEntityType, level);
        this.owner = player;
        entityData.set(START_FUSION, false);
    }

    @Override
    public void tick() {
        if (owner != null && owner.isAlive()) {
            if (owner.level().isClientSide()) return;
            Vec3 lookVec = owner.getLookAngle().normalize(); // Направление взгляда игрока
            Vec3 eyePos = owner.getEyePosition();
            if (!entityData.get(START_FUSION)) {
                Vec3 targetPos = getOffsetLookPosition(owner, eyePos.add(lookVec.scale(2.0f)), -1.5, 0, 0); // Точка в 2 блоках перед игроком
                Vec3 currentPos = this.position();
                Vec3 delta = targetPos.subtract(currentPos).scale(0.5f); // Плавное приближение
                move(MoverType.SELF, delta);
            }
            else {
                Vec3 targetPos = getOffsetLookPosition(owner, eyePos.add(lookVec.scale(2.0f)), 0, 0, 0);;
                Vec3 currentPos = this.position();
                Vec3 delta = targetPos.subtract(currentPos).scale(1f); // Плавное приближение
                move(MoverType.SELF, delta);
            }
            lookAt(EntityAnchorArgument.Anchor.FEET, eyePos);
        }
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(START_FUSION, false);
    }

    public void setFusion(){
        entityData.set(START_FUSION, true);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

    }
}
