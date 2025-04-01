package com.aleksiyflekssiy.tutorialmod.effect;

import com.aleksiyflekssiy.tutorialmod.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.LimitlessCursedTechnique;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.limitless.Infinity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class InfinityEffect extends MobEffect {
    public InfinityEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFFFFF);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Вызывается каждый тик, пока эффект активен
        // amplifier - уровень эффекта (0 - базовый, 1 и выше - усиленный)
        if (entity instanceof Player player){
            if (CursedTechniqueCapability.getCursedTechnique(player) instanceof LimitlessCursedTechnique limitless){
                Infinity infinity = (Infinity) limitless.getFirstSkill();
                if (infinity != null && infinity.infinitySwitch.isEnabled()) {
                    infinity.infinitySwitch.applyInfinityEffect(player, player.level());
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Определяет, вызывать ли applyEffectTick каждый тик
        return true; // Вызываем каждый тик
    }
}
