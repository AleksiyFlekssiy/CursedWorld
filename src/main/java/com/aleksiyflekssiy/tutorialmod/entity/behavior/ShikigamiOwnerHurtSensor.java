package com.aleksiyflekssiy.tutorialmod.entity.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.Set;

public class ShikigamiOwnerHurtSensor extends Sensor<LivingEntity> {
    @Override
    protected void doTick(ServerLevel level, LivingEntity entity) {
        Brain<?> brain = entity.getBrain();
        brain.getMemory(CustomMemoryModuleTypes.OWNER.get()).ifPresent(owner -> {
            LivingEntity lastHurtMob = owner.getLastHurtMob();
            if (lastHurtMob != null && !lastHurtMob.equals(entity) && !lastHurtMob.equals(owner) && lastHurtMob.isAlive()) {
                brain.setMemory(MemoryModuleType.ATTACK_TARGET, lastHurtMob);
                brain.setMemory(CustomMemoryModuleTypes.GRAB_TARGET.get(), lastHurtMob);
            }
        });
        brain.getMemory(CustomMemoryModuleTypes.OWNER_HURT.get()).ifPresent(enemy ->{
            boolean inDistance = enemy.getAttribute(Attributes.FOLLOW_RANGE) != null && enemy.distanceToSqr(enemy) <= enemy.getAttributeValue(Attributes.FOLLOW_RANGE);
            if (!enemy.isAlive() || !inDistance) {
                brain.eraseMemory(CustomMemoryModuleTypes.OWNER_HURT.get());
                brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
                brain.eraseMemory(CustomMemoryModuleTypes.GRAB_TARGET.get());
            }
        });
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return Set.of(CustomMemoryModuleTypes.OWNER.get(), CustomMemoryModuleTypes.OWNER_HURT.get());
    }
}
