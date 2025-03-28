package com.aleksiyflekssiy.tutorialmod.cursed_technique;

import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.DivineDogs;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.Skill;

import java.util.List;

public class TenShadowsTechnique extends CursedTechnique{
    private final List<Skill> skills = List.of(new DivineDogs());

    public TenShadowsTechnique() {
        super(true);
    }

    @Override
    public Skill getDomain() {
        return null;
    }

    @Override
    public Skill getFirstSkill() {
        return skills.get(0);
    }

    @Override
    public List<Skill> getSkillSet() {
        return skills;
    }

    @Override
    public String getName() {
        return "Ten Shadows";
    }
}
