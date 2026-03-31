package com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.tenshadows;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.tutorialmod.entity.Shikigami;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class RabbitEscape extends ShikigamiSkill {
    private List<Shikigami> rabbits = new ArrayList<>(50);

    @Override
    public List<Shikigami> getShikigami() {
        return rabbits;
    }

    @Override
    public void setShikigami(List<Shikigami> shikigamiList) {

    }

    @Override
    public void switchOrder(LivingEntity owner, int direction) {

    }

    @Override
    public String getName() {
        return "rabbit_escape";
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "textures/gui/rabbit_escape.png");
    }
}
