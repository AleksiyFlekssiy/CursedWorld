package com.aleksiyflekssiy.tutorialmod.entity.behavior.toad;

import com.aleksiyflekssiy.tutorialmod.entity.ToadEntity;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class TongueCatch extends Behavior<ToadEntity> {

    public TongueCatch(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition) {
        super(pEntryCondition, 0, 72000);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, ToadEntity toad) {
        if (toad.getBrain().hasMemoryValue(CustomMemoryModuleTypes.GRABBED_ENTITY.get())) return false;
        if (toad.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent()) {
            if (!toad.isTamed()) return true;
            else return toad.getOrder() != ToadEntity.ToadOrder.MOVE;
        }
        return false;
    }

    @Override
    protected void start(ServerLevel level, ToadEntity toad, long pGameTime) {
        System.out.println("START CATCH " + toad.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get().getClass().getSimpleName());
        toad.getNavigation().stop(); // Останавливаем движение жабы
        toad.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(target -> {
                toad.getLookControl().setLookAt(target);
                Vec3 startPos = toad.getEyePosition(); // Позиция глаз жабы
                Vec3 endPos = startPos.add(toad.getViewVector(1).scale(50)); // Дальность языка (30 блоков)
                EntityHitResult result = ProjectileUtil.getEntityHitResult(toad.level(), toad, startPos, endPos, new AABB(startPos, endPos), entity -> entity instanceof LivingEntity && !entity.equals(toad));
                if (result != null && result.getEntity() instanceof LivingEntity entity) {
                    initializeCatch(toad, entity);
                    System.out.println("CATCHED: " + entity.getClass().getSimpleName());
                }
        });
    }

    private void initializeCatch(ToadEntity toad, LivingEntity target) {
        toad.getBrain().setMemory(CustomMemoryModuleTypes.GRABBED_ENTITY.get(), target);
        toad.setDistance((float) toad.position().subtract(target.position()).length());
        toad.getLookControl().setLookAt(target);
        String attackType = toad.getBrain().getMemory(CustomMemoryModuleTypes.ATTACK_TYPE.get()).orElse("NULL");
        switch (attackType){
            case "NULL", "SWING" -> toad.getBrain().setMemory(CustomMemoryModuleTypes.ATTACK_TYPE.get(), "PULL");
            case "PULL" -> toad.getBrain().setMemory(CustomMemoryModuleTypes.ATTACK_TYPE.get(), "IMMOBILIZE");
            case "IMMOBILIZE" -> toad.getBrain().setMemory(CustomMemoryModuleTypes.ATTACK_TYPE.get(), "SWING");
        }
    }

    public void stop(ServerLevel level, ToadEntity toad, long pGameTime) {
        System.out.println("STOP CATCH");
    }
}
