package com.aleksiyflekssiy.tutorialmod.client.model;// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.aleksiyflekssiy.tutorialmod.entity.ToadEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class ToadModel<T extends ToadEntity> extends HierarchicalModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("tutorialmod", "toad"), "main");
    private final ModelPart body;
    private final ModelPart leftFrontLeg;
    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart upperHead;
    private final ModelPart tongue;
    private final ModelPart leftBackLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart rightBackLeg;

    public ToadModel(ModelPart root) {
        this.body = root.getChild("body");
        this.leftFrontLeg = this.body.getChild("leftFrontLeg");
        this.torso = this.body.getChild("torso");
        this.head = this.body.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.upperHead = this.head.getChild("upperHead");
        this.tongue = this.head.getChild("tongue");
        this.leftBackLeg = this.body.getChild("leftBackLeg");
        this.rightFrontLeg = this.body.getChild("rightFrontLeg");
        this.rightBackLeg = this.body.getChild("rightBackLeg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition leftFrontLeg = body.addOrReplaceChild("leftFrontLeg", CubeListBuilder.create().texOffs(80, 64).addBox(-14.0F, -1.0F, 12.0F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -13.0F));

        PartDefinition cube_r1 = leftFrontLeg.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 80).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.5F, -6.0F, 8.5F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r2 = leftFrontLeg.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(88, 0).addBox(-3.0F, -5.0F, -1.0F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -10.0F, 7.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r3 = torso.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 58).addBox(0.0F, -8.0F, -1.0F, 1.0F, 8.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(46, 46).addBox(15.0F, -8.0F, -1.0F, 1.0F, 8.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -2.0F, -8.0F, 1.1345F, 0.0F, 0.0F));

        PartDefinition cube_r4 = torso.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(30, 68).addBox(-11.0F, -14.0F, -1.0F, 12.0F, 14.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -5.0F, -17.0F, -0.4363F, 0.0F, 0.0F));

        PartDefinition cube_r5 = torso.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(76, 54).addBox(-11.0F, -8.0F, -1.0F, 12.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -15.0F, -2.0F, 1.1345F, 0.0F, 0.0F));

        PartDefinition cube_r6 = torso.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(60, 16).addBox(-11.0F, -8.0F, -1.0F, 12.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 0.0F, -9.0F, 1.1345F, 0.0F, 0.0F));

        PartDefinition cube_r7 = torso.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(60, 0).addBox(-11.0F, -14.0F, -1.0F, 12.0F, 14.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -1.0F, -7.0F, -0.4363F, 0.0F, 0.0F));

        PartDefinition cube_r8 = torso.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 0).addBox(-14.0F, -10.0F, 0.0F, 14.0F, 10.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 0.0F, -8.0F, 1.1345F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 1.0F));

        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create(), PartPose.offset(0.0F, -12.0F, -5.0F));

        PartDefinition cube_r9 = jaw.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 46).addBox(-13.0F, -3.0F, -1.0F, 14.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, 0.0F, 1.0F, 0.1309F, 0.0F, 0.0F));

        PartDefinition upperHead = head.addOrReplaceChild("upperHead", CubeListBuilder.create().texOffs(48, 37).addBox(-5.0F, -7.0F, 0.0F, 10.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(57, 68).addBox(-8.0F, -7.0F, 2.0F, 2.0F, 7.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(75, 37).addBox(6.0F, -7.0F, 2.0F, 2.0F, 7.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(30, 58).addBox(3.0F, -9.0F, 10.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(88, 24).addBox(-7.0F, -9.0F, 10.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 26).addBox(-5.0F, -9.0F, 2.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -15.0F, -10.0F));

        PartDefinition cube_r10 = upperHead.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 26).addBox(-11.0F, -8.0F, -1.0F, 12.0F, 8.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 0.0F, 12.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition tongue = head.addOrReplaceChild("tongue", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -17.0F, -6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition leftBackLeg = body.addOrReplaceChild("leftBackLeg", CubeListBuilder.create().texOffs(80, 76).addBox(-13.0F, -1.0F, -15.0F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 0.0F, 0.0F));

        PartDefinition cube_r11 = leftBackLeg.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(36, 84).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-16.5F, -9.0F, -12.5F, 0.0F, 0.0F, -1.1345F));

        PartDefinition cube_r12 = leftBackLeg.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(24, 84).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-16.5F, -6.0F, -12.5F, 0.0F, 0.0F, -1.1345F));

        PartDefinition rightFrontLeg = body.addOrReplaceChild("rightFrontLeg", CubeListBuilder.create().texOffs(80, 70).addBox(-14.0F, -1.0F, -1.0F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(23.0F, 0.0F, 0.0F));

        PartDefinition cube_r13 = rightFrontLeg.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(12, 80).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.5F, -6.0F, -3.5F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r14 = rightFrontLeg.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(88, 12).addBox(-3.0F, -5.0F, -1.0F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.0F, -12.0F, -6.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition rightBackLeg = body.addOrReplaceChild("rightBackLeg", CubeListBuilder.create().texOffs(80, 82).addBox(-14.0F, -1.0F, -15.0F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(21.0F, 0.0F, 0.0F));

        PartDefinition cube_r15 = rightBackLeg.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(60, 85).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, -9.0F, -12.5F, 0.0F, 0.0F, 1.1345F));

        PartDefinition cube_r16 = rightBackLeg.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(48, 85).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, -6.0F, -12.5F, 0.0F, 0.0F, 1.1345F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(ToadEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.body.getAllParts().forEach(ModelPart::resetPose);
        this.tongue.zScale = -(entity.getDistance() * 8);
        this.tongue.z = tongue.zScale * 4;
        this.animate(entity.mouthOpen, ToadAnimations.mouth_open, ageInTicks, 1);
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