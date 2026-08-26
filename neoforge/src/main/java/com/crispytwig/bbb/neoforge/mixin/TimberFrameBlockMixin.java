package com.crispytwig.bbb.neoforge.mixin;

import com.crispytwig.bbb.block.Contents;
import com.crispytwig.bbb.block.TimberFrameBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TimberFrameBlock.class)
public class TimberFrameBlockMixin {
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return Contents.soundType(level, pos, state);
    }
}
