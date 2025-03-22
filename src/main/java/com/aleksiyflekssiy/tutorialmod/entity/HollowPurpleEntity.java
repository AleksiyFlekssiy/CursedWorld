package com.aleksiyflekssiy.tutorialmod.entity;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static com.aleksiyflekssiy.tutorialmod.util.RotationUtil.getOffsetLookPosition;

public class HollowPurpleEntity extends Projectile {
    private static final EntityDataAccessor<Boolean> IS_LAUNCHED = SynchedEntityData.defineId(HollowPurpleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> CHANT = SynchedEntityData.defineId(HollowPurpleEntity.class, EntityDataSerializers.INT);

    private float radius;
    private float speed;
    private final Player owner;
    private int lifetime = 200;
    protected HollowPurpleEntity(EntityType<? extends Projectile> entityType, Level level) {
        this(entityType, level, null, 0,0);
    }

    public HollowPurpleEntity(EntityType<? extends Projectile> entityType, Level level, Player owner, float radius, float speed) {
        super(entityType, level);
        this.owner = owner;
        this.radius = radius;
        this.speed = speed;
        this.noPhysics = true;
        this.noCulling = true;
        entityData.set(IS_LAUNCHED, false);
        entityData.set(CHANT, 0);
    }

    public void chant(){
        this.radius *= 1.5F;
        this.speed *= 1.5F;
        this.lifetime += 200;
        this.entityData.set(CHANT, getChant() + 1);
    }

    public int getChant(){
        return entityData.get(CHANT);
    }

    @Override
    public void tick() {
        super.tick();
        if (owner == null || !owner.isAlive()) return;
        if (!level().isClientSide()){
            Vec3 lookVec = owner.getLookAngle().normalize(); // Направление взгляда игрока
            Vec3 eyePos = owner.getEyePosition();
            Vec3 targetPos = getOffsetLookPosition(owner, eyePos.add(lookVec.scale( getChant() > 0 ? (2.5f * getChant()) + radius : 5)), 0, 0, 0);

            if (!isLaunched()){
                Vec3 currentPos = this.position();
                Vec3 delta = targetPos.subtract(currentPos).scale(0.5f);
                move(MoverType.SELF, delta);
                lookAt(EntityAnchorArgument.Anchor.FEET, eyePos);
            }
            else{
                updatePos();
            }
            if (lifetime == 0) this.discard();
            else lifetime--;
        }
    }

    public void launch(Vec3 direction) {
        setDeltaMovement(direction.scale(speed));
        entityData.set(IS_LAUNCHED, true);
    }

    public boolean isLaunched() {
        return entityData.get(IS_LAUNCHED);
    }

    private void updatePos(){
        erase();
        move(MoverType.SELF, getDeltaMovement());
    }

    private void erase(){
        Vec3 vec = position();
        double x = vec.x;
        double y = vec.y;
        double z = vec.z;
        int radiusInt = (int) Math.ceil(radius);
        BlockPos center = new BlockPos((int) x, (int) y, (int) z);
        DamageSources damageSources = this.level().damageSources();

        // Разрушение блоков и урон сущностям
        int blocksPerTick = 1000; // Обрабатываем 1000 блоков за тик
        int totalBlocks = (2 * radiusInt + 1) * (2 * radiusInt + 1) * (2 * radiusInt + 1);
        for (int i = 0; i < totalBlocks; i += blocksPerTick) {
            int finalI = i;
            this.level().getServer().execute(() -> {
                int end = Math.min(finalI + blocksPerTick, totalBlocks);
                for (int idx = finalI; idx < end; idx++) {
                    int dx = (idx / (radiusInt * 2 + 1) / (radiusInt * 2 + 1)) - radiusInt;
                    int dy = (idx / (radiusInt * 2 + 1)) % (radiusInt * 2 + 1) - radiusInt;
                    int dz = idx % (radiusInt * 2 + 1) - radiusInt;
                    BlockPos pos = center.offset(dx, dy, dz);
                    double distanceSquared = dx * dx + dy * dy + dz * dz;
                    if (distanceSquared <= radius * radius) {
                        BlockState state = this.level().getBlockState(pos);
                        if (!state.getFluidState().isEmpty()) {
                            this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        } else if (!state.isAir() && state.getDestroySpeed(this.level(), pos) >= 0) {
                            this.level().removeBlock(pos, false); // Удаляем твёрдые блоки
                        }
                    }
                }
            });
        }

        // Нанесение урона сущностям в радиусе
        AABB explosionArea = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
        List<Entity> entities = this.level().getEntities(this, explosionArea, e -> e != this && e.isAlive());
        for (Entity entity : entities) {
            double distance = entity.distanceToSqr(x, y, z);
            if (distance <= radius * radius) {
                float damage = 500.0F * (float) (1.0 - Math.sqrt(distance) / radius); // Урон уменьшается с расстоянием
                entity.hurt(damageSources.explosion(this, this.getOwner()), entity == owner ? damage / 10 : damage);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(IS_LAUNCHED, false);
        entityData.define(CHANT, 0);
    }
}