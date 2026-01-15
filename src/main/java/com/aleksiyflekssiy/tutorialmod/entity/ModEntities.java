package com.aleksiyflekssiy.tutorialmod.entity;

import com.aleksiyflekssiy.tutorialmod.entity.animation.AnimationBlueEntity;
import com.aleksiyflekssiy.tutorialmod.entity.animation.AnimationRedEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    // Регистрация сущности
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "tutorialmod");

    public static final RegistryObject<EntityType<BlueEntity>> BLUE_ENTITY = ENTITIES.register("blue_entity",
            () -> EntityType.Builder.<BlueEntity>of(BlueEntity::new, MobCategory.MISC)
                    .sized(1.5F, 1.5F) // Размер сущности
                    .setUpdateInterval(1)
                    .build("tutorialmod:blue_entity"));

    public static final RegistryObject<EntityType<AnimationBlueEntity>> ANIMATION_BLUE_ENTITY = ENTITIES.register("animation_blue_entity",
            () -> EntityType.Builder.<AnimationBlueEntity>of(AnimationBlueEntity::new, MobCategory.MISC)
                    .sized(1.5F, 1.5F) // Размер сущности
                    .setUpdateInterval(1)
                    .build("tutorialmod:animation_blue_entity"));

    public static final RegistryObject<EntityType<RedEntity>> RED_ENTITY = ENTITIES.register("red_entity",
            () -> EntityType.Builder.<RedEntity>of(RedEntity::new, MobCategory.MISC)
                    .sized(0.5F,0.5F)
                    .clientTrackingRange(50)
                    .setUpdateInterval(1)
                    .build("tutorialmod:red_entity"));

    public static final RegistryObject<EntityType<AnimationRedEntity>> ANIMATION_RED_ENTITY = ENTITIES.register("animation_red_entity",
            () -> EntityType.Builder.<AnimationRedEntity>of(AnimationRedEntity::new, MobCategory.MISC)
                    .sized(0.5F,0.5F)
                    .clientTrackingRange(10)
                    .setUpdateInterval(1)
                    .build("tutorialmod:animation_red_entity"));

    public static final RegistryObject<EntityType<HollowPurpleEntity>> HOLLOW_PURPLE_ENTITY = ENTITIES.register("hollow_purple_entity",
            () -> EntityType.Builder.<HollowPurpleEntity>of(HollowPurpleEntity::new, MobCategory.MISC)
                    .sized(1.5f, 1.5f)
                    .setUpdateInterval(1)
                    .clientTrackingRange(20)
                    .build("tutorialmod:hollow_purple_entity"));

    public static final RegistryObject<EntityType<DivineDogEntity>> DIVINE_DOG = ENTITIES.register("divine_dog",
            () -> EntityType.Builder.<DivineDogEntity>of(DivineDogEntity::new, MobCategory.CREATURE)
                    .sized(1.5f,1.5f)
                    .build("tutorialmod:divine_dog"));

    public static final RegistryObject<EntityType<NueEntity>> NUE = ENTITIES.register("nue",
            () -> EntityType.Builder.<NueEntity>of(NueEntity::new, MobCategory.CREATURE)
                    .sized(5f, 6f)
                    .build("tutorialmod:nue"));

    public static final RegistryObject<EntityType<ToadEntity>> TOAD = ENTITIES.register("toad",
            () -> EntityType.Builder.<ToadEntity>of(ToadEntity::new, MobCategory.CREATURE)
                    .sized(1.5f, 2.5f)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .build("tutorialmod:toad"));

    public static final RegistryObject<EntityType<GreatSerpentEntity>> GREAT_SERPENT = ENTITIES.register("great_serpent",
            () -> EntityType.Builder.<GreatSerpentEntity>of(GreatSerpentEntity::new, MobCategory.CREATURE)
                    .sized(2, 2)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .clientTrackingRange(20)
                    .build("tutorialmod:great_serpent"));

    public static final RegistryObject<EntityType<GreatSerpentSegment>> GREAT_SERPENT_SEGMENT = ENTITIES.register("great_serpent_segment",
            () -> EntityType.Builder.<GreatSerpentSegment>of(GreatSerpentSegment::new, MobCategory.CREATURE)
                    .sized(2, 2)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .clientTrackingRange(20)
                    .build("tutorialmod:great_serpent_segment"));


    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}
