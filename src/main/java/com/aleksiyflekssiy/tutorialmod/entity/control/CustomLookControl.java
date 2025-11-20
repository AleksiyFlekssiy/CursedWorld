package com.aleksiyflekssiy.tutorialmod.entity.control;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;

public class CustomLookControl extends LookControl {

    public CustomLookControl(Mob mob) {
        super(mob);
    }

    public CustomLookControl(Mob mob, boolean instantRotation){
        super(mob);
        this.xMaxRotAngle = 360.0F;
        this.yMaxRotSpeed = 1000;
        if (instantRotation) this.lookAtCooldown = 0;
    }
}
