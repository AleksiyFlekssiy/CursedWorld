package com.aleksiyflekssiy.tutorialmod.entity.behavior;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CustomSensorTypes {
    public static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(ForgeRegistries.SENSOR_TYPES, TutorialMod.MOD_ID);
    public static final RegistryObject<SensorType<ShikigamiOwnerHurtBySensor>> SHIKIGAMI_OWNER_HURT_BY = SENSOR_TYPES.register("shikigami_owner_hurt_by",
            () -> new SensorType<>(ShikigamiOwnerHurtBySensor::new));
    public static final RegistryObject<SensorType<ShikigamiOwnerHurtSensor>> SHIKIGAMI_OWNER_HURT = SENSOR_TYPES.register("shikigami_owner_hurt",
            () -> new SensorType<>(ShikigamiOwnerHurtSensor::new));

    public static void register(IEventBus eventBus) {
        SENSOR_TYPES.register(eventBus);
    }
}
