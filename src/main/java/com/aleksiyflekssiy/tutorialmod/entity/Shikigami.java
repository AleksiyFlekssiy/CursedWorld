package com.aleksiyflekssiy.tutorialmod.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class Shikigami extends PathfinderMob implements OwnableEntity {
    protected Player owner;
    protected boolean isTamed = false;
    protected IOrder currentOrder = null;

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

    public IOrder getOrder(){
        return this.currentOrder;
    }

    public void setOrder(IOrder order){
        this.currentOrder = order;
    }

    public boolean followOrder(LivingEntity target, BlockPos blockPos, IOrder order){
        if (this.isTamed() && this.owner != null) {
            setOrder(order);
            return true;
        }
        return false;
    }

    public void clearOrder(){
        this.setOrder(null);
    }

    protected Vec3 getRiddenInput(Player player, Vec3 travelVector) {
        byte x = 0;
        byte y = 0;
        byte z = 0;
        Minecraft client = Minecraft.getInstance();
        if (client.options.keyUp.isDown()) z += 1;
        if (client.options.keyDown.isDown()) z -= 1;
        if (client.options.keyLeft.isDown()) x += 1;
        if (client.options.keyRight.isDown()) x -= 1;
        if (client.options.keyJump.isDown()) y += 1;
        if (client.options.keySprint.isDown()) y -= 1;
        return new Vec3(x, y, z);
    }
}
