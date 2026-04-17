package com.aleksiyflekssiy.cursedworld.cursed_technique;

import com.aleksiyflekssiy.cursedworld.cursed_technique.skill.RCTSkill;
import com.aleksiyflekssiy.cursedworld.cursed_technique.skill.tenshadows.*;
import com.aleksiyflekssiy.cursedworld.cursed_technique.skill.Skill;

import java.util.List;

public class TenShadowsTechnique extends CursedTechnique{
    private final List<Skill> skills = List.of(new DivineDogs(), new Nue(), new Toad(), new GreatSerpent(), new RabbitEscape(), new MaxElephant(), new RCTSkill());

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
        return "ten_shadows";
    }
}
