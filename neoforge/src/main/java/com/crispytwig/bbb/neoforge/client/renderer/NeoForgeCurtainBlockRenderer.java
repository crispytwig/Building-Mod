package com.crispytwig.bbb.neoforge.client.renderer;

import com.crispytwig.bbb.client.renderer.CurtainBlockRenderer;
import com.crispytwig.bbb.common.block.entity.CurtainBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public class NeoForgeCurtainBlockRenderer extends CurtainBlockRenderer {
    @Override
    public AABB getRenderBoundingBox(CurtainBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX(), pos.getY() - blockEntity.getLength() + 1, pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }
}
