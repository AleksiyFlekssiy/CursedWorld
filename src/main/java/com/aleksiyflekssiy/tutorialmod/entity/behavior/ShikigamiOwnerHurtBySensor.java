package com.aleksiyflekssiy.tutorialmod.entity.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.Set;

public class ShikigamiOwnerHurtBySensor extends Sensor<LivingEntity> {
    @Override
    protected void doTick(ServerLevel level, LivingEntity entity) {
        Brain<?> brain = entity.getBrain();
        brain.getMemory(CustomMemoryModuleTypes.OWNER.get()).ifPresent(owner -> {
                LivingEntity lastHurtByMob = owner.getLastHurtByMob();
                if (lastHurtByMob != null && !lastHurtByMob.equals(entity) && !lastHurtByMob.equals(owner) && lastHurtByMob.isAlive()) {
                    brain.setMemory(MemoryModuleType.ATTACK_TARGET, lastHurtByMob);
                    brain.setMemory(CustomMemoryModuleTypes.GRAB_TARGET.get(), lastHurtByMob);
                }
            });

        brain.getMemory(CustomMemoryModuleTypes.OWNER_HURT_BY_ENTITY.get()).ifPresent(enemy ->{
            boolean inDistance = enemy.getAttribute(Attributes.FOLLOW_RANGE) != null && enemy.distanceToSqr(enemy) <= enemy.getAttributeValue(Attributes.FOLLOW_RANGE);
            if (!enemy.isAlive() || !inDistance) {
                brain.eraseMemory(CustomMemoryModuleTypes.OWNER_HURT_BY_ENTITY.get());
                brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
                brain.eraseMemory(CustomMemoryModuleTypes.GRAB_TARGET.get());
            }
        });
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return Set.of(MemoryModuleType.WALK_TARGET, CustomMemoryModuleTypes.OWNER.get(), CustomMemoryModuleTypes.OWNER_HURT_BY_ENTITY.get());
    }
}
