package com.aleksiyflekssiy.tutorialmod.entity.control;

import com.aleksiyflekssiy.tutorialmod.entity.DivineDogEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.shapes.VoxelShape;

class CustomMoveControl extends MoveControl {
    private final DivineDogEntity divineDogEntity;
    private BlockPos lastPos = null;
    private BlockPos nextPos = null;
    private Path path = mob.getNavigation().getPath();
    private int index = 0;

    public CustomMoveControl(DivineDogEntity divineDogEntity, Mob pMob) {
        super(pMob);
        this.divineDogEntity = divineDogEntity;
    }

    public void tick() {
        if (this.operation == Operation.STRAFE) {
            float f = (float) this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
            float f1 = (float) this.speedModifier * f;
            float f2 = this.strafeForwards;
            float f3 = this.strafeRight;
            float f4 = Mth.sqrt(f2 * f2 + f3 * f3);

            if (f4 < 1.0F) {
                f4 = 1.0F;
            }

            f4 = f1 / f4;
            f2 *= f4;
            f3 *= f4;
            float f5 = Mth.sin(this.mob.getYRot() * ((float) Math.PI / 180F));
            float f6 = Mth.cos(this.mob.getYRot() * ((float) Math.PI / 180F));
            float f7 = f2 * f6 - f3 * f5;
            float f8 = f3 * f6 + f2 * f5;
            if (!this.isWalkable(f7, f8)) {
                this.strafeForwards = 1.0F;
                this.strafeRight = 0.0F;
            }

            this.mob.setSpeed(f1);
            this.mob.setZza(this.strafeForwards);
            this.mob.setXxa(this.strafeRight);
            this.operation = Operation.WAIT;
        } else if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;
            double d0 = this.wantedX - this.mob.getX();
            double d1 = this.wantedZ - this.mob.getZ();
            double d2 = this.wantedY - this.mob.getY();
            double d3 = d0 * d0 + d2 * d2 + d1 * d1;
            if (d3 < (double) 2.5000003E-7F) {
                this.mob.setZza(0.0F);
                return;
            }
            System.out.println("WantedX: " + wantedX);
            System.out.println("WantedY: " + wantedY);
            System.out.println("WantedZ: " + wantedZ);
            System.out.println("Entity pos: X: " + mob.getX() + " Y: " + mob.getY() + " Z: " + mob.getZ());
            System.out.println("Mob YRot: " + mob.getYRot());
            //изначальное
            float currentYaw = mob.getYRot();
            if (mob.getTarget() != null) {
                // Плавные повороты при спуске вниз
                if (mob.getY() > mob.getTarget().getY()) { // Условие спуска
                    float targetAngle = (float) (Mth.atan2(d1, d0) * (180.0 / Math.PI)) - 90.0F;
                    float deltaYaw = Mth.wrapDegrees(targetAngle - currentYaw);
                    this.mob.setYRot(currentYaw + deltaYaw);
                    System.out.println("Mob YRot with targetAngle: " + (currentYaw + deltaYaw));
                } else if (mob.getY() != mob.getTarget().getY()) { // Подъём или другое изменение высоты
                    float targetAngle = (float) (Mth.atan2(d1, d0) * (180.0 / Math.PI)) - 90.0F;
                    float deltaYaw = Mth.wrapDegrees(targetAngle - currentYaw);
                    this.mob.setYRot(currentYaw + deltaYaw);
                    System.out.println("Mob YRot with targetAngle: " + (currentYaw + deltaYaw));
                }
            } else {
                // Логика без цели (оставляем как есть)
                float deltaYaw = Mth.wrapDegrees(divineDogEntity.targetYaw - currentYaw);
                float smoothedYaw = currentYaw + Mth.clamp(deltaYaw, -15.0F, 15.0F);
                mob.setYRot(smoothedYaw);
            }
            this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            BlockPos blockpos = this.mob.blockPosition();
            BlockState blockstate = this.mob.level().getBlockState(blockpos);
            VoxelShape voxelshape = blockstate.getCollisionShape(this.mob.level(), blockpos);
            if (d2 > (double) this.mob.getStepHeight() && d0 * d0 + d1 * d1 < (double) Math.max(1.0F, this.mob.getBbWidth()) || !voxelshape.isEmpty() && this.mob.getY() < voxelshape.max(Direction.Axis.Y) + (double) blockpos.getY() && !blockstate.is(BlockTags.DOORS) && !blockstate.is(BlockTags.FENCES)) {
                this.mob.getJumpControl().jump();
                this.operation = Operation.JUMPING;
            }
        } else if (this.operation == Operation.JUMPING) {
            this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            if (this.mob.onGround()) {
                this.operation = Operation.WAIT;
            }
        } else {
            this.mob.setZza(0.0F);
        }
    }

    private boolean isWalkable(float pRelativeX, float pRelativeZ) {
        PathNavigation pathnavigation = this.mob.getNavigation();
        if (pathnavigation != null) {
            NodeEvaluator nodeevaluator = pathnavigation.getNodeEvaluator();
            if (nodeevaluator != null && nodeevaluator.getBlockPathType(this.mob.level(), Mth.floor(this.mob.getX() + (double) pRelativeX), this.mob.getBlockY(), Mth.floor(this.mob.getZ() + (double) pRelativeZ)) != BlockPathTypes.WALKABLE) {
                return false;
            }
        }
        return true;
    }
}
