package com.aleksiyflekssiy.tutorialmod.entity.ai;

import com.aleksiyflekssiy.tutorialmod.entity.ToadEntity;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMemoryModuleTypes;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.toad.TongueCatch;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.toad.TongueImmobilize;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.toad.TonguePull;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.toad.TongueSwing;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.particle.SonicBoomParticle;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.goat.GoatAi;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenAi;
import net.minecraft.world.entity.schedule.Activity;

public class ToadAI {
    public static Brain<?> makeBrain(Brain<ToadEntity> brain) {
        initializeCoreActivity(brain);
        initializeIdleActivity(brain);
        initializeFightActivity(brain);

        // Устанавливаем начальную активность
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();

        return brain;
    }

    protected static void initializeCoreActivity(Brain<ToadEntity> brain){
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new MoveToTargetSink()));
    }

    protected static void initializeIdleActivity(Brain<ToadEntity> brain){

    }

    protected static void initializeFightActivity(Brain<ToadEntity> brain){
        brain.addActivityWithConditions(Activity.FIGHT,
                ImmutableList.of(
                        Pair.of(0, new TongueCatch(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT))),
                        Pair.of(1, new TonguePull(ImmutableMap.of(CustomMemoryModuleTypes.GRABBED_ENTITY.get(), MemoryStatus.VALUE_PRESENT))),
                        Pair.of(2, new TongueImmobilize(ImmutableMap.of(CustomMemoryModuleTypes.GRABBED_ENTITY.get(), MemoryStatus.VALUE_PRESENT))),
                        Pair.of(3, new TongueSwing(ImmutableMap.of(CustomMemoryModuleTypes.GRABBED_ENTITY.get(), MemoryStatus.VALUE_PRESENT)))),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT),
                        Pair.of(CustomMemoryModuleTypes.GRABBED_ENTITY.get(), MemoryStatus.REGISTERED)
                ));
    }

    public static void updateActivity(Brain<ToadEntity> brain){
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
    }
}
