package com.aleksiyflekssiy.tutorialmod.entity.behavior.nue;

import com.aleksiyflekssiy.tutorialmod.entity.NueEntity;
import com.aleksiyflekssiy.tutorialmod.entity.navigation.FlyingMoveControl;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class AscendToPoint extends Behavior<NueEntity> {
    private int height = 20;
    private int distance = 20;
    private Vec3 pos = null;

    public AscendToPoint(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition) {
        super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED), 0, 72000);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, NueEntity nue) {
        boolean bool = nue.getAttackPhase() == NueEntity.AttackPhase.ASCEND;
        boolean order = nue.getOrder() != NueEntity.NueOrder.SIT;
        System.out.println("CHECK ASCEND: " + (bool && order));
        return bool && order;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, NueEntity nue, long pGameTime) {
        System.out.println("CAN STILL USE ASCEND");
//        FlyingMoveControl moveControl = (FlyingMoveControl) nue.getMoveControl();
//        return moveControl.getWantedPosition().distanceToSqr(nue.getX(), nue.getY(), nue.getZ()) > 1.0D;

//        WalkTarget walkTarget = null;
//        if (nue.getBrain().getMemory(MemoryModuleType.WALK_TARGET).isPresent()) walkTarget = nue.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get();
//        if (walkTarget != null) return walkTarget.getTarget().currentPosition().distanceToSqr(nue.getX(), nue.getY(), nue.getZ()) > 1;
//        else return false;
        if (checkExtraStartConditions(level, nue)) {
            if (nue.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
                Vec3 pos = nue.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get().getTarget().currentBlockPosition().getCenter();
                return pos.distanceToSqr(nue.getX(), nue.getY(), nue.getZ()) >= 1 && nue.getAttackPhase() == NueEntity.AttackPhase.ASCEND;
            }
        }
        return false;
    }

    @Override
    protected void start(ServerLevel level, NueEntity nue, long time) {
        System.out.println("START ASCEND");
        if (nue.getOrder() == NueEntity.NueOrder.GRAB){
            distance = 40;
            height = 40;
        }
        else {
            distance = 20;
            height = 20;
        }
        BlockPos target = getPositionInDirection(distance, nue).above(height);
        //nue.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0D);
        pos = target.getCenter();
        nue.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, 1, 1));
        //nue.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1);
    }

    @Override
    protected void tick(ServerLevel level, NueEntity nue, long time) {
        System.out.println("ASCEND");

        if (nue.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)){
            Vec3 pos = nue.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get().getTarget().currentPosition();
            if (pos.distanceToSqr(nue.getX(), nue.getY(), nue.getZ()) < 1) {
                this.stop(level, nue, time);
            }
        }
    }

    @Override
    protected void stop(ServerLevel level, NueEntity nue, long time) {
        System.out.println("STOP ASCEND");
        if (nue.getOrder() == NueEntity.NueOrder.GRAB){
            nue.dropGrabbedEntity();
        }
        nue.setAttackPhase(NueEntity.AttackPhase.SWOOP);
    }

    public BlockPos getPositionInDirection(double distance, NueEntity nue) {
        // Получаем текущий угол поворота (yaw) в градусах
        float yaw = nue.getYRot();
        // Преобразуем в радианы
        double yawRadians = Math.toRadians(yaw);

        // Вычисляем смещение по X и Z
        double deltaX = distance * Math.cos(yawRadians);
        double deltaZ = distance * Math.sin(yawRadians);

        // Текущая позиция моба
        double currentX = nue.getX();
        double currentZ = nue.getZ();
        double currentY = nue.getY(); // Y остаётся неизменным

        // Новая позиция
        double newX = currentX + deltaX;
        double newZ = currentZ + deltaZ;

        // Преобразуем в BlockPos
        return BlockPos.containing(newX, currentY, newZ);
    }
}
