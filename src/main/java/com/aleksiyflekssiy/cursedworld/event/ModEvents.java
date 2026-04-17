package com.aleksiyflekssiy.cursedworld.event;

import com.aleksiyflekssiy.cursedworld.CursedWorld;
import com.aleksiyflekssiy.cursedworld.config.ModConfig;
import com.aleksiyflekssiy.cursedworld.cursed_technique.skill.domain_expansion.DomainExpansionSkill;
import com.aleksiyflekssiy.cursedworld.entity.*;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = CursedWorld.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.DIVINE_DOG.get(), DivineDogEntity.createAttributes().build());
        event.put(ModEntities.NUE.get(), NueEntity.createAttributes().build());
        event.put(ModEntities.TOAD.get(), ToadEntity.createAttributes().build());
        event.put(ModEntities.GREAT_SERPENT.get(), GreatSerpentEntity.createAttributes().build());
        event.put(ModEntities.GREAT_SERPENT_SEGMENT.get(),  GreatSerpentSegment.createAttributes().build());
        event.put(ModEntities.RABBIT_ESCAPE.get(),  RabbitEscapeEntity.createAttributes().build());
        event.put(ModEntities.MAX_ELEPHANT.get(),  MaxElephantEntity.createAttributes().build());
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
