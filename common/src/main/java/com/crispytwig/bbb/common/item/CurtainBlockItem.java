package com.crispytwig.bbb.common.item;

import com.crispytwig.bbb.common.block.entity.CurtainBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class CurtainBlockItem extends BlockItem {
    public CurtainBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();

        for (int offset = 1; offset <= CurtainBlockEntity.MAX_LENGTH + 1; offset++) {
            BlockPos above = clicked.above(offset);
            BlockState aboveState = level.getBlockState(above);
            if (aboveState.getBlock() == getBlock()) {
                if (level.getBlockEntity(above) instanceof CurtainBlockEntity curtain) {
                    if (offset == curtain.getLength()) {
                        if (!level.isClientSide) {
                            curtain.setLength(curtain.getLength() + 1);
                        }
                        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                            context.getItemInHand().shrink(1);
                        }
                        SoundType sound = SoundType.WOOL;
                        level.playSound(context.getPlayer(), clicked, sound.getPlaceSound(), SoundSource.BLOCKS,
                                (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
                        return InteractionResult.sidedSuccess(level.isClientSide);
                    }
                    if (offset < curtain.getLength()) {
                        return InteractionResult.FAIL;
                    }
                }
                break;
            }
            if (!aboveState.isAir() && !aboveState.canBeReplaced()) {
                break;
            }
        }

        return super.place(context);
    }
}
