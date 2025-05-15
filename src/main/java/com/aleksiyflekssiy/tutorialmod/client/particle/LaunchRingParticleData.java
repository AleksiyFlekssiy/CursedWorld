package com.aleksiyflekssiy.tutorialmod.client.particle;

import com.aleksiyflekssiy.tutorialmod.particle.ModParticles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;

public class LaunchRingParticleData implements ParticleOptions {
    public static final Codec<LaunchRingParticleData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("entityId").forGetter(data -> data.entityId)
            ).apply(instance, LaunchRingParticleData::new)
    );

    public static final ParticleOptions.Deserializer<LaunchRingParticleData> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public LaunchRingParticleData fromCommand(ParticleType<LaunchRingParticleData> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            int entityId = reader.readInt();
            return new LaunchRingParticleData(entityId);
        }

        @Override
        public LaunchRingParticleData fromNetwork(ParticleType<LaunchRingParticleData> type, FriendlyByteBuf buffer) {
            return new LaunchRingParticleData(buffer.readInt());
        }
    };

    private final int entityId;

    public LaunchRingParticleData(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public ParticleType<?> getType() {
        return ModParticles.LAUNCH_RING.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeInt(this.entityId);
    }

    @Override
    public String writeToString() {
        return ForgeRegistries.PARTICLE_TYPES.getKey(getType()) + " " + this.entityId;
    }

    public int getEntityId() {
        return this.entityId;
    }
}
