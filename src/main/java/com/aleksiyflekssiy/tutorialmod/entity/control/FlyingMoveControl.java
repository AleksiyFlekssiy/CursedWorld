package com.aleksiyflekssiy.tutorialmod.entity.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;


import net.minecraft.world.entity.LivingEntity;

public class FlyingMoveControl extends MoveControl {
    private final LivingEntity entity;
    private float speed = 0.1F;

    public FlyingMoveControl(Mob entity) {
        super(entity);
        this.entity = entity;
    }

    public void tick() {
        if (this.operation == Operation.MOVE_TO) {
            if (entity.horizontalCollision) {
                entity.setYRot(entity.getYRot() + 180.0F);
                this.speed = 0.1F;
            }
            double d0 = wantedX - entity.getX();
            double d1 = wantedY - entity.getY();
            double d2 = wantedZ - entity.getZ();
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            if (Math.abs(d3) > (double) 1.0E-5F) {
                double d4 = 1.0D - Math.abs(d1 * (double) 0.7F) / d3;
                d0 *= d4;
                d2 *= d4;
                d3 = Math.sqrt(d0 * d0 + d2 * d2);
                double d5 = Math.sqrt(d0 * d0 + d2 * d2 + d1 * d1);
                float f = entity.getYRot();
                float f1 = (float) Mth.atan2(d2, d0);
                float f2 = Mth.wrapDegrees(entity.getYRot() + 90.0F);
                float f3 = Mth.wrapDegrees(f1 * (180F / (float) Math.PI));
                entity.setYRot(Mth.approachDegrees(f2, f3, 5.0F) - 90.0F);
                entity.yBodyRot = entity.getYRot();
                if (Mth.degreesDifferenceAbs(f, entity.getYRot()) < 3.0F) {
                    this.speed = Mth.approach(this.speed, 5F, 0.005F * (5F / this.speed));
                }
                else {
                    this.speed = Mth.approach(this.speed, 0.2F, 0.025F);
                }
                float f4 = (float) (-(Mth.atan2(-d1, d3) * (double) (180F / (float) Math.PI)));
                entity.setXRot(f4);
                float f5 = entity.getYRot() + 90.0F;
                double d6 = (double) (this.speed * Mth.cos(f5 * ((float) Math.PI / 180F))) * Math.abs(d0 / d5);
                double d7 = (double) (this.speed * Mth.sin(f5 * ((float) Math.PI / 180F))) * Math.abs(d2 / d5);
                double d8 = (double) (this.speed * Mth.sin(f4 * ((float) Math.PI / 180F))) * Math.abs(d1 / d5);
                Vec3 vec3 = entity.getDeltaMovement();
                entity.setDeltaMovement(vec3.add((new Vec3(d6, d8, d7)).subtract(vec3).scale(0.2D)));
            }
        }
    }

    public Vec3 getWantedPosition() {
        return new Vec3(wantedX, wantedY, wantedZ);
    }
}
//public class FlyingMoveControl extends MoveControl {
//    private final int maxTurn;
//    private final boolean hoversInPlace;
//
//    public FlyingMoveControl(Mob entity) {
//        super(entity);
//        this.maxTurn = 20;
//        this.hoversInPlace = false;
//    }
//
//    @Override
//    public void tick() {
//        System.out.println("MoveControl Operation: " + this.operation + " | Wanted: " + wantedX + ", " + wantedY + ", " + wantedZ);
        //if (this.operation == Operation.MOVE_TO) {
//            if (this.mob.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
//                WalkTarget target = this.mob.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get();
//                BlockPos pos = target.getTarget().currentBlockPosition();
//                setWantedPosition(pos.getX(), pos.getY(), pos.getZ(), 1);
//            }
//            double dx = this.wantedX - this.mob.getX();
//            double dy = this.wantedY - this.mob.getY();
//            double dz = this.wantedZ - this.mob.getZ();
//            double distSqr = dx * dx + dy * dy + dz * dz;

            //System.out.println("Distance to target: " + Math.sqrt(distSqr) + " | Current Pos: " + this.mob.position());

//            if (distSqr < 1D) { // Цель достигнута
//                this.operation = Operation.WAIT;
//                this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0.5D));
//                System.out.println("Target reached, stopping.");
//                return;
//            }

//            float speed = (float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
//            Vec3 direction = new Vec3(dx, dy, dz).normalize();
//            Vec3 motion = direction.scale(speed);
//
//            // Поворот к цели
//            float yaw = (float) (Mth.atan2(dz, dx) * (180F / (float) Math.PI)) - 90F;
//            float pitch = (float) (-Mth.atan2(dy, Mth.sqrt((float) (dx * dx + dz * dz))) * (180F / (float) Math.PI));
//            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), yaw, this.maxTurn));
//            this.mob.setXRot(this.rotlerp(this.mob.getXRot(), pitch, this.maxTurn));
//            this.mob.yBodyRot = this.mob.getYRot();
//
//            // Движение
//            this.mob.setDeltaMovement(motion);
            //System.out.println("Moving to: " + motion);
        //}
//    }
//    public Vec3 getWantedPosition() {
//        return new Vec3(wantedX, wantedY, wantedZ);
//    }
//}
