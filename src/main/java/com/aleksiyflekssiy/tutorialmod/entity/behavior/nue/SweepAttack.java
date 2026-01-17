package com.aleksiyflekssiy.tutorialmod.entity.behavior.nue;

import com.aleksiyflekssiy.tutorialmod.entity.NueEntity;

import com.aleksiyflekssiy.tutorialmod.entity.ai.NueAI;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class SweepAttack extends Behavior<NueEntity> {

    public SweepAttack(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition) {
        super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED, CustomMemoryModuleTypes.GRAB_TARGET.get(), MemoryStatus.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT), 0, 72000);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, NueEntity nue) {
        LivingEntity target = getRequiredTarget(nue);
        boolean bool = target != null && target.isAlive() && nue.getAttackPhase() == NueEntity.AttackPhase.SWOOP;
        boolean order = nue.getOrder() != NueEntity.NueOrder.MOVE;
        return bool && order;
    }

    private LivingEntity getRequiredTarget(NueEntity nue) {
        if (nue.getOrder() == NueEntity.NueOrder.NONE){
            if (nue.getBrain().getMemory(CustomMemoryModuleTypes.ATTACK_TYPE.get()).isPresent()) {
                switch (nue.getBrain().getMemory(CustomMemoryModuleTypes.ATTACK_TYPE.get()).get()) {
                    case "ATTACK" -> {
                        return nue.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
                    }
                    case "GRAB" -> {
                        return nue.getBrain().getMemory(CustomMemoryModuleTypes.GRAB_TARGET.get()).orElse(null);
                    }
                }
            }
        }
        else if (nue.getOrder() == NueEntity.NueOrder.ATTACK) {
            return nue.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        }
        else if (nue.getOrder() == NueEntity.NueOrder.GRAB) {
            return nue.getBrain().getMemory(CustomMemoryModuleTypes.GRAB_TARGET.get()).orElse(null);
        }
        //System.out.println("WTF WITH THIS SHIT");
        return null;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, NueEntity nue, long time) {
        LivingEntity livingentity = getRequiredTarget(nue);
        if (livingentity == null) {
            return false;
        } else if (!livingentity.isAlive()) {
            return false;
        } else {
            if (livingentity instanceof Player player) {
                if (livingentity.isSpectator() || player.isCreative()) {
                    return false;
                }
            }
            return this.checkExtraStartConditions(level, nue);
        }
    }

    @Override
    protected void start(ServerLevel pLevel, NueEntity nue, long pGameTime) {

    }

    @Override
    protected void tick(ServerLevel level, NueEntity nue, long time) {
        LivingEntity livingentity = getRequiredTarget(nue);
        if (livingentity != null) {
            nue.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(livingentity.blockPosition(), 1, 5));
            if (nue.getOrder() == NueEntity.NueOrder.GRAB ||
                    (nue.getOrder() == NueEntity.NueOrder.NONE && NueAI.checkAttackType(nue, "GRAB"))) {
                if (nue.getBoundingBox().inflate(0.5F).intersects(livingentity.getBoundingBox()) && nue.checkGrabCooldown()) {
                    System.out.println("SOMEONE IS THERE");
                    nue.tryGrabEntityBelow(livingentity);
                    setNextAttackType(nue);
                    this.stop(level, nue, time);
                }
            }
            else if (nue.getOrder() == NueEntity.NueOrder.ATTACK ||
                    (nue.getOrder() == NueEntity.NueOrder.NONE && NueAI.checkAttackType(nue, "ATTACK"))){
                if (nue.getBoundingBox().inflate(0.5F).intersects(livingentity.getBoundingBox()) && nue.checkAttackCooldown()) {
                    nue.doHurtTarget(livingentity);
                    setNextAttackType(nue);
                    this.stop(level, nue, time);
                } else if (nue.horizontalCollision) {
                    this.stop(level, nue, time);
                }
            }
        }
    }

    private void setNextAttackType(NueEntity nue) {
        if (nue.getOrder() == NueEntity.NueOrder.NONE) {
            switch (nue.getBrain().getMemory(CustomMemoryModuleTypes.ATTACK_TYPE.get()).get()){
                case "ATTACK" -> {
                    nue.setAttackCooldown();
                    nue.setGrabCooldown();
                    nue.getBrain().setMemory(CustomMemoryModuleTypes.ATTACK_TYPE.get(), "GRAB");
                }
                case "GRAB" -> {
                    nue.setAttackCooldown();
                    nue.setGrabCooldown();
                    nue.getBrain().setMemory(CustomMemoryModuleTypes.ATTACK_TYPE.get(), "ATTACK");
                }
            }
        }
    }

    @Override
    protected void stop(ServerLevel level, NueEntity nue, long time) {
        nue.setAttackPhase(NueEntity.AttackPhase.ASCEND);
    }
}
