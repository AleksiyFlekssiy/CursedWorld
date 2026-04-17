package com.aleksiyflekssiy.cursedworld.entity.control;

import com.aleksiyflekssiy.cursedworld.entity.ToadEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class JumpingMoveControl extends MoveControl {
    private final ToadEntity toad;
    private int jumpDelay;

    public JumpingMoveControl(ToadEntity toad) {
        super(toad);
        this.toad = toad;
    }

    public void tick() {
        if (this.operation == Operation.MOVE_TO){
            this.operation = MoveControl.Operation.WAIT;
            double d0 = this.wantedX - this.mob.getX();
            double d1 = this.wantedZ - this.mob.getZ();
            double d2 = this.wantedY - this.mob.getY();
            double d3 = d0 * d0 + d2 * d2 + d1 * d1;
            if (d3 < (double)2.5000003E-7F) {
                this.mob.setZza(0.0F);
                return;
            }
            float f9 = (float)(Mth.atan2(d1, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
            if (mob.onGround()) {
                mob.getJumpControl().jump();
                this.mob.setYRot(f9);
            }
            this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            BlockPos blockpos = this.mob.blockPosition();
            BlockState blockstate = this.mob.level().getBlockState(blockpos);
            VoxelShape voxelshape = blockstate.getCollisionShape(this.mob.level(), blockpos);
            if (d2 > (double)this.mob.getStepHeight() && d0 * d0 + d1 * d1 < (double)Math.max(1.0F, this.mob.getBbWidth()) || !voxelshape.isEmpty() && this.mob.getY() < voxelshape.max(Direction.Axis.Y) + (double)blockpos.getY() && !blockstate.is(BlockTags.DOORS) && !blockstate.is(BlockTags.FENCES)) {
                this.mob.getJumpControl().jump();
                this.operation = MoveControl.Operation.JUMPING;
            }
//            if (mob.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)){
//                mob.level().setBlock(new BlockPos((int) wantedX, (int) wantedY + 10, (int) wantedZ), Blocks.RED_WOOL.defaultBlockState(), 3);
//                mob.lookAt(EntityAnchorArgument.Anchor.FEET, new Vec3(wantedX,  wantedY, wantedZ));
//            }
//            this.operation = Operation.WAIT;
//            if (this.mob.onGround()) {
//                this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
//                mob.getJumpControl().jump();
//            } else {
//                this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
//            }

        }
        else if (this.operation == MoveControl.Operation.JUMPING) {
            this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            if (this.mob.onGround()) {
                this.operation = MoveControl.Operation.WAIT;
            }
        } else {
            this.mob.setZza(0.0F);
        }
    }

    protected int getJumpDelay() {
        return this.toad.getRandom().nextInt(20) + 10;
    }
}
