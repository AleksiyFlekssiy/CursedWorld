package com.aleksiyflekssiy.tutorialmod.event;

import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.Skill;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public class SkillEvent extends Event {
    private final LivingEntity caster;
    private final Skill skill;

    public SkillEvent(LivingEntity caster, Skill skill) {
        this.caster = caster;
        this.skill = skill;
    }

    public LivingEntity getCaster() {
        return caster;
    }

    public Skill getSkill() {
        return skill;
    }

    public static class Hit extends SkillEvent {
        private final LivingEntity target;

        public Hit(LivingEntity caster, Skill skill, LivingEntity target) {
            super(caster, skill);
            this.target = target;
        }

        public LivingEntity getTarget() {
            return target;
        }
    }
}
