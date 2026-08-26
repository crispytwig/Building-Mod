package com.crispytwig.bbb.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringLayerBlock extends LayerBlock implements WeatheringCopper {
    public static final MapCodec<WeatheringLayerBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    WeatherState.CODEC.fieldOf("weathering_state").forGetter(ChangeOverTimeBlock::getAge),
                    propertiesCodec()
            ).apply(instance, WeatheringLayerBlock::new));

    private final WeatherState weatherState;

    public WeatheringLayerBlock(WeatherState weatherState, Properties properties) {
        super(properties);
        this.weatherState = weatherState;
    }

    @Override
    protected MapCodec<WeatheringLayerBlock> codec() {
        return CODEC;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        changeOverTime(state, level, pos, random);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return WeatheringCopper.getNext(state.getBlock()).isPresent();
    }

    @Override
    public WeatherState getAge() {
        return weatherState;
    }
}
