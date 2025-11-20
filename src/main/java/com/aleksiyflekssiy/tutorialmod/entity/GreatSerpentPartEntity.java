package com.aleksiyflekssiy.tutorialmod.entity;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class GreatSerpentPartEntity extends Shikigami {
    public final int index;
    private final GreatSerpentEntity parent;

    public GreatSerpentPartEntity(EntityType<? extends Shikigami> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        //this.noPhysics = true;
        this.refreshDimensions();
        index = 0;
        parent = null;
    }

    public GreatSerpentPartEntity(EntityType<? extends Shikigami> pEntityType, Level pLevel, GreatSerpentEntity parent, int index) {
        super(pEntityType, pLevel);
        //this.noPhysics = true;
        this.refreshDimensions();
        this.parent = parent;
        this.index = index;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(2.0F, 2.0F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return this.parent.hurt(source, amount);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean is(Entity pEntity) {
        return this.equals(pEntity) || parent.equals(pEntity);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 60)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 5f)
                .add(Attributes.FOLLOW_RANGE, 100)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 1)
                .add(Attributes.ARMOR_TOUGHNESS, 2.5)
                .add(Attributes.JUMP_STRENGTH, 1);
    }

    @Override
    protected void defineSynchedData() {

    }


}
