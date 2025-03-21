package com.aleksiyflekssiy.tutorialmod.cursed_technique.skill;

import com.aleksiyflekssiy.tutorialmod.capability.CursedEnergyCapability;
import com.aleksiyflekssiy.tutorialmod.effect.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "tutorialmod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Infinity extends Skill {
    private static final float MIN_RADIUS = 2.0F;
    private static final float MAX_RADIUS = 5.0F;
    private static final float REPEL_FORCE = 2.5F;
    private boolean isBlocking;
    private boolean isEnabled;
    private boolean isOutputIncreased;
    private int tick = 0;
    public final InfinitySwitch infinitySwitch = new InfinitySwitch();
    public final IncreaseInfinityOutput increaseInfinityOutput = new IncreaseInfinityOutput();

    private static final Map<BlockPos, BlockState> changedBlocks = new HashMap<>();

    public Infinity(){
        this.subSkills = new HashSet<>();
    }

    public void use(LivingEntity entity, UseType type, int charge){
        switch (type){
            case ACTIVATION -> infinitySwitch.activate(entity);
            case DISACTIVATION -> infinitySwitch.disactivate(entity);
            case CHARGING -> increaseInfinityOutput.charge(entity, charge);
            case RELEASING -> increaseInfinityOutput.release(entity);
        }
    }


    public class InfinitySwitch extends Skill{

        public boolean isEnabled(){
            return isEnabled;
        }

        public void activate(LivingEntity entity){
            if (!entity.isCrouching()) {
                if (isEnabled) entity.removeEffect(ModEffects.INFINITY.get());
                else entity.addEffect(new MobEffectInstance(ModEffects.INFINITY.get(), MobEffectInstance.INFINITE_DURATION));
                isEnabled = !isEnabled;
            }
            else isBlocking = !isBlocking;
        }

        public void applyInfinityEffect(Player player, Level level) {
            if (!CursedEnergyCapability.isEnoughEnergy(player, 1)) return;
            int INTERVAL = 20;
            if (tick >= INTERVAL){
                CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 1);
                tick = 0;
            }
            else tick++;
            Vec3 playerPos = player.position();
            boolean isCrouching = player.isCrouching();
            BlockPos posUnderPlayer = player.blockPosition().below();
            BlockState currentState = level.getBlockState(posUnderPlayer);
            if (isBlocking && !player.isCrouching()){
                if (currentState.isAir() && !changedBlocks.containsKey(posUnderPlayer)) {
                    BlockState newState = Blocks.BARRIER.defaultBlockState();
                    level.setBlock(posUnderPlayer, newState, 3);
                    changedBlocks.put(posUnderPlayer.immutable(), currentState); // Сохраняем исходное состояние
                }
            }
            // Проверяем все изменённые блоки и возвращаем их, если игрок не на них
            Iterator<Map.Entry<BlockPos, BlockState>> iterator = changedBlocks.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<BlockPos, BlockState> entry = iterator.next();
                BlockPos changedPos = entry.getKey();
                BlockState originalState = entry.getValue();

                // Если игрок не стоит на этом блоке
                if (!changedPos.equals(posUnderPlayer)) {
                    level.setBlock(changedPos, originalState, 3);
                    iterator.remove(); // Удаляем из списка
                }
            }
            AABB area = new AABB(playerPos.x - MAX_RADIUS, playerPos.y - MAX_RADIUS, playerPos.z - MAX_RADIUS,
                    playerPos.x + MAX_RADIUS, playerPos.y + MAX_RADIUS, playerPos.z + MAX_RADIUS);

            for (Entity entity : level.getEntitiesOfClass(Entity.class, area)) {
                if (entity == player) continue;

                Vec3 entityPos = entity.position();
                double distance = playerPos.distanceTo(entityPos);
                if (distance > MAX_RADIUS) continue;

                Vec3 direction = entityPos.subtract(playerPos).normalize();
                Vec3 currentMotion = entity.getDeltaMovement();

                if (isOutputIncreased) {
                    repelEntity(entity, direction, level, distance);

                } else if (distance <= MIN_RADIUS) {
                    entity.setDeltaMovement(direction.scale(0.2));
                    entity.hurtMarked = true;
                } else {
                    slowdownEntity(entity, distance, currentMotion);
                }

                spawnAmbientParticles(entityPos, level, isCrouching);
            }
            // Спавним сферический барьер при приседании
            if (isOutputIncreased && level instanceof ServerLevel serverLevel) {
                spawnBarrierParticles(serverLevel, playerPos);
            }
        }

        private static void slowdownEntity(Entity entity, double distance, Vec3 currentMotion) {
            double slowdownFactor = Math.max(0, (distance - MIN_RADIUS) / (MAX_RADIUS - MIN_RADIUS));
            Vec3 newMotion = currentMotion.scale(slowdownFactor);
            entity.setDeltaMovement(newMotion);
            entity.hurtMarked = true;
        }

        private static void repelEntity(Entity entity, Vec3 direction, Level level, double distance) {
            Vec3 newMotion = direction.scale(REPEL_FORCE);
            entity.setDeltaMovement(newMotion);
            entity.hurtMarked = true;

            applyPressureDamage(entity, level, distance);
        }

        private static void applyPressureDamage(Entity entity, Level level, double distance) {
            if (entity.horizontalCollision) {
                // Урон = (сила отталкивания / расстояние) для обратной пропорции
                float damage = (float) (REPEL_FORCE * (MAX_RADIUS - Math.max(0.1, distance))); // Избегаем деления на 0
                entity.hurt(entity.damageSources().magic(), damage);

                Vec3 entityPos = entity.position();
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.CRIT,
                            entityPos.x, entityPos.y, entityPos.z,
                            10, 0.2, 0.2, 0.2, 0.1
                    );
                }
            }
        }

        private static void spawnBarrierParticles(ServerLevel level, Vec3 center) {
            int particleCount = 625; // Увеличиваем для более равномерной сферы
            for (int i = 0; i < particleCount; i++) {
                // Равномерное распределение по сфере с использованием случайных углов
                double theta = 2 * Math.PI * level.random.nextDouble(); // 0..2π
                double phi = Math.acos(2 * level.random.nextDouble() - 1); // 0..π

                double x = MAX_RADIUS * Math.sin(phi) * Math.cos(theta);
                double y = MAX_RADIUS * Math.sin(phi) * Math.sin(theta);
                double z = MAX_RADIUS * Math.cos(phi);

                Vec3 particlePos = center.add(x, y, z);

                ClipContext clipContext = new ClipContext(center, particlePos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
                BlockHitResult hitResult = level.clip(clipContext);

                if (hitResult.getType() == BlockHitResult.Type.BLOCK) {
                    // Если есть столкновение, берём точку перед блоком
                    particlePos = hitResult.getLocation();
                }

                level.sendParticles(
                        ParticleTypes.ENCHANTED_HIT,
                        particlePos.x, particlePos.y, particlePos.z,
                        1, 0, 0, 0, 0.0
                );
            }
        }

        private static void spawnAmbientParticles(Vec3 entityPos, Level level, boolean isCrouching) {
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.ENCHANT,
                        entityPos.x, entityPos.y + 1.0, entityPos.z,
                        isCrouching ? 5 : 1,
                        0.2, 0.2, 0.2,
                        0.05
                );
            }
        }

        @Override
        public String getName() {
            return "InfinitySwitchSubSkill";
        }

        @Override
        public ResourceLocation getSkillIcon() {
            return null;
        }
    }

    public class IncreaseInfinityOutput extends Skill{

        public void charge(LivingEntity entity, int charge){
            if (!isEnabled) return;
            isOutputIncreased = true;
        }

        @Override
        public void release(LivingEntity entity) {
            if (!isEnabled) return;
            isOutputIncreased = false;
        }

        @Override
        public String getName() {
            return "IncreaseInfinityOutputSubSkill";
        }

        @Override
        public ResourceLocation getSkillIcon() {
            return null;
        }
    }

    @Override
    public String getName() {
        return "Infinity";
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity().hasEffect(ModEffects.INFINITY.get())){
            event.setCanceled(true);
        }
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return new ResourceLocation("tutorialmod", "textures/gui/infinity.png");
    }

}
