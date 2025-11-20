package com.aleksiyflekssiy.tutorialmod.cursedtechnique;

import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.*;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.limitless.*;

import java.util.List;

public class LimitlessCursedTechnique extends CursedTechnique{
    private final List<Skill> skills = List.of(new Infinity(), new Blue(), new Red(), new HollowPurple(), new UnlimitedVoid());

    public LimitlessCursedTechnique() {
        super(true);
    }

    @Override
    public Skill getFirstSkill() {
        return skills.get(0);
    }

    @Override
    public List<Skill> getSkillSet(){
        return skills;
    }

    @Override
    public Skill getDomain(){
        return skills.get(skills.size()-1);
    }

    @Override
    public String getName() {
        return "Limitless";
    }
}
