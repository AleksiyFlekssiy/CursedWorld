package com.aleksiyflekssiy.tutorialmod.cursedtechnique;

import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.Skill;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

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

    public void serializeNBT(CompoundTag tag) {
        tag.putString("technique", this.getName());
    }

}
