package com.aleksiyflekssiy.tutorialmod.entity.ai;

import com.aleksiyflekssiy.tutorialmod.entity.NueEntity;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMoveToTarget;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.nue.AscendToPoint;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.nue.SweepAttack;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomLookAround;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;


public class NueAI {
    public static Brain<?> makeBrain(Brain<NueEntity> brain) {
        initializeCoreActivity(brain);
        initializeIdleActivity(brain);
        initializeFightActivity(brain);

        // Устанавливаем начальную активность
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();

        return brain;
    }

    protected static void initializeCoreActivity(Brain<NueEntity> brain){
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new MoveToTargetSink()));
    }

    protected static void initializeIdleActivity(Brain<NueEntity> brain){
        brain.addActivityWithConditions(Activity.IDLE,
                ImmutableList.of(
                    //Pair.of(1, new DoNothing(0, 72000)),
                    Pair.of(2, new RandomLookAround(UniformInt.of(150, 300), 180, 0, 180))),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT)
                ));
    }

    protected static void initializeFightActivity(Brain<NueEntity> brain){
        brain.addActivityWithConditions(Activity.FIGHT,
                ImmutableList.of(
                Pair.of(0, new AscendToPoint(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT))),
                Pair.of(1, new SweepAttack(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)))),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT),
                        Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED)
                ));
    }

    public static void updateActivity(Brain<NueEntity> brain){
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
    }
}
