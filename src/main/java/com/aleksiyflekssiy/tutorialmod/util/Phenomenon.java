package com.aleksiyflekssiy.tutorialmod.util;

import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.Skill;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;

import java.util.Arrays;
import java.util.Objects;

public class Phenomenon {
    private final DamageType damageType;
    private final MobEffect effect;
    private final Skill skill;
    private final PhenomenonType phenomenonType;

    public Phenomenon(DamageType damageType) {
        this.damageType = damageType;
        this.effect = null;
        this.skill = null;
        this.phenomenonType = PhenomenonType.DAMAGE;
    }

    public Phenomenon(MobEffect effect) {
        this.damageType = null;
        this.effect = effect;
        this.skill = null;
        this.phenomenonType = PhenomenonType.EFFECT;
    }

    public Phenomenon(Skill skill) {
        this.damageType = null;
        this.effect = null;
        this.skill = skill;
        this.phenomenonType = PhenomenonType.SKILL;
    }

    public DamageType getDamageType() {
        return damageType;
    }

    public MobEffect getEffect() {
        return effect;
    }

    public Skill getSkill() {return skill;}

    public PhenomenonType getPhenomenonType() {
        return phenomenonType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Phenomenon phenomenon) {
            if (this.phenomenonType == phenomenon.phenomenonType) {
                if (phenomenonType == PhenomenonType.DAMAGE)
                    return this.damageType.msgId().equals(phenomenon.damageType.msgId());
                else if (phenomenonType == PhenomenonType.EFFECT)
                    return this.effect.getDescriptionId().equals(phenomenon.effect.getDescriptionId());
                else return this.skill.getName().equals(phenomenon.skill.getName());
            }
        }
        return false;
    }

    @Override
    public String toString() {
        if (phenomenonType == PhenomenonType.DAMAGE) {
            return "Type: " + phenomenonType.getSerializedName() + "; DamageType: " + damageType.msgId();
        } else if (phenomenonType == PhenomenonType.EFFECT) return "Type: " + phenomenonType.getSerializedName() + "; Effect: " + effect.getDescriptionId();
        else return "Type: " + phenomenonType.getSerializedName() + "; Skill: " + skill.getName();
    }

    @Override
    public int hashCode() {
        return Objects.hash(damageType, effect, phenomenonType);
    }

    public enum PhenomenonType {
        DAMAGE("damage"),
        EFFECT("effect"),
        SKILL("skill");

        private final String name;

        PhenomenonType(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return name;
        }

        public static PhenomenonType fromName(String name) {
            if (name == null) return null;
            return Arrays.stream(values())
                    .filter(t -> t.name.equals(name))
                    .findFirst()
                    .orElse(null);
        }
    }
}
