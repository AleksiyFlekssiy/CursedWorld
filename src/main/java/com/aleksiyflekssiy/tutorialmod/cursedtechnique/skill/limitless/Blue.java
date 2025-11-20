package com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.limitless;

import com.aleksiyflekssiy.tutorialmod.capability.CursedEnergyCapability;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.Skill;
import com.aleksiyflekssiy.tutorialmod.entity.BlueEntity;
import com.aleksiyflekssiy.tutorialmod.entity.ModEntities;
import com.aleksiyflekssiy.tutorialmod.particle.ModParticles;
import com.aleksiyflekssiy.tutorialmod.sound.ModSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class Blue extends Skill {
    private static final float PULL_RANGE = 10;
    private static final float PULL_FORCE = 1.5F;
    private static final float TELEPORT_RANGE = 25;
    private int CHANT = 0;
    public final Pull pull = new Pull();
    public final Teleport teleport = new Teleport();
    public final BlueSummon blueSummon = new BlueSummon();

    public void use(LivingEntity entity, UseType type, int charge){
        if (!(entity instanceof ServerPlayer)) return;
        switch (type){
            case ACTIVATION -> {
                if (entity.isCrouching()) teleport.activate(entity);
                else pull.activate(entity);
            }
            case CHARGING -> blueSummon.charge(entity, charge);
            case RELEASING -> blueSummon.release(entity);
        }
    }

    public class Pull extends Skill{

        // Длительность следа: 0.5 секунды (10 тиков)
        public void activate(LivingEntity entity){
            if (entity.level().isClientSide() || !(entity instanceof ServerPlayer player)) return;
                AABB pullArea = new AABB(player.position().add(-PULL_RANGE, -PULL_RANGE, -PULL_RANGE), player.position().add(PULL_RANGE, PULL_RANGE, PULL_RANGE));
                List<Entity> entitiesSelf = player.level().getEntitiesOfClass(Entity.class, pullArea);
                for (Entity affectedEntity : entitiesSelf) {
                    Vec3 toPlayer = player.position().subtract(affectedEntity.position()).normalize().scale(PULL_FORCE * 2);
                    affectedEntity.setDeltaMovement(affectedEntity.getDeltaMovement().add(toPlayer));
                    spawnTrailParticles((ServerLevel) player.level(), affectedEntity);
                }
        }

        public void spawnTrailParticles(ServerLevel level, Entity entity) {
            // Спавним частицы в текущей позиции сущности
            double x = entity.getX();
            double y = entity.getY() + entity.getBbHeight() / 2; // Центр сущности по высоте
            double z = entity.getZ();

            // Спавним синие частицы (например, minecraft:witch с синим оттенком)
            level.sendParticles(ModParticles.BLUE_PULL.get(), x, y, z, 3, 0.2, 0.2, 0.2, 0.0);
        }

        @Override
        public String getName() {
            return "Pull";
        }

        @Override
        public ResourceLocation getSkillIcon() {
            return null;
        }
    }

    public class Teleport extends Skill{

        public void activate(LivingEntity entity){
            if (entity instanceof ServerPlayer player) {
                    Vec3 eyePos = player.getEyePosition(1.0F); // Позиция глаз игрока
                    Vec3 lookVec = player.getViewVector(1.0F); // Вектор взгляда
                    Vec3 endPos = eyePos.add(lookVec.x * TELEPORT_RANGE, lookVec.y * TELEPORT_RANGE, lookVec.z * TELEPORT_RANGE);

                    // Трассировка взгляда
                    ClipContext context = new ClipContext(eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
                    HitResult result = player.level().clip(context);

                    if (result.getType() == HitResult.Type.BLOCK) {
                        // Если попали в блок, телепортируемся на верхнюю поверхность
                        BlockHitResult blockHit = (BlockHitResult) result;
                        int blockX = blockHit.getBlockPos().getX();
                        int blockY = blockHit.getBlockPos().getY();
                        int blockZ = blockHit.getBlockPos().getZ();

                        // Устанавливаем точку на верхней грани блока
                        Vec3 teleportPos = new Vec3(blockX + 0.5, blockY + 1.0, blockZ + 0.5);
                        player.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
                    } else {
                        // Если в воздухе, телепортируемся на максимальную дистанцию
                        player.teleportTo(endPos.x, endPos.y, endPos.z);
                    }
            }
        }

        @Override
        public String getName() {
            return "Teleport";
        }

        @Override
        public ResourceLocation getSkillIcon() {
            return null;
        }
    }

    public class BlueSummon extends Skill{

        public void release(LivingEntity entity){
            if (!entity.level().isClientSide()) {
                Level level = entity.level();
                boolean isFollowing = false;
                if (entity instanceof Player player) {
                    if (!CursedEnergyCapability.isEnoughEnergy(player, 10)) return;
                    if (player.isCrouching()) {
                        isFollowing = true;
                    }
                    BlueEntity blue = new BlueEntity(ModEntities.BLUE_ENTITY.get(), level, player, isFollowing, 60, 5, 3, 1.5f, CHANT);
                    createSimpleBlue(level, player, blue);
                    CHANT = 0;
                    CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 10);
                }
            }
        }

        public void charge(LivingEntity livingEntity, int chargeTicks) {
            if (livingEntity instanceof Player player &&  player.level() instanceof ServerLevel serverLevel) {
                // Проверка достижения фаз
                if (chargeTicks == 100 && CHANT == 2) {
                    if (!CursedEnergyCapability.isEnoughEnergy(player, 10)) return;
                    player.sendSystemMessage(Component.literal("Eyes of Wisdom"));
                    CHANT = 3;
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundEvents.CHANT_3.get(), SoundSource.NEUTRAL, 1f, 1f);
                    CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 10);
                } else if (chargeTicks == 75 && CHANT == 1) {
                    if (!CursedEnergyCapability.isEnoughEnergy(player, 10)) return;
                    player.sendSystemMessage(Component.literal("Twilight"));
                    CHANT = 2;
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundEvents.CHANT_2.get(), SoundSource.NEUTRAL, 1f, 1f);
                    CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 10);
                } else if (chargeTicks == 50 && CHANT == 0) {
                    if (!CursedEnergyCapability.isEnoughEnergy(player, 10)) return;
                    player.sendSystemMessage(Component.literal("Phase"));
                    CHANT = 1;
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundEvents.CHANT_1.get(), SoundSource.NEUTRAL, 1f, 1f);
                    CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 10);
                }
                spawnRotatingParticles(serverLevel, player.position(), 5f, 10, chargeTicks);
            }
        }

        private void createSimpleBlue(Level level, Player player, BlueEntity blue) {
            Vec3 eyePos = player.getEyePosition(1.0f);
            Vec3 lookVec = player.getLookAngle();
            Vec3 maxReach = eyePos.add(lookVec.scale(10.0));
            ClipContext clipContext = new ClipContext(eyePos, maxReach, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
            BlockHitResult hitResult = level.clip(clipContext);

            Vec3 targetPos = hitResult.getType() == HitResult.Type.BLOCK ?
                    hitResult.getLocation() :
                    eyePos.add(lookVec.scale(10.0));

            blue.setPos(targetPos.x, targetPos.y, targetPos.z);
            level.addFreshEntity(blue);
        }

        private void spawnRotatingParticles(ServerLevel level, Vec3 center, float radius, float density, int holdTicks) {
            // Угол вращения зависит от времени (holdTicks), чтобы создать эффект спирали
            double angle = (holdTicks % 40) * Math.PI / 20; // Полный оборот каждые 2 секунды (40 тиков)
            double particleRadius = radius * 0.5; // Радиус вращения частиц меньше радиуса притяжения

            // Спавним частицы по кругу
            for (int i = 0; i < density; i++) {
                double offsetAngle = angle + (i * 2 * Math.PI / density);
                double x = center.x + particleRadius * Math.cos(offsetAngle);
                double z = center.z + particleRadius * Math.sin(offsetAngle);
                double y = center.y + (Math.sin(angle + i) * 0.5); // Добавляем небольшое колебание по Y

                level.sendParticles(ModParticles.BLUE_PULL.get(), x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        @Override
        public String getName() {
            return "BlueSummon";
        }

        @Override
        public ResourceLocation getSkillIcon() {
            return null;
        }
    }


    public String getName(){
        return "Blue";
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/gui/blue.png");
    }
}
