package com.aleksiyflekssiy.tutorialmod.entity.ai;

import com.aleksiyflekssiy.tutorialmod.entity.DivineDogEntity;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMoveToTarget;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Map;

public class DivineDogAI {
    public static Brain<?> makeBrain(Brain<DivineDogEntity> brain) {
        initializeCoreActivity(brain);
        initializeIdleActivity(brain);
        initializeFightActivity(brain);

        // Устанавливаем начальную активность
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();

        return brain;
    }

    protected static void initializeCoreActivity(Brain<DivineDogEntity> brain){
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new Swim(0.8F),
                new CustomMoveToTarget(
                        Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT))));
    }

    protected static void initializeIdleActivity(Brain<DivineDogEntity> brain){
        brain.addActivityWithConditions(Activity.IDLE,
                ImmutableList.of(
                        Pair.of(1, new DoNothing(0, 72000)),
                        Pair.of(2, new RandomLookAround(UniformInt.of(150, 300), 180, 0, 180))),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT)
                ));
    }

    protected static void initializeFightActivity(Brain<DivineDogEntity> brain){
        brain.addActivityWithConditions(Activity.FIGHT,
                ImmutableList.of(
                        Pair.of(1, SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1)),
                        Pair.of(2, MeleeAttack.create(20))
                ),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)
                ));
    }

    public static void updateActivity(Brain<DivineDogEntity> brain){
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
    }
}
