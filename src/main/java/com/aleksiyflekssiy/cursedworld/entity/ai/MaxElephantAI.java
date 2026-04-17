package com.aleksiyflekssiy.cursedworld.entity.ai;

import com.aleksiyflekssiy.cursedworld.entity.MaxElephantEntity;
import com.aleksiyflekssiy.cursedworld.entity.RabbitEscapeEntity;
import com.aleksiyflekssiy.cursedworld.entity.behavior.CustomMoveToTarget;
import com.aleksiyflekssiy.cursedworld.entity.behavior.ShikigamiFollowOwner;
import com.aleksiyflekssiy.cursedworld.entity.behavior.maxelephant.WaterSplash;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.RandomLookAround;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Map;

public class MaxElephantAI {
    public static Brain<MaxElephantEntity> makeBrain(Brain<MaxElephantEntity> brain) {
        initializeCoreActivity(brain);
        initializeIdleActivity(brain);
        initializeFightActivity(brain);

        // Устанавливаем начальную активность
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();

        return brain;
    }

    protected static void initializeCoreActivity(Brain<MaxElephantEntity> brain){
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new CustomMoveToTarget(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT))
        ));
    }

    protected static void initializeIdleActivity(Brain<MaxElephantEntity> brain){
        brain.addActivityWithConditions(Activity.IDLE,
                ImmutableList.of(
                        Pair.of(1, new DoNothing(0, 72000)),
                        Pair.of(2, new RandomLookAround(UniformInt.of(150, 300), 180, 0, 180))),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT)
                ));
    }

    protected static void initializeFightActivity(Brain<MaxElephantEntity> brain){
        brain.addActivityWithConditions(Activity.FIGHT,
                ImmutableList.of(
                        Pair.of(0, new WaterSplash(Map.of(), 0, 72000))
                ),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT))
        );
    }

    public static void updateActivity(Brain<MaxElephantEntity> brain){
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
    }
}
