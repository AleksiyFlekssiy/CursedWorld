package com.aleksiyflekssiy.tutorialmod.entity.behavior;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.entity.IOrder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

public class CustomMemoryModuleTypes {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, TutorialMod.MOD_ID);

    public static final RegistryObject<MemoryModuleType<Player>> OWNER = MEMORY_MODULE_TYPES.register("owner",
            () -> new MemoryModuleType<>(Optional.empty()) {});
    public static final RegistryObject<MemoryModuleType<LivingEntity>> OWNER_HURT = MEMORY_MODULE_TYPES.register("owner_hurt",
            () -> new MemoryModuleType<>(Optional.empty()) {});
    public static final RegistryObject<MemoryModuleType<LivingEntity>> OWNER_HURT_BY_ENTITY = MEMORY_MODULE_TYPES.register("owner_hurt_by_entity",
            () -> new MemoryModuleType<>(Optional.empty()) {});
    public static final RegistryObject<MemoryModuleType<LivingEntity>> GRABBED_ENTITY = MEMORY_MODULE_TYPES.register("grabbed_entity",
            () -> new MemoryModuleType<>(Optional.empty()) {});
    public static final RegistryObject<MemoryModuleType<String>> ATTACK_TYPE = MEMORY_MODULE_TYPES.register("attack_type",
            () -> new MemoryModuleType<>(Optional.empty()) {});

    public static void register(IEventBus eventBus) {
        MEMORY_MODULE_TYPES.register(eventBus);
    }
}
