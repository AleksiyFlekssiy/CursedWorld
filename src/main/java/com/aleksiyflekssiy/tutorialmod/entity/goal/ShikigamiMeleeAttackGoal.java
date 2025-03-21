package com.aleksiyflekssiy.tutorialmod.entity.goal;

import com.aleksiyflekssiy.tutorialmod.entity.DivineDogEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ShikigamiMeleeAttackGoal extends MeleeAttackGoal {
    private int pathRecalculationCooldown;
    private int ticksUntilNextAttack;
    private DivineDogEntity entity;

    public ShikigamiMeleeAttackGoal(PathfinderMob mob, double speed, boolean followIfNotSeen) {
        super(mob, speed, followIfNotSeen);
        entity = (DivineDogEntity) mob;
    }

    @Override
    public void start() {
        super.start();
        this.pathRecalculationCooldown = 0;
        entity.startPathVisualization();
    }

    @Override
    public void stop() {
        super.stop();
        this.pathRecalculationCooldown = 0;
        entity.stopPathVisualization();
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity target = this.mob.getTarget();
        if (target == null) return;
        double dx = target.getX() - entity.getX();
        double dz = target.getZ() - entity.getZ();
        entity.targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;

        double distance = this.mob.distanceToSqr(target);
        System.out.println("Melee: Distance = " + Math.sqrt(distance) + " | Target at " + target.getX() + ", " + target.getZ() + " | Mob at " + this.mob.getX() + ", " + this.mob.getZ());
        //System.out.println("TargetYaw: " + targetYaw + " | SmoothedYaw: " + this.mob.getYRot());

        if (distance > 4.0D && this.pathRecalculationCooldown <= 0) {
            this.mob.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), mob.getSpeed());
            this.pathRecalculationCooldown = 5;
            System.out.println("Melee: Moving to " + target.getX() + ", " + target.getZ());
        }
        this.pathRecalculationCooldown--;
    }
}
