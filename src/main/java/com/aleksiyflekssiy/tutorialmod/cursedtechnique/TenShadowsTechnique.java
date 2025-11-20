package com.aleksiyflekssiy.tutorialmod.cursedtechnique;

import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.tenshadows.DivineDogs;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.tenshadows.GreatSerpent;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.tenshadows.Nue;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.Skill;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.tenshadows.Toad;

import java.util.List;

public class TenShadowsTechnique extends CursedTechnique{
    private final List<Skill> skills = List.of(new DivineDogs(), new Nue(), new Toad(), new GreatSerpent());

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
        return "TenShadows";
    }
}
