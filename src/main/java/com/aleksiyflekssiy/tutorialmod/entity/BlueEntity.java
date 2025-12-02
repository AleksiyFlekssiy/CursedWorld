package com.aleksiyflekssiy.tutorialmod.entity;

import com.aleksiyflekssiy.tutorialmod.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.Skill;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.limitless.Blue;
import com.aleksiyflekssiy.tutorialmod.damage.ModDamageSources;
import com.aleksiyflekssiy.tutorialmod.event.SkillEvent;
import com.aleksiyflekssiy.tutorialmod.item.custom.BlueItem;
import com.aleksiyflekssiy.tutorialmod.particle.ModParticles;
import com.aleksiyflekssiy.tutorialmod.sound.ModSoundEvents;
import com.aleksiyflekssiy.tutorialmod.sound.custom.BluePullSound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

import java.util.Optional;
import java.util.UUID;

public class BlueEntity extends Entity {
    private final float maxRadius;
    private final float blockBreakingRadius;
    private final float pullForce;
    private int lifetime; // 5 секунд (100 тиков)
    private Player owner; // Владелец сущности
    private boolean isFollowing; // Флаг следования за курсором
    private final boolean isBreakingBlock;

    private BluePullSound ambientSound;

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(BlueEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> IS_FOLLOWING = SynchedEntityData.defineId(BlueEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> CHANT = SynchedEntityData.defineId(BlueEntity.class, EntityDataSerializers.INT);

    public BlueEntity(EntityType<? extends BlueEntity> type, Level level) {
        this(type, level, null, false,  60, 5, 5, 1.5f, 0);
    }

    public BlueEntity(EntityType<? extends BlueEntity> type,
                      Level level,
                      Player owner,
                      boolean isFollowing,
                      int lifetime,
                      float maxRadius,
                      float blockBreakingRadius,
                      float pullForce,
                      int chant
    ) {
        super(type, level);
        this.owner = owner;
        this.isFollowing = isFollowing;
        this.isBreakingBlock = chant > 1;
        this.lifetime = chant > 0 ? lifetime * chant * 2 : lifetime;
        this.maxRadius = chant > 0 ? maxRadius * chant * 1.5f : maxRadius;
        this.blockBreakingRadius = isBreakingBlock ? blockBreakingRadius * chant / 2 : 0;
        this.pullForce = chant > 0 ? pullForce * chant : pullForce;
        if (owner != null) {
            this.entityData.set(OWNER_UUID, Optional.of(owner.getUUID()));
        } else {
            this.entityData.set(OWNER_UUID, Optional.of(UUID.randomUUID())); // Уникальный UUID по умолчанию
        }
        startAmbientSound();
        this.entityData.set(CHANT, chant);
        this.entityData.set(IS_FOLLOWING, isFollowing);
    }

    private static Skill getSkill(){
        return new Blue();
    }

    private void startAmbientSound() {
        this.ambientSound = new BluePullSound(ModSoundEvents.BLUE_PULL.get(), this);
        Minecraft.getInstance().getSoundManager().play(this.ambientSound);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        // Останавливаем звук на клиенте при удалении сущности
        if (this.level().isClientSide && this.ambientSound != null) {
         this.ambientSound.startFadeOut();
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER_UUID, Optional.of(UUID.randomUUID()));
        this.entityData.define(IS_FOLLOWING, false);
        this.entityData.define(CHANT, 0);
    }

    public int getChant(){
        return this.entityData.get(CHANT);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (isFollowing && owner != null) {
                if (owner.isCrouching()) {
                    // Следование за курсором
                    Vec3 eyePos = owner.getEyePosition(1.0f);
                    Vec3 lookVec = owner.getLookAngle();
                    Vec3 targetPos = eyePos.add(lookVec.scale(10.0));
                    Vec3 currentPos = this.position();
                    Vec3 newPos = currentPos.lerp(targetPos, 1.0); // Плавное движение с коэффициентом 0.2
                    this.setPos(newPos.x, newPos.y, newPos.z);
                }
                else {
                    isFollowing = false;
                    this.entityData.set(IS_FOLLOWING, false);
                }
            }
                if (lifetime > 0) {
                    applyBlueEffect();
                    spawnPullingParticles((ServerLevel) level());
                    lifetime--;
                }
                else this.discard();
        } else {
            // Обновляем owner и isFollowing на клиенте
            UUID ownerUuid = this.entityData.get(OWNER_UUID).get();
            for (Entity entity : this.level().getEntities((Entity) null, this.getBoundingBox().inflate(20.0), e -> e instanceof Player)) {
                if (entity instanceof Player player && player.getUUID().equals(ownerUuid)) {
                    this.owner = player;
                    break;
                }
            }
            if (lifetime == 1 && owner instanceof AbstractClientPlayer player) BlueItem.stopAnimation(player);
            this.isFollowing = this.entityData.get(IS_FOLLOWING);
        }
    }

    private void applyBlueEffect() {
        Vec3 targetPos = this.position();
        AABB area = new AABB(targetPos.x - maxRadius, targetPos.y - maxRadius, targetPos.z - maxRadius,
                targetPos.x + maxRadius, targetPos.y + maxRadius, targetPos.z + maxRadius);
        Level level = this.level();

        for (Entity entity : level.getEntitiesOfClass(Entity.class, area)) {
            if (entity == owner || entity instanceof RedEntity
            || !getSkill().canAffect(entity)) continue;

            Vec3 entityPos = entity.position();
            double distance = targetPos.distanceTo(entityPos);
            if (distance > maxRadius) continue;
            BlueItem.spawnTrailParticles((ServerLevel) level, entity);
            double pullStrength = pullForce * maxRadius * 0.5 * (1.0 - distance / maxRadius);
            Vec3 direction = targetPos.subtract(entityPos).normalize();
            Vec3 pullMotion = direction.scale(pullStrength);
            Vec3 newMotion = entity.getDeltaMovement().lerp(pullMotion, 0.4);
            entity.setDeltaMovement(newMotion);
            entity.hurtMarked = true;

            if (distance < 0.5 || entity.horizontalCollision || entity.verticalCollision) {
                float damage = (float) (pullStrength * 2.0);
                entity.hurt(ModDamageSources.blue(this, this.owner), damage);
                if (entity instanceof LivingEntity livingEntity) {
                    SkillEvent.Hit hitEvent = new SkillEvent.Hit(this.owner, new Blue(), livingEntity);
                    MinecraftForge.EVENT_BUS.post(hitEvent);
                }
            }
        }

        if (isBreakingBlock) {
            for (int x = (int) (targetPos.x - maxRadius); x <= (int) (targetPos.x + maxRadius); x++) {
                for (int y = (int) (targetPos.y - maxRadius); y <= (int) (targetPos.y + maxRadius); y++) {
                    for (int z = (int) (targetPos.z - maxRadius); z <= (int) (targetPos.z + maxRadius); z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        double distance = targetPos.distanceTo(Vec3.atCenterOf(pos));
                        if (distance <= blockBreakingRadius) {
                            BlockState state = level.getBlockState(pos);
                            float hardness = state.getDestroySpeed(level, pos);
                            double pullStrength = pullForce * maxRadius * 0.5 * (1.0 - distance / maxRadius);
                            if (hardness >= 0 && hardness < 5.0F && pullStrength > hardness) {
                                level.destroyBlock(pos, true);
                            }
                        }
                    }
                }
            }
        }
    }

    private void spawnPullingParticles(ServerLevel level) {
        if (lifetime <= 0) return; // Не спавним частицы, если сущность уже "мертва"
        int particleCount = (int)(maxRadius * 3); // Ограничиваем количество частиц для плавности
        for (int i = 0; i < particleCount; i++) {
            double spawnX = this.getX();
            double spawnY = this.getY() + 0.5;
            double spawnZ = this.getZ();

            double speed = 25;

            // Спавним частицу с направлением к центру
            level.sendParticles(ModParticles.BLUE_PULL.get(), spawnX, spawnY, spawnZ, Math.max(1, getChant() - 1),
                    0, 0, 0, speed);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

}

