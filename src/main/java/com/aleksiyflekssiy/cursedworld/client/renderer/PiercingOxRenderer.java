package com.aleksiyflekssiy.cursedworld.client.renderer;

import com.aleksiyflekssiy.cursedworld.CursedWorld;
import com.aleksiyflekssiy.cursedworld.client.model.ModModelLayers;
import com.aleksiyflekssiy.cursedworld.client.model.PiercingOxModel;
import com.aleksiyflekssiy.cursedworld.entity.PiercingOxEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class PiercingOxRenderer extends MobRenderer<PiercingOxEntity, PiercingOxModel<PiercingOxEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(CursedWorld.MOD_ID, "textures/entity/piercing_ox.png");

    public PiercingOxRenderer(EntityRendererProvider.Context context) {
        super(context, new PiercingOxModel<>(context.bakeLayer(ModModelLayers.PIERCING_OX_LAYER)), 1.5f);
    }

    @Override
    public void render(PiercingOxEntity entity, float pEntityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        // Рендеринг модели
        super.render(entity,pEntityYaw,partialTicks, poseStack, buffer, packedLight);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(PiercingOxEntity pEntity) {
        return TEXTURE;
    }
}
