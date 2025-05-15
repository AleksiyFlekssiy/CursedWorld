package com.aleksiyflekssiy.tutorialmod.entity.behavior.nue;

import com.aleksiyflekssiy.tutorialmod.entity.NueEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Optional;

public class SweepAttack extends Behavior<NueEntity> {

    public SweepAttack(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition) {
        super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED), 0, 72000);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, NueEntity nue) {
        LivingEntity target = nue.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        boolean bool = target != null && target.isAlive() && nue.getAttackPhase() == NueEntity.AttackPhase.SWOOP;
        boolean order = nue.getOrder() != NueEntity.NueOrder.SIT;
        System.out.println("CHECK SWEEP: " + (bool && order));
        return bool && order;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, NueEntity nue, long time) {
        System.out.println("CAN STILL USE SWEEP");
        LivingEntity livingentity = nue.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (livingentity == null) {
            return false;
        } else if (!livingentity.isAlive()) {
            return false;
        } else {
            if (livingentity instanceof Player) {
                Player player = (Player) livingentity;
                if (livingentity.isSpectator() || player.isCreative()) {
                    return false;
                }
            }

            return this.checkExtraStartConditions(level, nue);
        }
    }

    @Override
    protected void tick(ServerLevel level, NueEntity nue, long time) {
        System.out.println("SWEEP");
        LivingEntity livingentity = nue.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (livingentity != null) {
            //nue.getMoveControl().setWantedPosition(livingentity.getX(), livingentity.getY(0.5), livingentity.getZ(), 1);
            //nue.getNavigation().moveTo(livingentity.getX(), livingentity.getY(0.5), livingentity.getZ(), 1);
            nue.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(livingentity.position(), 1, 1));
            if (nue.getOrder() == NueEntity.NueOrder.GRAB) {
                if (nue.getBoundingBox().inflate(0.5F).intersects(livingentity.getBoundingBox())) {
                    nue.tryGrabEntityBelow();
                    this.stop(level, nue, time);
                }
            }
            else {
                if (nue.getBoundingBox().inflate(0.5F).intersects(livingentity.getBoundingBox())) {
                    nue.doHurtTarget(livingentity);
                    if (!nue.isSilent()) {
                        nue.level().levelEvent(1039, nue.blockPosition(), 0);
                    }
                    System.out.println("ATTACK");
                    this.stop(level, nue, time);
                } else if (nue.horizontalCollision) {
                    nue.setAttackPhase(NueEntity.AttackPhase.ASCEND);
                    this.stop(level, nue, time);
                }
            }
        }
        else System.out.println("where?");
    }

    @Override
    protected void stop(ServerLevel level, NueEntity nue, long time) {
        System.out.println("SWEEP STOP");
        nue.setAttackPhase(NueEntity.AttackPhase.ASCEND);
    }
}
