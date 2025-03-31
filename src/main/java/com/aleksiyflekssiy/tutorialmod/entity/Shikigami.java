package com.aleksiyflekssiy.tutorialmod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class Shikigami extends PathfinderMob implements OwnableEntity {
    protected Player owner;
    protected boolean isTamed = false;

    protected Shikigami(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.owner = null;
    }

    protected Shikigami(EntityType<? extends PathfinderMob> pEntityType, Level pLevel, Player owner) {
        super(pEntityType, pLevel);
        this.owner = owner;
    }

    public void tame(Player owner){
        this.isTamed = true;
        this.owner = owner;
        this.goalSelector.removeAllGoals(filter -> true);
        this.targetSelector.removeAllGoals(filter -> true);
        this.registerGoals();
    }

    public boolean isTamed() {
        return isTamed;
    }

    @Override
    public @Nullable UUID getOwnerUUID() {
        return owner.getUUID();
    }

    @Override
    public @Nullable Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }
}
