package com.aleksiyflekssiy.tutorialmod.client.particle;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;

public class LaunchRingParticleType extends ParticleType<LaunchRingParticleData> {
    public LaunchRingParticleType() {
        super(false, LaunchRingParticleData.DESERIALIZER);
    }

    @Override
    public Codec<LaunchRingParticleData> codec() {
        return LaunchRingParticleData.CODEC;
    }
}
