package com.aleksiyflekssiy.cursedworld.entity;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RoundDeerEntity extends Shikigami{
    public RoundDeerEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 500)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.ATTACK_DAMAGE, 10f)
                .add(Attributes.FOLLOW_RANGE, 100)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 2.5)
                .add(Attributes.ARMOR_TOUGHNESS, 2.5)
                .add(Attributes.JUMP_STRENGTH, 1);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide){
            List<LivingEntity> entities = level().getEntities(this, new AABB(
                    this.position().add(-5, -5, -5),
                    this.position().add(5, 5, 5)
            )).stream().filter(entity -> entity instanceof LivingEntity).map(entity -> (LivingEntity) entity).toList();

            for (LivingEntity entity : entities) entity.heal(1);
        }
    }
}
