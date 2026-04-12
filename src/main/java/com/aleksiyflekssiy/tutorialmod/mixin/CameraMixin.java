package com.aleksiyflekssiy.tutorialmod.mixin;

import com.aleksiyflekssiy.tutorialmod.client.renderer.RabbitSwarmRenderer;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private Vec3 position;

    @Shadow
    public abstract boolean isDetached();

    @Shadow
    protected abstract double getMaxZoom(double pStartingDistance);

    @Shadow
    protected abstract void move(double pDistanceOffset, double pVerticalOffset, double pHorizontalOffset);

    @Inject(method = "setup",
            at = @At(value = "TAIL"))
    public void onSetup(BlockGetter pLevel, Entity entity, boolean pDetached, boolean pThirdPersonReverse, float pPartialTick, CallbackInfo ci) {
        if (RabbitSwarmRenderer.ID_RENDER_TARGETS.containsKey(entity.getId())) {
            // Прямое управление камерой
            if (RabbitSwarmRenderer.ID_RENDER_TARGETS.get(entity.getId())) {
                Vec3 view = entity.getViewVector(1.0F);
                byte multiplier = (byte) (pThirdPersonReverse ? 2 : -2);
                double x = view.x * multiplier * 5;
                double y = view.y * multiplier * 5;
                double z = view.z * multiplier * 5;
                Vec3 pos = new Vec3(x, y, z).add(entity.position());
                if (!isDetached()) pos.add(0, 1.6, 0);
                this.position = pos;
                //this.move(-getMaxZoom(x), 0, 0);
            }
        }
    }
}
