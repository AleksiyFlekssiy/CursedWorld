package com.aleksiyflekssiy.tutorialmod.mixin;

import com.aleksiyflekssiy.tutorialmod.client.screen.JujutsuHUD;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void renderHotbar(float pPartialTick, GuiGraphics pGuiGraphics, CallbackInfo ci) {
        if (JujutsuHUD.shouldRender) {
            ci.cancel();
        }
    }
}
