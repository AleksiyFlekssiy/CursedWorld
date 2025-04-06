package com.aleksiyflekssiy.tutorialmod.entity.navigation;

import com.aleksiyflekssiy.tutorialmod.entity.ToadEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

public class JumpingMoveControl extends MoveControl {
    private final ToadEntity toad;
    private float yRot;
    private int jumpDelay;
    private boolean isAggressive;
    private float jumpHeight = 0.5F;

    public JumpingMoveControl(ToadEntity toad) {
        super(toad);
        this.toad = toad;
        this.yRot = 180.0F * toad.getYRot() / (float) Math.PI;
    }

    public void setDirection(float pYRot, boolean pAggressive) {
        this.yRot = pYRot;
        this.isAggressive = pAggressive;
    }

    public void setWantedMovement(double pSpeed) {
        this.speedModifier = pSpeed;
        this.operation = Operation.MOVE_TO;
    }

    public void tick() {
        this.setDirection(this.toad.targetYaw, true);
        this.mob.setYRot(this.rotlerp(this.mob.getYRot(), this.yRot, 90.0F));
        this.mob.yHeadRot = this.mob.getYRot();
        this.mob.yBodyRot = this.mob.getYRot();
        if (this.operation != Operation.MOVE_TO) {
            this.mob.setZza(0.0F);
        } else {
            this.operation = Operation.WAIT;
            if (this.mob.onGround()) {
                this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                if (this.jumpDelay-- <= 0) {
                    this.jumpDelay = this.getJumpDelay();
                    if (this.isAggressive) {
                        this.jumpDelay /= 3;
                    }
                    //TODO СДЕЛАТЬ СЛЕДОВАНИЕ ЦЕЛИ ДЛЯ ЖАБЫ

                } else {
                    this.toad.xxa = 0.0F;
                    this.toad.zza = 0.0F;
                    this.mob.setSpeed(0.0F);
                }
            } else {
                this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            }
        }
    }

    protected int getJumpDelay() {
        return this.toad.getRandom().nextInt(20) + 10;
    }
}
