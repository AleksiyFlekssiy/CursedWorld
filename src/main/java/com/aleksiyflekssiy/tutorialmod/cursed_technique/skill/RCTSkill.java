package com.aleksiyflekssiy.tutorialmod.cursed_technique.skill;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class RCTSkill extends Skill{

    @Override
    public void use(LivingEntity entity, UseType type, int charge) {
        if (type == UseType.CHARGING) this.charge(entity, charge);
    }

    @Override
    public void charge(LivingEntity entity, int charge) {
        if (spendCursedEnergy(entity, 2)){
            entity.setHealth(entity.getHealth() + 1);
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
