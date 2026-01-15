package com.aleksiyflekssiy.tutorialmod.client.model;// Made with Blockbench 5.0.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.entity.GreatSerpentEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class GreatSerpentHeadModel<T extends GreatSerpentEntity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "great_serpent_head"), "main");
	private final ModelPart upperJaw;
	private final ModelPart lowerJaw;

	public GreatSerpentHeadModel(ModelPart root) {
		this.upperJaw = root.getChild("upperJaw");
		this.lowerJaw = root.getChild("lowerJaw");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition upperJaw = partdefinition.addOrReplaceChild("upperJaw", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -9.0F, -14.0F, 14.0F, 9.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(40, 54).addBox(-8.0F, -7.0F, -14.0F, 1.0F, 7.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(0, 56).addBox(7.0F, -7.0F, -14.0F, 1.0F, 7.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(0, 42).addBox(-6.0F, -6.0F, -22.0F, 12.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.0F, 7.0F));

		PartDefinition lowerJaw = partdefinition.addOrReplaceChild("lowerJaw", CubeListBuilder.create().texOffs(0, 23).addBox(-7.0F, 0.0F, -14.0F, 14.0F, 5.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(56, 0).addBox(7.0F, 0.0F, -14.0F, 1.0F, 3.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(56, 17).addBox(-8.0F, 0.0F, -14.0F, 1.0F, 3.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(40, 42).addBox(-6.0F, 0.0F, -22.0F, 12.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.0F, 7.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(GreatSerpentEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		upperJaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		lowerJaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}