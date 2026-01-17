package com.aleksiyflekssiy.tutorialmod.cursed_technique.skill;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class RCTSkill extends Skill{

    @Override
    public void use(LivingEntity entity, UseType type, int charge) {
        if (type == UseType.CHARGING) this.charge(entity, charge);
    }

    @Override
    public void charge(LivingEntity entity, int charge) {
        if (!entity.isCrouching()) {
            if (entity.getHealth() < entity.getMaxHealth() && spendCursedEnergy(entity, 2)) {
                entity.setHealth(entity.getHealth() + 1);
            }
        }
        else {
            HitResult result = ProjectileUtil.getHitResultOnViewVector(entity, target -> !target.equals(entity), 2);
            if (result.getType() == HitResult.Type.ENTITY) {
                EntityHitResult hitResult = (EntityHitResult) result;
                if (hitResult.getEntity() instanceof LivingEntity target) {
                    if (target.getHealth() < target.getMaxHealth() && spendCursedEnergy(entity, 2)) {
                        target.setHealth(target.getHealth() + 1);
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "reverse_cursed_technique";
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "textures/gui/rct.png");
    }
}
