package com.aleksiyflekssiy.tutorialmod.client.particle;

import com.aleksiyflekssiy.tutorialmod.entity.BlueEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BluePullParticle extends TextureSheetParticle {
    private final double xStart;
    private final double yStart;
    private final double zStart;

    private BlueEntity nearestBlueEntity;

    protected BluePullParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.xStart = x;
        this.yStart = y;
        this.zStart = z;
        this.xo = x + xSpeed;
        this.yo = y + ySpeed;
        this.zo = z + zSpeed;
        this.x = this.xo;
        this.y = this.yo;
        this.z = this.zo;
        this.lifetime = 20; // 0.5 секунды
        this.hasPhysics = false;
        // Находим ближайшую BlueEntity и берём её chant
        float chantScaleFactor = 1.0f; // По умолчанию масштаб 1
        nearestBlueEntity = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : level.getEntities((Entity) null, this.getBoundingBox().inflate(10.0))) {
            if (entity instanceof BlueEntity blueEntity) {
                double distance = this.getPos().distanceTo(blueEntity.position());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    nearestBlueEntity = blueEntity;
                }
            }
        }

        if (nearestBlueEntity != null) {
            int chant = nearestBlueEntity.getChant();
            chantScaleFactor = Math.max(1, chant); // Масштаб зависит от chant
        }

        // Устанавливаем масштаб частицы
        float baseScale = 1.0f; // Базовый масштаб
        this.scale(baseScale * chantScaleFactor);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
        else{
                float f = (float) this.age / (float) this.lifetime;
                f = 1.0F - f;
                this.x = this.xStart + this.xd * (double) f;
                this.y = this.yStart + this.yd * (double) f;
                this.z = this.zStart + this.zd * (double) f;
                this.setPos(this.x, this.y, this.z);
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
            BluePullParticle particle = new BluePullParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.spriteSet); // Привязываем текстуру
            return particle;
        }
    }
}