// Made with Blockbench 4.12.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports
package com.aleksiyflekssiy.tutorialmod.client.model;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.entity.DivineDogEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class DivineDogModel<T extends Entity> extends HierarchicalModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(TutorialMod.MOD_ID, "divine_dog"), "main");
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	private final ModelPart body;
	private final ModelPart leg4;
	private final ModelPart leg3;
	private final ModelPart leg2;
	private final ModelPart leg1;
	private final ModelPart head;
	private final ModelPart torso;

	public DivineDogModel(ModelPart root) {
		this.body = root.getChild("body");
		this.leg4 = this.body.getChild("leg4");
		this.leg3 = this.body.getChild("leg3");
		this.leg2 = this.body.getChild("leg2");
		this.leg1 = this.body.getChild("leg1");
		this.head = this.body.getChild("head");
		this.torso = this.body.getChild("torso");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offsetAndRotation(4.0F, 24.0F, -8.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition leg4 = body.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(24, 62).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(36, 69).addBox(-3.5F, 9.0F, -2.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(20.5F, -11.0F, 7.5F));

		PartDefinition leg3 = body.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(52, 61).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(64, 68).addBox(-3.5F, 9.0F, -2.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(20.5F, -11.0F, 0.5F));

		PartDefinition leg2 = body.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(12, 60).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(64, 61).addBox(-3.5F, 9.0F, -2.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, -11.0F, 8.5F));

		PartDefinition leg1 = body.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 60).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(36, 62).addBox(-3.5F, 9.0F, -2.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, -11.0F, -0.5F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(48, 23).addBox(-6.0F, -14.0F, -1.0F, 7.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(0, 51).addBox(-13.0F, -8.0F, 2.0F, 8.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(52, 55).addBox(-13.0F, -5.0F, 3.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, -12.0F, -1.0F));

		PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(84, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -3.0F, 11.0F, 0.7854F, 0.6981F, 0.0F));

		PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(84, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -3.0F, 0.0F, 0.7854F, -0.7854F, 0.0F));

		PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(28, 55).addBox(-7.0F, -5.0F, -1.0F, 8.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -5.0F, 4.0F, 0.0F, 0.0F, -0.2182F));

		PartDefinition cube_r4 = head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(10, 74).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -11.0F, 8.0F, -0.2618F, 0.0F, 0.0F));

		PartDefinition cube_r5 = head.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 74).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -11.0F, 1.0F, 0.2618F, 0.0F, 0.0F));

		PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -9.0F, -2.0F, 30.0F, 11.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(0, 23).addBox(-12.0F, -11.0F, -3.0F, 10.0F, 14.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -13.0F, 0.0F));

		PartDefinition cube_r6 = torso.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(48, 47).addBox(-15.0F, -4.0F, -1.0F, 16.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(21.0F, -8.0F, 3.0F, 0.0F, 0.0F, -2.0071F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
		super.prepareMobModel(pEntity, pLimbSwing, pLimbSwingAmount, pPartialTick);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.body.getAllParts().forEach(ModelPart::resetPose);
		//this.applyHeadRotation(netHeadYaw, headPitch, ageInTicks);


		// Поворот тела в сторону цели
//		if (entity instanceof DivineDogEntity dog && dog.getTarget() != null) {
//			this.body.yRot = netHeadYaw * ((float)Math.PI / 180F) * 0.5F; // Плавный поворот тела
//		}
		runAnimations((DivineDogEntity) entity, limbSwing, limbSwingAmount, 2, 2.5F);
	}

	private void runAnimations(DivineDogEntity dog,float limbSwing, float limbSwingAmount, int animationSpeed, float animationScale){
		if (dog.getRealSpeed() >= 0.66F) this.animateWalk(DivineDogAnimations.run_animation, limbSwing, limbSwingAmount, animationSpeed, animationScale);
		else this.animateWalk(DivineDogAnimations.walk_animation, limbSwing, limbSwingAmount, animationSpeed, animationScale);
	}

	private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch, float pAgeInTicks) {
		pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);
		pHeadPitch = Mth.clamp(pHeadPitch, -25.0F, 45.0F);

		this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
		this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return body;
	}
}