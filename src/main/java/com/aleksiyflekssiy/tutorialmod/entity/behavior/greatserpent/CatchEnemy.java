package com.aleksiyflekssiy.tutorialmod.entity.behavior.greatserpent;

import com.aleksiyflekssiy.tutorialmod.entity.GreatSerpentEntity;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.AABB;

import java.util.Map;

public class CatchEnemy extends Behavior<GreatSerpentEntity> {
    private LivingEntity target = null;

    public CatchEnemy(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition) {
        super(pEntryCondition, 0, 72000);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, GreatSerpentEntity serpent) {
        return serpent.getBrain().hasMemoryValue(CustomMemoryModuleTypes.GRAB_TARGET.get())
                && (serpent.getOrder() == GreatSerpentEntity.GreatSerpentOrder.CATCH ||
                serpent.getOrder() == GreatSerpentEntity.GreatSerpentOrder.SMASH);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, GreatSerpentEntity serpent, long gameTime) {
        return serpent.getBrain().hasMemoryValue(CustomMemoryModuleTypes.GRAB_TARGET.get());
    }

    @Override
    protected void start(ServerLevel pLevel, GreatSerpentEntity serpent, long pGameTime) {
        this.target = serpent.getBrain().getMemory(CustomMemoryModuleTypes.GRAB_TARGET.get()).get();
        System.out.println("Catch Start");
    }

    @Override
    protected void tick(ServerLevel level, GreatSerpentEntity serpent, long gameTime) {
        if (!serpent.getBrain().hasMemoryValue(CustomMemoryModuleTypes.GRABBED_ENTITY.get())) {
            serpent.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, 1, 1));
                AABB aabb = serpent.getBoundingBox().inflate(1);
                if (aabb.intersects(target.getBoundingBox())) {
                    boolean success = target.startRiding(serpent, true);
                    if (success) {
                        serpent.getBrain().setMemory(CustomMemoryModuleTypes.GRABBED_ENTITY.get(), target);
                        System.out.println("Grabbed");
                        serpent.getBrain().eraseMemory(CustomMemoryModuleTypes.GRAB_TARGET.get());
                    }
                }
        }
    }

    @Override
    protected void stop(ServerLevel pLevel, GreatSerpentEntity pEntity, long pGameTime) {
        System.out.println("Catch Stop");
        super.stop(pLevel, pEntity, pGameTime);
    }
}
