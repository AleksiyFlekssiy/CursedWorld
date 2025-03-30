package com.aleksiyflekssiy.tutorialmod.entity.goal;

import com.aleksiyflekssiy.tutorialmod.entity.Shikigami;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

import java.util.EnumSet;

public class ShikigamiFollowOwnerGoal extends Goal {
    private final Shikigami shikigami;
    private final LivingEntity owner;
    private final Level level;
    private final PathNavigation pathNavigation;
    private int timeToRecalcPath;
    private float speedModifier;
    private final float stopDistance;
    private final float startDistance;

    public ShikigamiFollowOwnerGoal(Shikigami shikigami, float startDistance, float stopDistance, float speedModifier) {
        this.shikigami = shikigami;
        this.owner = shikigami.getOwner();
        this.level = shikigami.level();
        this.pathNavigation = shikigami.getNavigation();
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (owner == null) {
            return false;
        }
        else if (owner.isSpectator()) {
            return false;
        }
        else if (shikigami.distanceToSqr(owner) < this.startDistance * this.startDistance) {
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (pathNavigation.isDone()) {
            return false;
        }
        else {
            return !(shikigami.distanceToSqr(owner) <= this.stopDistance * this.stopDistance);
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        this.timeToRecalcPath = 0;
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void stop() {
        this.pathNavigation.stop();
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick() {
        this.shikigami.getLookControl().setLookAt(this.owner, 10.0F, (float)this.shikigami.getMaxHeadXRot());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            this.pathNavigation.moveTo(this.owner, this.speedModifier);
        }
    }
}
