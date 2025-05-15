package com.aleksiyflekssiy.tutorialmod.entity.goal;


import com.aleksiyflekssiy.tutorialmod.entity.Shikigami;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

public class ShikigamiTargetSummonerGoal extends TargetGoal {

    public ShikigamiTargetSummonerGoal(Shikigami shikigami, LivingEntity target, boolean pMustSee) {
        super(shikigami, pMustSee);
        this.targetMob = target;
    }

    @Override
    public boolean canUse() {
        return this.targetMob != null && this.targetMob.isAlive();
    }
}
