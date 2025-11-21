package com.aleksiyflekssiy.tutorialmod.entity.ai;

import com.aleksiyflekssiy.tutorialmod.entity.GreatSerpentEntity;
import com.aleksiyflekssiy.tutorialmod.entity.GreatSerpentSegment;
import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMoveToTarget;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.common.util.BrainBuilder;

public class GreatSerpentAI {
    public static Brain<?> makeBrain(Brain<GreatSerpentEntity> brain) {
        initializeCoreActivity(brain);

        // Устанавливаем начальную активность
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        return brain;
    }

    public static Brain<GreatSerpentSegment> makeSegmentBrain(Brain<GreatSerpentSegment> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new MoveToTargetSink()));
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        return brain;
    }

    protected static void initializeCoreActivity(Brain<GreatSerpentEntity> brain){
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new MoveToTargetSink()));
    }


}
