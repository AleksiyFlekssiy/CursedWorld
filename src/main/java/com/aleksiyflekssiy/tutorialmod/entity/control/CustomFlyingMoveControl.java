package com.aleksiyflekssiy.tutorialmod.entity.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class CustomFlyingMoveControl extends MoveControl {
    private LivingEntity entity;
    private float speed = 1;
    public CustomFlyingMoveControl(Mob pMob) {
        super(pMob);
        this.entity = pMob;
    }

    public void tick() {
        if (this.operation == Operation.MOVE_TO) {
            Vec3 target = new Vec3(this.wantedX, this.wantedY, this.wantedZ);
            Vec3 current = mob.position();

            Vec3 direction = target.subtract(current).normalize();
            double speed = this.speedModifier * mob.getAttributeValue(Attributes.MOVEMENT_SPEED);

            // Идём НАПРЯМУЮ, игнорируя всё
            mob.setDeltaMovement(direction.scale(speed));

            // Поворот лицом к цели (по желанию)
            float yaw = (float) (Math.atan2(direction.z, direction.x) * 180F / Math.PI) - 90F;
            mob.setYRot(yaw);
            mob.yBodyRot = yaw;
        }
    }

    public Vec3 getWantedPosition() {
        return new Vec3(wantedX, wantedY, wantedZ);
    }
}
