package com.aleksiyflekssiy.tutorialmod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class CustomExplosion {
    /**
     * Вызывает кастомный взрыв в указанной точке.
     *
     * @param level         Мир, где происходит взрыв
     * @param center        Координаты центра взрыва
     * @param radius        Радиус взрыва
     * @param strength      Максимальная сила взрыва в центре
     * @param dropItems     Дропать ли предметы с разрушенных блоков
     * @param damageEntities Наносить ли урон сущностям
     */
    public static void createExplosion(ServerLevel level, Vec3 center, float radius, float strength, boolean dropItems, boolean damageEntities) {
        int radiusInt = (int) Math.ceil(radius);
        BlockPos centerPos = new BlockPos((int)center.x, (int)center.y, (int)center.z);
        Random random = new Random();

        // Разрушение блоков
        for (int x = -radiusInt; x <= radiusInt; x++) {
            for (int y = -radiusInt; y <= radiusInt; y++) {
                for (int z = -radiusInt; z <= radiusInt; z++) {
                    BlockPos pos = centerPos.offset(x, y, z);
                    double distanceSq = center.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    double distance = Math.sqrt(distanceSq);

                    if (distance <= radius) {
                        float explosionStrength = strength * (1.0F - (float)distance / radius);
                        BlockState state = level.getBlockState(pos);

                        // Разрушение с учётом прочности
                        if (!state.isAir() && explosionStrength > state.getDestroySpeed(level, pos) && state.getDestroySpeed(level, pos) >= 0) {
                            level.destroyBlock(pos, dropItems);
                            // Частицы с шансом 30%
                            if (random.nextFloat() < 0.3F) {
                                level.sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                        1, 0.1, 0.1, 0.1, 0.0);
                            }
                        }
                    }
                }
            }
        }

        // Урон сущностям (опционально)
        if (damageEntities) {
            AABB aabb = new AABB(center.add(-radius, -radius, -radius), center.add(radius, radius, radius));
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
                double dist = center.distanceTo(entity.position());
                float damage = strength * (1.0F - (float)dist / radius);
                if (damage > 0) {
                    entity.hurt(level.damageSources().explosion(null), damage);
                }
            }
        }

        // Звук взрыва
        level.playSound(null, centerPos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 2.0F, 1.0F);
    }
}
