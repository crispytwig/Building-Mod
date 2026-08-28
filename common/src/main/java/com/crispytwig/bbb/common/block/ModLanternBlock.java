package com.crispytwig.bbb.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ModLanternBlock extends LanternBlock {
    public static final MapCodec<LanternBlock> CODEC = simpleCodec(ModLanternBlock::new);

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(5, 0, 5, 11, 9, 11),
            Block.box(7, 9, 7, 9, 11, 9));
    private static final VoxelShape HANGING_SHAPE = Shapes.or(SHAPE, Block.box(7, 11, 7, 9, 16, 9));

    public ModLanternBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<LanternBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HANGING) ? HANGING_SHAPE : SHAPE;
    }
}
