package com.aleksiyflekssiy.tutorialmod.mixin;

import com.aleksiyflekssiy.tutorialmod.entity.behavior.CustomMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MoveToTargetSink.class)
public abstract class MoveTargetMixin {

    @Inject(method = "canStillUse(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Mob;J)Z", at = @At(value = "HEAD"), cancellable = true)
    private void canStillUse(ServerLevel pLevel, Mob pEntity, long pGameTime, CallbackInfoReturnable<Boolean> cir){
        if (pEntity.getBrain().hasMemoryValue(CustomMemoryModuleTypes.GRAB_TARGET.get())
        || pEntity.getBrain().hasMemoryValue(CustomMemoryModuleTypes.GRABBED_ENTITY.get())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Mob;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void checkExtraConditions(ServerLevel pLevel, Mob pEntity, CallbackInfoReturnable<Boolean> cir){
        if (pEntity.getBrain().hasMemoryValue(CustomMemoryModuleTypes.GRAB_TARGET.get())
                || pEntity.getBrain().hasMemoryValue(CustomMemoryModuleTypes.GRABBED_ENTITY.get())) {
            cir.setReturnValue(false);
        }
    }
}
