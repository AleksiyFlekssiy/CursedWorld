package com.aleksiyflekssiy.tutorialmod.animation;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class BlueControlAnimation implements IAnimation {
    private final AbstractClientPlayer player;
    private boolean isUsing;

    public BlueControlAnimation(AbstractClientPlayer player) {
        this.player = player;
    }

    public void setUsing(boolean using) {
        this.isUsing = using;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public @NotNull Vec3f get3DTransform(@NotNull String boneName, @NotNull TransformType transformType, float tickDelta, @NotNull Vec3f vec3f) {
        if (boneName.equals("rightArm") && transformType == TransformType.ROTATION) {
            if (isUsing) {
                float x = (float) (-player.getLookAngle().y * 0.75f * 2) - 1.5f;
                float y = (player.getYRot() - player.yBodyRot) / 60;
                float scaledY = Math.max(Math.min(y, 1f), -1f);
                return new Vec3f(x, scaledY, 0);
            }
        }
        return vec3f;
    }

    @Override
    public void setupAnim(float v) {
    }

    @Override
    public void tick() {
    }
}