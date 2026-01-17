package com.aleksiyflekssiy.tutorialmod.entity.behavior.nue;

import com.aleksiyflekssiy.tutorialmod.entity.NueEntity;
import com.aleksiyflekssiy.tutorialmod.entity.ai.NueAI;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMemoryModuleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class AscendToPoint extends Behavior<NueEntity> {
    Vec3 pos = null;

    public AscendToPoint(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition) {
        super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED, CustomMemoryModuleTypes.GRAB_TARGET.get(), MemoryStatus.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT), 0, 72000);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, NueEntity nue) {
        boolean bool = nue.getAttackPhase() == NueEntity.AttackPhase.ASCEND;
        boolean order = nue.getOrder() != NueEntity.NueOrder.MOVE;
        return bool && order;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, NueEntity nue, long pGameTime) {
        if (checkExtraStartConditions(level, nue)) {
            if (nue.getBrain().getMemory(MemoryModuleType.WALK_TARGET).isPresent()) {
                return nue.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get().getTarget().currentBlockPosition().distManhattan(nue.blockPosition()) > 5;
            }
        }
        return false;
    }


    @Override
    protected void start(ServerLevel level, NueEntity nue, long time) {
        int height;
        int distance;
        if (nue.getBrain().getMemory(CustomMemoryModuleTypes.GRABBED_ENTITY.get()).isPresent()) {
            distance = 40;
            height = 40;
        } else {
            distance = 20;
            height = 20;
        }
        BlockPos target = getPositionInDirection(distance, nue).above(height);
        pos = target.getCenter();
        nue.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, 1, 5));
        System.out.println("ASCEND PATH LENGTH - " + nue.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get().getTarget().currentBlockPosition().distManhattan(nue.blockPosition()));
    }

    @Override
    protected void stop(ServerLevel level, NueEntity nue, long time) {
        //ЧТО-ТО ОСТАНАВЛИВАЕТ ЭТО ПОСЛЕ ТИКА
        if (nue.getOrder() == NueEntity.NueOrder.GRAB || (nue.getOrder() == NueEntity.NueOrder.NONE && NueAI.checkAttackType(nue, "ATTACK"))) {
            //ИСПОЛЬЗУЕТСЯ СЛЕДУЮЩИЙ ТИП АТАКИ
            nue.dropGrabbedEntity();
        }
        //ОСТАНОВКА УЖЕ ПРЕДПОЛАГАЕТ ОТСУТСТВИЕ ТОЧКИ НАЗНАЧЕНИЯ
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
