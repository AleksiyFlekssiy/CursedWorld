package com.aleksiyflekssiy.tutorialmod.entity;

import com.aleksiyflekssiy.tutorialmod.client.particle.LaunchRingParticleData;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.Skill;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.limitless.HollowPurple;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.limitless.Red;
import com.aleksiyflekssiy.tutorialmod.damage.ModDamageSources;
import com.aleksiyflekssiy.tutorialmod.event.SkillEvent;
import com.aleksiyflekssiy.tutorialmod.sound.ModSoundEvents;
import com.aleksiyflekssiy.tutorialmod.util.CustomExplosion;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RedEntity extends Projectile {
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(RedEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> IS_LAUNCHED = SynchedEntityData.defineId(RedEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> CHARGE = SynchedEntityData.defineId(RedEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CHANT = SynchedEntityData.defineId(RedEntity.class, EntityDataSerializers.INT);
    private final Player owner;
    private final int chargeTime = 20; // 3 секунды зарядки (60 тиков)
    private final float speed;
    private final float explosionPower;
    private final boolean isDestroyingBlocks;
    private final ATTACK_TYPE attackType;
    private final SpiralAnimation animation = new SpiralAnimation();
    private int piercing;
    private int charge = 0;
    private int lifetime; // Время жизни после зарядки
    private boolean isCharged = false;

    public RedEntity(EntityType<? extends RedEntity> entityType, Level level) {
        this(entityType, level, null, 80, 2.5f, 0, ATTACK_TYPE.MELEE, 0);
    }

    public RedEntity(EntityType<? extends RedEntity> entityType, Level level, Player owner, int lifetime, float speed, float explosionPower, ATTACK_TYPE attackType, int chant) {
        super(entityType, level);
        this.owner = owner;
        this.lifetime = chant > 0 ? lifetime * chant * 2 : lifetime;
        this.speed = chant > 0 ? chant == 1 ? speed * 2 : speed * chant : speed;
        this.isDestroyingBlocks = chant > 0;
        this.explosionPower = chant > 0 ? chant == 1 ? explosionPower * 2 : explosionPower * chant : explosionPower;
        this.attackType = attackType;
        this.piercing = isDestroyingBlocks ? chant * 3 : 0;
        this.noPhysics = true;
        if (owner != null) {
            this.entityData.set(OWNER_UUID, Optional.of(owner.getUUID()));
            Vec3 eyePos = owner.getEyePosition(); // Позиция глаз игрока
            lookAt(EntityAnchorArgument.Anchor.FEET, eyePos);
        } else {
            this.entityData.set(OWNER_UUID, Optional.empty());
            System.out.println("NO OWNER");
        }
        this.entityData.set(CHANT, chant);
        this.entityData.set(IS_LAUNCHED, false);
        this.entityData.set(CHARGE, 0);
    }

    protected boolean canHitEntity(Entity entity) {
        if (this == entity) return false;
        return getSkill().canAffect(entity);
    }

    private static Skill getSkill(){
        return new Red();
    }

    @Override
    public void tick() {
        if (owner == null || !owner.isAlive()) {
            if (!this.level().isClientSide()) {
                this.discard(); // Удаляем на сервере, если владельца нет
            }
            return;
        }
        super.tick();
        // Логика на сервере: только управление состоянием и временем жизни
        if (!this.level().isClientSide()) {
            isCharged = this.entityData.get(IS_LAUNCHED);
            if (!isCharged) {
                Vec3 lookVec = owner.getLookAngle().normalize(); // Направление взгляда игрока
                Vec3 eyePos = owner.getEyePosition(); // Позиция глаз игрока
                if (charge < chargeTime) {
                    if (charge == 0 && this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.playSound(
                                null, // null для воспроизведения всем игрокам поблизости
                                this.getX(), this.getY(), this.getZ(),
                                ModSoundEvents.RED_LAUNCH.get(),
                                SoundSource.NEUTRAL,
                                1.0f, // Громкость
                                1.0f  // Высота тона
                        );
                    }
                    animation.spawnParticles(this, (ServerLevel) level(), getChargeProgress());
                    Vec3 targetPos = eyePos.add(lookVec.scale(2.0f)); // Точка в 2 блоках перед игроком
                    Vec3 currentPos = this.position();
                    Vec3 delta = targetPos.subtract(currentPos).scale(0.5f); // Плавное приближение
                    this.setDeltaMovement(delta);
                    charge++;
                    this.entityData.set(CHARGE, charge); // Обновляем charge для клиента
                } else {
                    if (this.attackType == ATTACK_TYPE.MELEE) meleeAttack();
                    else rangedAttack(lookVec);
                    this.entityData.set(IS_LAUNCHED, true); // Синхронизируем запуск
                    isCharged = true;
                }
                lookAt(EntityAnchorArgument.Anchor.FEET, eyePos);
            }
            updatePos();
            if (lifetime > 0) {
                lifetime--;
            } else {
                this.discard();
            }
            if (isCharged && !level().isClientSide() &&  level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        new LaunchRingParticleData(this.getId()),
                        this.getX(), this.getY(), this.getZ(),
                        1,
                        0, 0, 0,
                        0
                );
            }
        }
    }

    private void meleeAttack() {
        Vec3 position = position();
        AABB repelArea = new AABB(position.x + explosionPower, position.y + explosionPower, position.z + explosionPower,
                position.x - explosionPower, position.y - explosionPower, position.z - explosionPower);
        for (Entity entity : level().getEntitiesOfClass(Entity.class, repelArea)) {
            if (entity == owner && !owner.isCrouching()) {
                Vec3 vec = owner.getLookAngle();
                owner.push(-vec.x * explosionPower * 2, -vec.y * explosionPower * 2, -vec.z * explosionPower * 2);
            } else {
                if (canHitEntity(entity)) {
                    Vec3 toPlayer = this.position().subtract(entity.position()).normalize().scale(explosionPower * 2);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(toPlayer).scale(speed));
                    entity.hurt(ModDamageSources.red(this, this.owner), explosionPower * 2f);
                    entity.hurtMarked = true;
                    if (entity instanceof LivingEntity livingEntity) {
                        SkillEvent.Hit hitEvent = new SkillEvent.Hit(this.owner, new Red(), livingEntity);
                        MinecraftForge.EVENT_BUS.post(hitEvent);
                    }
                }
            }
        }
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 1.0f, 1.0f);
        owner.sendSystemMessage(Component.literal("Red LMB"));
        this.discard();
    }

    private void rangedAttack(Vec3 angle) {
        launch(angle);
    }

    public float getChargeProgress() {
        return (float) this.entityData.get(CHARGE) / (float) chargeTime;
    }

    private void launch(Vec3 direction) {
        if (!isCharged) {
            if (this.level() instanceof ServerLevel serverLevel) {
                // Спавним кастомную частицу с направлением движения
                serverLevel.sendParticles(
                        new LaunchRingParticleData(this.getId()),
                        this.getX(), this.getY(), this.getZ(),
                        1,
                        0, 0, 0,
                        0.0
                );
            }
            this.setDeltaMovement(direction.scale(speed));
            isCharged = true;
        }
    }

    private void updatePos() {
        Vec3 vec3 = this.getDeltaMovement();
        // Синхронизация ориентации с взглядом игрока
        if (this.attackType != ATTACK_TYPE.MELEE) {
            checkBlockCollision(vec3);
            customCheckCollision(vec3);
        }
        move(MoverType.SELF, vec3);
    }

    private void customCheckCollision(Vec3 delta) {
        Vec3 start = position();
        Vec3 end = start.add(delta);
        HitResult hitresult = this.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hitresult.getType() != HitResult.Type.MISS) {
            end = hitresult.getLocation();
        }
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(this.level(), this, start, end, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0F), this::canHitEntity);
        if (entityHitResult != null) hitresult = entityHitResult;
        if (hitresult.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) hitresult).getEntity();
            Entity entity1 = this.getOwner();
            if (entity instanceof Player && entity1 instanceof Player && !((Player) entity1).canHarmPlayer((Player) entity)) {
                hitresult = null;
            }
            if (hitresult != null && hitresult.getType() != HitResult.Type.MISS) {
                switch (ForgeEventFactory.onProjectileImpactResult(this, hitresult)) {
                    case SKIP_ENTITY:
                        if (hitresult.getType() != HitResult.Type.ENTITY) {
                            this.onHit(hitresult);
                            this.hasImpulse = true;
                        }
                        break;
                    case STOP_AT_CURRENT_NO_DAMAGE:
                        this.discard();
                        break;
                    case STOP_AT_CURRENT:
                    case DEFAULT:
                        this.onHit(hitresult);
                        this.hasImpulse = true;
                }
            }
        }
    }

    private void checkBlockCollision(Vec3 vec3) {
        HitResult hitResult = this.level().clip(new ClipContext(
                this.position(), this.position().add(vec3), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER_UUID, Optional.empty());
        this.entityData.define(IS_LAUNCHED, false);
        this.entityData.define(CHARGE, 0); // Регистрируем charge
        this.entityData.define(CHANT, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.hasUUID("OwnerUUID")) {
            this.entityData.set(OWNER_UUID, Optional.of(compoundTag.getUUID("OwnerUUID")));
        }
        this.isCharged = compoundTag.getBoolean("IsLaunched");
        this.entityData.set(IS_LAUNCHED, this.isCharged);
        this.charge = compoundTag.getInt("Charge");
        this.entityData.set(CHARGE, this.charge);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        this.entityData.get(OWNER_UUID).ifPresent(uuid -> compoundTag.putUUID("OwnerUUID", uuid));
        compoundTag.putBoolean("IsLaunched", this.isCharged);
        compoundTag.putInt("Charge", this.charge);
    }

    public int getChant() {
        return this.entityData.get(CHANT);
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide()) { // Взрыв только на сервере
            super.onHit(result);
            // Проверяем, попали ли мы в BlueEntity
            if (result.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) result;
                Entity hitEntity = entityHit.getEntity();
                if (canHitEntity(hitEntity)) {
                    hitEntity.hurt(ModDamageSources.red(this, this.owner), speed * explosionPower);
                    hitEntity.setDeltaMovement(this.getDeltaMovement().scale(speed));
                    if (hitEntity instanceof LivingEntity livingEntity) {
                        SkillEvent.Hit hitEvent = new SkillEvent.Hit(this.owner, getSkill(), livingEntity);
                        MinecraftForge.EVENT_BUS.post(hitEvent);
                    }
                }
                if (hitEntity instanceof BlueEntity blueEntity) {
                    // Гигантский взрыв
                    System.out.println("Blue: " + blueEntity.getChant() + " Red: " + this.getChant());
                    if (blueEntity.getChant() == 3 && this.getChant() == 3) {
                        this.createMassiveExplosion(this.getX(), this.getY(), this.getZ(), 50);
                        hitEntity.discard();
                        this.discard();
                        return;
                    }
                }
            }
            if (isDestroyingBlocks) {
                CustomExplosion.createExplosion((ServerLevel) level(), position(), explosionPower * 1.5f, explosionPower * 2, false, true);
            }
            if (piercing <= 0) {
                this.discard(); // Удаляем сущность после взрыва
            } else {
                piercing--;
                this.hasImpulse = true;
            }
        }
    }

    // Метод для создания гигантского взрыва через серию меньших
    private void createMassiveExplosion(double x, double y, double z, double radius) {
        if (!this.level().isClientSide()) {
            // Базовый взрыв для визуального эффекта
            level().playSound(null, x, y, z, ModSoundEvents.PURPLE_EXPLOSION.get(), SoundSource.NEUTRAL, 3, 1f);
            // Ручное разрушение блоков в радиусе 50 блоков
            int radiusInt = (int) Math.ceil(radius);
            BlockPos center = new BlockPos((int) x, (int) y, (int) z);

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
                    float damage = 1000.0F * (float) (1.0 - Math.sqrt(distance) / radius); // Урон уменьшается с расстоянием
                    if (new HollowPurple().canAffect(entity)) {
                        entity.hurt(ModDamageSources.hollow_purple(this, this.owner), entity == owner ? damage / 10 : damage);
                        if (entity instanceof LivingEntity livingEntity) {
                            SkillEvent.Hit hitEvent = new SkillEvent.Hit(this.owner, getSkill(), livingEntity);
                            MinecraftForge.EVENT_BUS.post(hitEvent);
                        }
                    }
                }
            }

            // Визуальные эффекты
            this.level().addParticle(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER, x, y, z, 0, 0, 0);
            this.level().playSound(null, x, y, z, net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.NEUTRAL, 2.0F, 1.0F);
        }
    }

    public enum ATTACK_TYPE {
        MELEE,
        RANGED
    }

    public static class SpiralAnimation {

        // Статические значения по умолчанию
        private static SpiralMode defaultMode = SpiralMode.HYBRID;
        private static int defaultSpirals = 5;
        private static double defaultMaxRadius = 1.5;
        private static double defaultMinRadius = 0.1;
        private static double defaultNumTurns = 5;
        private static int defaultParticlesPerSpiral = 2;
        private static float defaultAnimationSpeed = 0.1f;
        private static double defaultParticleStep = 0.1;
        private static float defaultParticleSize = 0.5f;
        // Локальные значения для конкретной сущности
        private final SpiralMode mode;
        private final int spirals;
        private final double maxRadius;
        private final double minRadius;
        private final double numTurns;
        private final int particlesPerSpiral;
        private final float animationSpeed;
        private final double particleStep;
        private final float particleSize;
        private float spiralAnimation = 0.0f;
        public SpiralAnimation() {
            // При создании используем значения по умолчанию
            this.mode = defaultMode;
            this.spirals = defaultSpirals;
            this.maxRadius = defaultMaxRadius;
            this.minRadius = defaultMinRadius;
            this.numTurns = defaultNumTurns;
            this.particlesPerSpiral = defaultParticlesPerSpiral;
            this.animationSpeed = defaultAnimationSpeed;
            this.particleStep = defaultParticleStep;
            this.particleSize = defaultParticleSize;
        }

        // Статические сеттеры для значений по умолчанию
        public static void setDefaultMode(SpiralMode mode) {
            defaultMode = mode;
        }

        public static void setDefaultSpirals(int spirals) {
            defaultSpirals = spirals;
        }

        public static void setDefaultMaxRadius(double maxRadius) {
            defaultMaxRadius = maxRadius;
        }

        public static void setDefaultMinRadius(double minRadius) {
            defaultMinRadius = minRadius;
        }

        public static void setDefaultNumTurns(double numTurns) {
            defaultNumTurns = numTurns;
        }

        public static void setDefaultParticlesPerSpiral(int particlesPerSpiral) {
            defaultParticlesPerSpiral = particlesPerSpiral;
        }

        public static void setDefaultAnimationSpeed(float animationSpeed) {
            defaultAnimationSpeed = animationSpeed;
        }

        public static void setDefaultParticleStep(double particleStep) {
            defaultParticleStep = particleStep;
        }

        public static void setDefaultParticleSize(float particleSize) {
            defaultParticleSize = particleSize;
        }

        // Метод для генерации частиц
        public void spawnParticles(RedEntity entity, ServerLevel level, float progress) {
            double centerX = entity.getX();
            double centerY = entity.getY();
            double centerZ = entity.getZ();

            Vec3 lookVec = entity.owner.getLookAngle().normalize();
            Vec3 upVec = new Vec3(0, 1, 0);
            Vec3 rightVec = lookVec.cross(upVec).normalize();
            Vec3 planeUpVec = rightVec.cross(lookVec).normalize();

            DustParticleOptions coloredDust = new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), particleSize);

            spiralAnimation += animationSpeed;

            for (int spiral = 0; spiral < spirals; spiral++) {
                double baseAngle = 2 * Math.PI * spiral / spirals;
                int effectiveParticles = (mode == SpiralMode.SINGLE) ? 1 : particlesPerSpiral;

                for (int i = 0; i < effectiveParticles; i++) {
                    double s;
                    double r;
                    double theta;

                    switch (mode) {
                        case SPIRAL:
                            s = i / (double) (effectiveParticles - 1); // Полное заполнение
                            r = maxRadius - (maxRadius - minRadius) * s;
                            theta = baseAngle + numTurns * 2 * Math.PI * s + spiralAnimation;
                            break;
                        case SINGLE:
                            s = progress; // Одна частица движется к центру
                            r = maxRadius - (maxRadius - minRadius) * s;
                            theta = baseAngle + numTurns * 2 * Math.PI * (1.0 - s) + spiralAnimation;
                            break;
                        case HYBRID:
                        default:
                            s = progress + (i * particleStep); // Хвост движется
                            if (s > 1.0) s = 1.0;
                            r = maxRadius - (maxRadius - minRadius) * s;
                            theta = baseAngle + numTurns * 2 * Math.PI * (1.0 - s) + spiralAnimation;
                            break;
                    }

                    double xOffset = r * Math.cos(theta);
                    double yOffset = r * Math.sin(theta);

                    Vec3 particlePos = new Vec3(centerX, centerY, centerZ)
                            .add(rightVec.scale(xOffset))
                            .add(planeUpVec.scale(yOffset));

                    level.sendParticles(coloredDust, particlePos.x, particlePos.y, particlePos.z, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }

        public enum SpiralMode {
            SINGLE,
            SPIRAL,
            HYBRID
        }
    }
}
