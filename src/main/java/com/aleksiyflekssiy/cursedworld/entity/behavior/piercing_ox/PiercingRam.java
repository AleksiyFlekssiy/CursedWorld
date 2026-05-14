package com.aleksiyflekssiy.cursedworld.entity.behavior.piercing_ox;

import com.aleksiyflekssiy.cursedworld.entity.PiercingOxEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class PiercingRam extends Behavior<PiercingOxEntity> {
    private LivingEntity target;
    private Vec3 delta;

    public PiercingRam(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition, int pMinDuration, int pMaxDuration) {
        super(pEntryCondition, pMinDuration, pMaxDuration);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PiercingOxEntity piercingOx) {
        return piercingOx.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PiercingOxEntity piercingOx, long gameTime) {
        return checkExtraStartConditions(level, piercingOx);
    }

    @Override
    protected void start(ServerLevel level, PiercingOxEntity piercingOx, long gameTime) {
        target = piercingOx.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        piercingOx.lookAt(EntityAnchorArgument.Anchor.EYES, target.position());
        delta = piercingOx.getViewVector(1).scale(0.1);
        piercingOx.setDeltaMovement(delta);
    }

    @Override
    protected void tick(ServerLevel level, PiercingOxEntity piercingOx, long gameTime) {
        delta = delta.scale(1.1);
        piercingOx.setDeltaMovement(delta);
        if (piercingOx.position().distanceToSqr(target.position()) <= 1 + delta.lengthSqr()) {
            target.hurt(piercingOx.damageSources().mobAttack(piercingOx), (float) (5 * delta.lengthSqr()));
            target.setDeltaMovement(delta.scale(1.5));
            this.doStop(level, piercingOx, gameTime);
        }
        else if (piercingOx.horizontalCollision) this.doStop(level, piercingOx, gameTime);
    }

    @Override
    protected void stop(ServerLevel level, PiercingOxEntity piercingOx, long gameTime) {
        delta = Vec3.ZERO;
        piercingOx.setDeltaMovement(delta);
        target = null;
    }
}
