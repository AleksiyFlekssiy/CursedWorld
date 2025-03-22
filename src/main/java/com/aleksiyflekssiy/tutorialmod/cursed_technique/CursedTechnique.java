package com.aleksiyflekssiy.tutorialmod.cursed_technique;

import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.Skill;

import java.util.ArrayList;
import java.util.List;

public abstract class CursedTechnique {
    public static CursedTechnique DEV_TECHNIQUE = new LimitlessCursedTechnique();
    public final boolean haveDomain;

    public CursedTechnique(boolean haveDomain) {
        this.haveDomain = haveDomain;
    }

    public abstract Skill getDomain();

    public abstract Skill getFirstSkill();

    public abstract List<Skill> getSkillSet();

    public abstract String getName();


}
