package com.aleksiyflekssiy.cursedworld.particle;

import com.aleksiyflekssiy.cursedworld.CursedWorld;
import com.aleksiyflekssiy.cursedworld.client.particle.LaunchRingParticleData;
import com.aleksiyflekssiy.cursedworld.client.particle.LaunchRingParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, CursedWorld.MOD_ID);
    public static final RegistryObject<ParticleType<LaunchRingParticleData>> LAUNCH_RING = PARTICLE_TYPES.register("red_launch_ring",
            LaunchRingParticleType::new);
    public static final RegistryObject<SimpleParticleType> BLUE_PULL = PARTICLE_TYPES.register("blue_pull",
            () -> new SimpleParticleType(false));

    public static void register(IEventBus bus) {
        PARTICLE_TYPES.register(bus);
    }
}
