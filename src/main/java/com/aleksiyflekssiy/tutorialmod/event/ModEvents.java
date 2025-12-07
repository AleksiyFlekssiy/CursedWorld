package com.aleksiyflekssiy.tutorialmod.event;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.config.ModConfig;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.domain_expansion.DomainExpansionSkill;
import com.aleksiyflekssiy.tutorialmod.entity.*;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.DIVINE_DOG.get(), DivineDogEntity.createAttributes().build());
        event.put(ModEntities.NUE.get(), NueEntity.createAttributes().build());
        event.put(ModEntities.TOAD.get(), ToadEntity.createAttributes().build());
        event.put(ModEntities.GREAT_SERPENT.get(), GreatSerpentEntity.createAttributes().build());
        event.put(ModEntities.GREAT_SERPENT_SEGMENT.get(),  GreatSerpentSegment.createAttributes().build());
    }

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        System.out.println("LOAD CONFIG---------------------------------------------------------------------");
        if (event.getConfig().getSpec() == ModConfig.COMMON_SPEC) {
            DomainExpansionSkill.DOMAIN_RADIUS = ModConfig.COMMON.DOMAIN_RADIUS.get();
            DomainExpansionSkill.DOMAIN_DURATION = ModConfig.COMMON.DOMAIN_DURATION.get();
        }
        System.out.println(ModConfig.COMMON.DOMAIN_RADIUS.get() + " :  " + ModConfig.COMMON.DOMAIN_DURATION.get());
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        System.out.println("RELOAD CONFIG-------------------------------------------------------------------");
        DomainExpansionSkill.DOMAIN_RADIUS = ModConfig.COMMON.DOMAIN_RADIUS.get();
        DomainExpansionSkill.DOMAIN_DURATION = ModConfig.COMMON.DOMAIN_DURATION.get();
        System.out.println(ModConfig.COMMON.DOMAIN_RADIUS.get() + " :  " + ModConfig.COMMON.DOMAIN_DURATION.get());
    }
}
