package com.aleksiyflekssiy.tutorialmod.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class LaunchRingParticle extends TextureSheetParticle {
    private final float startScale = 0.1f;
    private final float endScale = 5.0f;
    private final int projectileId;
    private Vec3 direction;
    private float currentScale;

    protected LaunchRingParticle(ClientLevel level, double x, double y, double z, LaunchRingParticleData data) {
        super(level, x, y, z, 0, 0, 0);
        this.projectileId = data.getEntityId();
        this.direction = new Vec3(0, 0, 1);
        this.lifetime = 20;
        this.hasPhysics = false;
        this.currentScale = startScale;
        this.quadSize = startScale;
        updateDirection();
    }

    private void updateDirection() {
        Entity projectile = this.level.getEntity(this.projectileId);
        if (projectile != null) {
            Vec3 motion = projectile.getDeltaMovement();
            if (motion.lengthSqr() > 0.0001) {
                this.direction = motion.normalize();
            }
        }
        else System.out.println("NOTHING");
        // Не меняем direction на (0, 0, 1), чтобы сохранить последнее валидное значение
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            float progress = (float) this.age / this.lifetime;
            this.currentScale = startScale + (endScale - startScale) * progress;
            this.quadSize = this.currentScale;
            this.alpha = 1.0f - progress;
            updateDirection();
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.getPosition();
        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        PoseStack poseStack = new PoseStack();
        poseStack.translate(x, y, z);

        // Направление
        Vector3f dir = new Vector3f(
                (float) this.direction.x,
                (float) this.direction.y,
                (float) this.direction.z
        );
        if (dir.lengthSquared() < 0.0001f) {
            dir.set(0, 0, 1); // Запасное направление
        }
        dir.normalize();

        // Вычисляем поворот через минимальный угол
        Vector3f baseNormal = new Vector3f(0, 0, 1); // Базовая нормаль текстуры (смотрит на Z)
        float dot = baseNormal.dot(dir);
        Vector3f axis = baseNormal.cross(dir, new Vector3f());
        float angle = (float) Math.acos(Mth.clamp(dot, -1.0f, 1.0f));

        // Применяем поворот
        Quaternionf rotation = new Quaternionf();
        if (axis.lengthSquared() > 0.0001f) {
            axis.normalize();
            rotation.rotateAxis(angle, axis);
        } else if (dot < -0.999f) {
            // Если dir противоположен baseNormal, поворачиваем на 180°
            rotation.rotateX((float) Math.PI);
        }

        poseStack.mulPose(rotation);

        // Масштабирование
        float renderScale = Mth.lerp(partialTicks, this.currentScale, this.currentScale);
        poseStack.scale(renderScale, renderScale, renderScale);

        PoseStack.Pose pose = poseStack.last();
        float size = 0.5f;
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int light = this.getLightColor(partialTicks);

        buffer.vertex(pose.pose(), -size, -size, 0)
                .uv(u0, v1)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
        buffer.vertex(pose.pose(), -size, size, 0)
                .uv(u0, v0)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
        buffer.vertex(pose.pose(), size, size, 0)
                .uv(u1, v0)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
        buffer.vertex(pose.pose(), size, -size, 0)
                .uv(u1, v1)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();

        buffer.vertex(pose.pose(), size, -size, 0)
                .uv(u1, v1)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
        buffer.vertex(pose.pose(), size, size, 0)
                .uv(u1, v0)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
        buffer.vertex(pose.pose(), -size, size, 0)
                .uv(u0, v0)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
        buffer.vertex(pose.pose(), -size, -size, 0)
                .uv(u0, v1)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<LaunchRingParticleData> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(LaunchRingParticleData data, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            LaunchRingParticle particle = new LaunchRingParticle(level, x, y, z, data);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }
}