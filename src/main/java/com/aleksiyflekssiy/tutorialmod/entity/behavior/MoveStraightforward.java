package com.aleksiyflekssiy.tutorialmod.entity.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class MoveStraightforward extends Behavior<Mob> {
    private Vec3 deltaMovement;
    private WalkTarget walkTarget;

    public MoveStraightforward(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition) {
        super(pEntryCondition, 0, 72000);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, Mob mob) {
        return mob.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET) &&
                (mob.getBrain().hasMemoryValue(CustomMemoryModuleTypes.GRAB_TARGET.get()) ||
                mob.getBrain().hasMemoryValue(CustomMemoryModuleTypes.GRABBED_ENTITY.get()));
    }

    @Override
    protected boolean canStillUse(ServerLevel pLevel, Mob pEntity, long pGameTime) {
        if (!isInBorders(pEntity.position())) {
            System.out.println("Not in borders");
            return false;
        }
        if (!pEntity.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)){
            System.out.println("No walk target");
            return false;
        }
        return true;
        //return isInBorders(pEntity.position()) && pEntity.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected void start(ServerLevel level, Mob mob, long pGameTime) {
        deltaMovement = mob.getDeltaMovement();
        walkTarget = mob.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get();
        System.out.println("Move start");
    }

    @Override
    protected void tick(ServerLevel level, Mob mob, long pGameTime) {
        mob.setDeltaMovement(deltaMovement);
        System.out.println("Moving to " + walkTarget.getTarget().currentBlockPosition() + " with speed " + mob.getDeltaMovement());
    }

    @Override
    protected void stop(ServerLevel pLevel, Mob pEntity, long pGameTime) {
        System.out.println("Move stop");
        pEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        pEntity.setDeltaMovement(Vec3.ZERO);
    }

    private boolean isInBorders(Vec3 vec) {
        return walkTarget.getTarget().currentPosition().distanceToSqr(vec) > deltaMovement.lengthSqr();
    }
}
