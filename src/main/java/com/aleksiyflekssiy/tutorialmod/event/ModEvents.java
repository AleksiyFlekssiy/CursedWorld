package com.aleksiyflekssiy.tutorialmod.event;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.entity.*;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.DIVINE_DOG.get(), DivineDogEntity.createAttributes().build());
        event.put(ModEntities.NUE.get(), NueEntity.createAttributes().build());
        event.put(ModEntities.TOAD.get(), ToadEntity.createAttributes().build());
        event.put(ModEntities.GREAT_SERPENT.get(), GreatSerpentEntity.createAttributes().build());
        event.put(ModEntities.GREAT_SERPENT_SEGMENT.get(),  GreatSerpentPartEntity.createAttributes().build());
    }
}
