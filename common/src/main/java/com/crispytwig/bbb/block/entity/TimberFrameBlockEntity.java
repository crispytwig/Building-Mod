package com.crispytwig.bbb.block.entity;

import com.crispytwig.bbb.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.Map;

public class TimberFrameBlockEntity extends BlockEntity {
    private static final String HELD_BLOCK = "held_block";

    private static final int REMOVED_MEMORY = 16;

    private static final Map<BlockPos, BlockState> REMOVED = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<BlockPos, BlockState> eldest) {
            return size() > REMOVED_MEMORY;
        }
    };

    private BlockState heldBlock = Blocks.AIR.defaultBlockState();

    public TimberFrameBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TIMBER_FRAME.get(), pos, state);
    }

    public static BlockState removedHeldBlock(BlockPos pos) {
        return REMOVED.getOrDefault(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        REMOVED.remove(worldPosition);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && level.isClientSide && !heldBlock.isAir()) {
            REMOVED.put(worldPosition.immutable(), heldBlock);
        }
    }

    public BlockState getHeldBlock() {
        return heldBlock;
    }

    public void setHeldBlock(BlockState state) {
        heldBlock = state;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        heldBlock = tag.contains(HELD_BLOCK)
                ? NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound(HELD_BLOCK))
                : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!heldBlock.isAir()) {
            tag.put(HELD_BLOCK, NbtUtils.writeBlockState(heldBlock));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
