package com.aleksiyflekssiy.tutorialmod.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LaunchRingParticle extends TextureSheetParticle {
    private final float startScale = 1f; // Начальный масштаб
    private final float endScale = 5.0f;   // Конечный масштаб

    protected LaunchRingParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.lifetime = 20; // 0.5 секунды
        this.hasPhysics = false;
        this.scale(startScale); // Устанавливаем начальный масштаб
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            // Увеличиваем масштаб каждый тик
            float progress = (float) this.age / this.lifetime;
            this.scale(startScale + (endScale - startScale) * progress);
            this.alpha = 1.0f - progress; // Затухание
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            LaunchRingParticle particle = new LaunchRingParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.spriteSet); // Привязываем текстуру
            return particle;
        }
    }
}