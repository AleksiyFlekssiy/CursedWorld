package com.aleksiyflekssiy.tutorialmod.client.model;// Made with Blockbench 5.1.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.entity.RabbitEscapeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class RabbitEscapeEntityModel<T extends RabbitEscapeEntity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "rabbit_escape"), "main");
	private final ModelPart head;
	private final ModelPart bb_main;

	public RabbitEscapeEntityModel(ModelPart root) {
		this.head = root.getChild("head");
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(8, 23).addBox(-1.0F, -4.0F, -1.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(26, 22).addBox(4.0F, -4.0F, -1.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-0.5F, 0.0F, -2.0F, 5.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 13.0F, -5.0F));

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(22, 8).addBox(-1.0F, -5.0F, 4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(18, 14).addBox(-4.0F, -2.0F, -2.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(22, 0).addBox(2.0F, -2.0F, -2.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(18, 22).addBox(-2.5F, -5.0F, -5.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 23).addBox(0.5F, -5.0F, -5.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r1 = bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -9.0F, -2.0F, 6.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 3.0F, 0.9599F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(RabbitEscapeEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}