package com.aleksiyflekssiy.tutorialmod.entity.goal;

import com.aleksiyflekssiy.tutorialmod.entity.Shikigami;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class ShikigamiOwnerHurtTargetGoal extends TargetGoal {
    private final Shikigami shikigami;
    private LivingEntity target;
    private int timestamp;

    public ShikigamiOwnerHurtTargetGoal(Shikigami shikigami, boolean pMustSee) {
        super(shikigami, pMustSee);
        this.shikigami = shikigami;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (shikigami.isTamed()){
            Player player = shikigami.getOwner();
            if (player == null){
                return false;
            }
            else {
                if (player.getLastHurtMob() instanceof Shikigami) return false;
                this.target = player.getLastHurtMob();
                int i = player.getLastHurtMobTimestamp();
                return i != this.timestamp && this.canAttack(target, TargetingConditions.DEFAULT);
            }
        }
        else {
            return false;
        }
    }

    @Override
    public void start() {
        this.mob.setTarget(this.target);
        Player player = shikigami.getOwner();
        if (player == null){
            this.timestamp = player.getLastHurtMobTimestamp();
        }
        super.start();
    }
}
