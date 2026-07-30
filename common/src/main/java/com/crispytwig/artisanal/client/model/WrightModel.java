package com.crispytwig.artisanal.client.model;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.client.model.animation.WrightAnimations;
import com.crispytwig.artisanal.entity.Wright;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.NotNull;

public class WrightModel extends ArtisanalEntityModel<Wright> implements ArmedModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Artisanal.location("wright"), "main");

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightArm;

    public WrightModel(ModelPart root) {
        super(RenderType::entityCutout);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.rightArm = this.body.getChild("rightArm");
    }

    @Override
    public @NotNull ModelPart root() {
        return this.body;
    }

    @Override
    protected String getRootPartName() {
        return "body";
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
        .texOffs(0, 10).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(8, 14).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offset(0.0F, 18.0F, 0.0F));

        body.addOrReplaceChild("head", CubeListBuilder.create()
        .texOffs(0, 0).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
        .texOffs(0, 22).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("leftArm", CubeListBuilder.create()
        .texOffs(24, 0).mirror().addBox(-0.25F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(1.75F, 0.5F, 0.0F));

        body.addOrReplaceChild("rightArm", CubeListBuilder.create()
        .texOffs(24, 0).addBox(-1.75F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.75F, 0.5F, 0.0F));

        body.addOrReplaceChild("leftWing", CubeListBuilder.create()
        .texOffs(16, 2).addBox(0.0F, 0.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 1.0F, 1.0F));

        body.addOrReplaceChild("rightWing", CubeListBuilder.create()
        .texOffs(16, 2).addBox(0.0F, 0.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 1.0F, 1.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    protected void setupAnimations(Wright entity, float limbSwing, float limbSwingAmount, float ageInTicks, float partialTick, float netHeadYaw, float headPitch) {
        this.animateIdleSmooth(entity.idleAnimationState, WrightAnimations.IDLE, ageInTicks, partialTick, limbSwingAmount);
        this.animateSmooth(entity.flyAnimationState, WrightAnimations.FLY, ageInTicks, partialTick, movementAnimationSpeed(limbSwingAmount, 1.5F, 0.7D));
        this.animateSmooth(entity.holdItemAnimationState, WrightAnimations.HOLD_ITEM, ageInTicks, partialTick);

        applyHeadLook(this.head, netHeadYaw, headPitch);
    }

    @Override
    public void translateToHand(@NotNull HumanoidArm arm, @NotNull PoseStack poseStack) {
        this.body.translateAndRotate(poseStack);
        poseStack.translate(0.0F, -0.1F, 0.1F);
        poseStack.mulPose(Axis.XP.rotation(this.rightArm.xRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(22.0F));
        poseStack.scale(0.7F, 0.7F, 0.7F);
        poseStack.translate(0.0625F, 0.0F, 0.0F);
    }
}
