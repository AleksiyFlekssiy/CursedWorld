package com.aleksiyflekssiy.tutorialmod.util;

import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.Skill;
import com.aleksiyflekssiy.tutorialmod.item.custom.WheelOfHarmonyItem;
import net.minecraft.world.effect.MobEffectInstance;

public class Adaptation {
    private static final int TICK_TO_SPIN = 600;

    private int cyclesToAdapt;
    private int cyclesWent;
    private float speed = 1;
    private float ticksLeft = TICK_TO_SPIN;

    public Adaptation(int cyclesToAdapt) {
        this.cyclesToAdapt = cyclesToAdapt;
    }

    public void makeCycle() {
        if (cyclesToAdapt - cyclesWent > 0) {
            cyclesWent++;
            ticksLeft = TICK_TO_SPIN;
        }
    }

    public int getCyclesToAdapt() {
        return cyclesToAdapt;
    }

    public void setCyclesToAdapt(int cyclesToAdapt) {
        this.cyclesToAdapt = cyclesToAdapt;
    }

    public int getCyclesWent() {
        return cyclesWent;
    }

    public void setCyclesWent(int cyclesWent) {
        this.cyclesWent = cyclesWent;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void increaseSpeed() {
        speed *= 1.1f;
    }

    public void decreaseTicks(float ticks) {
        ticksLeft -= ticks;
    }

    public float getTicksLeft() {
        return ticksLeft;
    }

    public void setTicksLeft(float ticksLeft) {
        this.ticksLeft = ticksLeft;
    }

    public boolean isComplete() {
        return cyclesToAdapt - cyclesWent == 0;
    }

    public static int calculateCyclesFromDamage(float health, float damage) {
        return Math.round(health / (health / damage));
    }

    public static int calculateCyclesFromEffect(MobEffectInstance effectInstance) {
        int total = 0;
        if (effectInstance.isInfiniteDuration()) total += 5;
        else total += Math.round(effectInstance.getDuration() / 200 / 5);
        total += effectInstance.getAmplifier();
        return total;
    }

    public static int calculateCyclesFromSkill(Skill skill) {
        return 5;
    }

    @Override
    public String toString() {
        return "Cycles need: " + cyclesToAdapt + "; Cycles went: " + cyclesWent + "; Speed: " + speed;
    }
}
