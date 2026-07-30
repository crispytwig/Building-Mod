package com.crispytwig.artisanal.client.renderer;

import com.crispytwig.artisanal.client.AllayFlightClient;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.AllayModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.allay.Allay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AllayFlightLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/allay/allay.png");
    private static final float SCALE = 1.0F;
    private static final float ARM_YAW = 0.27925268F;
    private static final float ARM_ROLL = 0.43633232F;

    private final AllayModel model;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    @Nullable
    private Allay puppet;

    public AllayFlightLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet modelSet) {
        super(parent);
        this.model = new AllayModel(modelSet.bakeLayer(ModelLayers.ALLAY));
        this.body = this.model.root().getChild("body");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
    }

    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (player.isInvisible()) {
            return;
        }
        AllayFlightClient.Anim anim = AllayFlightClient.of(player);
        if (anim.remaining(player, partialTick) <= 0.0F) {
            return;
        }

        ModelPart playerHead = this.getParentModel().head;
        this.model.setupAnim(this.puppet(player), 0.0F, anim.amount(partialTick) * 0.25F,
                anim.time(partialTick), netHeadYaw, headPitch);
        this.pose(playerHead.xRot, anim.pitch(partialTick), anim.roll(partialTick));

        poseStack.pushPose();
        playerHead.translateAndRotate(poseStack);
        poseStack.translate(0.0F, -3.0F / 16.0F, 0.0F);
        poseStack.mulPose(Axis.XP.rotation(-playerHead.xRot));
        poseStack.mulPose(Axis.YP.rotation(-playerHead.yRot));
        poseStack.mulPose(Axis.ZP.rotation(-playerHead.zRot));

        float pitch = playerHead.xRot;
        float hold = 4.0F * Mth.cos(pitch) + 2.0F * Mth.sin(2.0F * Mth.abs(pitch));
        poseStack.translate(0.0F, (-hold - 23.5F * SCALE) / 16.0F, 0.0F);
        poseStack.scale(SCALE, SCALE, SCALE);

        VertexConsumer buffer = bufferSource.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void pose(float headPitch, float pitch, float roll) {
        float reach = -1.134464F + Mth.abs(headPitch) * 0.5F;
        this.body.xRot = Mth.PI / 4.0F + pitch;
        this.body.zRot = roll;
        this.rightArm.xRot = reach - pitch;
        this.leftArm.xRot = reach - pitch;
        this.rightArm.yRot = ARM_YAW;
        this.leftArm.yRot = -ARM_YAW;
        this.rightArm.zRot = ARM_ROLL - roll;
        this.leftArm.zRot = -ARM_ROLL - roll;
    }

    private Allay puppet(AbstractClientPlayer player) {
        if (this.puppet == null || this.puppet.level() != player.level()) {
            this.puppet = new Allay(EntityType.ALLAY, player.level());
        }
        return this.puppet;
    }
}
