package com.aleksiyflekssiy.tutorialmod.mixin;

import com.aleksiyflekssiy.tutorialmod.client.renderer.VortexRenderer;
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
public class CameraMixin {
    @Shadow
    private Vec3 position;



    @Inject(method = "setup",
            at = @At(value = "TAIL"))
    public void onSetup(BlockGetter pLevel, Entity entity, boolean pDetached, boolean pThirdPersonReverse, float pPartialTick, CallbackInfo ci) {
        if (VortexRenderer.USERS.contains(entity.getUUID())) {
            // Прямое управление камерой

            Vec3 view = entity.getViewVector(1.0F);

            this.position = view.scale(-2 * 2.5).add(entity.position());
        }
    }
}
