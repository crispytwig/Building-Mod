package com.crispytwig.bbb.common.block.entity;

import com.crispytwig.bbb.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CurtainBlockEntity extends BlockEntity {
    public static final int MAX_LENGTH = 32;

    private int length = 1;

    public CurtainBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CURTAIN.get(), pos, state);
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        int clamped = Math.max(1, Math.min(MAX_LENGTH, length));
        if (clamped == this.length) {
            return;
        }
        this.length = clamped;
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Length", length);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        length = Math.max(1, Math.min(MAX_LENGTH, tag.getInt("Length")));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("Length", length);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
