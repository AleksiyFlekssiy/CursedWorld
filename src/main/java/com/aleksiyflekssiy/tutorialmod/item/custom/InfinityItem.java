package com.aleksiyflekssiy.tutorialmod.item.custom;

import com.aleksiyflekssiy.tutorialmod.capability.CursedEnergyCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import java.util.Iterator;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "tutorialmod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InfinityItem extends Item {
    private static final float MIN_RADIUS = 2.0F;
    private static final float MAX_RADIUS = 5.0F;
    private static final float REPEL_FORCE = 2.5F;
    private boolean isBlocking;
    private int tick = 0;
    private final int INTERVAL = 20;

    private static final Map<BlockPos, BlockState> changedBlocks = new HashMap<>();

    public InfinityItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        if (level.isClientSide()) return;
        if (player.getMainHandItem() == stack || player.getOffhandItem() == stack) {
            if (!player.getCooldowns().isOnCooldown(this)) applyInfinityEffect(player, level);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player &&
                (player.getMainHandItem().getItem() instanceof InfinityItem || player.getOffhandItem().getItem() instanceof InfinityItem)) {
            event.setCanceled(true); // Полная защита от урона
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getMainHandItem();
        if (level.isClientSide()) return InteractionResultHolder.fail(stack);
        if (!player.getCooldowns().isOnCooldown(this)){
            isBlocking = !isBlocking;
        }
        return InteractionResultHolder.success(stack);
    }

    private void applyInfinityEffect(Player player, Level level) {
        if (!CursedEnergyCapability.isEnoughEnergy(player, 1)) return;
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

            if (isCrouching) {
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
        if (isCrouching && level instanceof ServerLevel serverLevel) {
            spawnBarrierParticles(serverLevel, playerPos);
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
}