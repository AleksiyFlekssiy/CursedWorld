package com.aleksiyflekssiy.tutorialmod.entity.control;

import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMemoryModuleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class GreatSerpentMoveControl extends MoveControl {
    private LivingEntity entity;
    private float speed = 1;
    public GreatSerpentMoveControl(Mob pMob) {
        super(pMob);
        this.entity = pMob;
    }

    public void tick() {
        if (this.operation == Operation.MOVE_TO) {
            this.operation = MoveControl.Operation.WAIT;
            Vec3 target = new Vec3(this.wantedX, this.wantedY, this.wantedZ);
            Vec3 current = mob.position();

            Vec3 direction = target.subtract(current).normalize();
            double speed = this.speedModifier * mob.getAttributeValue(Attributes.MOVEMENT_SPEED);

            double d0 = this.wantedX - this.mob.getX();
            double d1 = this.wantedY - this.mob.getY();
            double d2 = this.wantedZ - this.mob.getZ();
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;
            double d4 = Math.sqrt(d0 * d0 + d2 * d2);
            // Идём НАПРЯМУЮ, игнорируя всё
            mob.setDeltaMovement(direction.scale(speed));
            if (Math.abs(d1) > (double)1.0E-5F || Math.abs(d4) > (double)1.0E-5F) {
                this.mob.setYya((float) (d1 > 0.0D ? speed : -speed));
            }

            // Поворот лицом к цели (по желанию)
            float yaw = (float) (Math.atan2(direction.z, direction.x) * 180F / Math.PI) - 90F;
            mob.setYRot(yaw);
            mob.yBodyRot = yaw;
        }
        else {
            if (!mob.getBrain().hasMemoryValue(CustomMemoryModuleTypes.GRAB_TARGET.get())
            && !mob.getBrain().hasMemoryValue(CustomMemoryModuleTypes.GRABBED_ENTITY.get())) this.mob.setDeltaMovement(Vec3.ZERO);
            this.mob.setYya(0.0F);
            this.mob.setZza(0.0F);
        }
    }

    public Vec3 getWantedPosition() {
        return new Vec3(wantedX, wantedY, wantedZ);
    }
}
