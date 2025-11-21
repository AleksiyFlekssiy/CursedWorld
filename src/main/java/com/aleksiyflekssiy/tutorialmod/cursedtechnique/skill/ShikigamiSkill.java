package com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill;

import com.aleksiyflekssiy.tutorialmod.entity.Shikigami;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public abstract class ShikigamiSkill extends Skill{
    protected boolean isActive;
    protected boolean isTamed;
    protected boolean isDead;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isTamed() {
        return isTamed;
    }

    public void setTamed(boolean tamed) {
        isTamed = tamed;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }

    public abstract List<Shikigami> getShikigami();

    //-1 for the previous order, 1 for the next order
    public abstract void switchOrder(LivingEntity owner, int direction);
}
