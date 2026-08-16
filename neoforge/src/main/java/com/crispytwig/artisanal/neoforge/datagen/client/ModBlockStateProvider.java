package com.crispytwig.artisanal.neoforge.datagen.client;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.block.TrimBlock;
import com.crispytwig.artisanal.block.TrimStairBlock;
import com.crispytwig.artisanal.block.TrimType;
import com.crispytwig.artisanal.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.EnumMap;
import java.util.Map;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, Artisanal.MOD_ID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        cube("oak_boards", ModBlocks.OAK_BOARDS.get());
        cubeStairs("oak_board_stairs", Artisanal.location("block/oak_boards"), ModBlocks.OAK_BOARD_STAIRS.get());
        cubeSlab("oak_board_slab", Artisanal.location("block/oak_boards"), Artisanal.location("block/oak_boards"), ModBlocks.OAK_BOARD_SLAB.get());
        cube("polished_oak", ModBlocks.POLISHED_OAK.get());

        trim("oak_trim", Artisanal.location("block/oak_boards"), ModBlocks.OAK_TRIM.get());
        trimStairs("oak_trim", ModBlocks.OAK_TRIM_STAIRS.get());
        slab("oak_trim_slab", Artisanal.location("block/oak_trim_slab_full"), ModBlocks.OAK_TRIM_SLAB.get());
    }

    private void cube(String name, Block block) {
        simpleBlockWithItem(block, models().cubeAll(name, Artisanal.location("block/" + name)));
    }

    private void cubeStairs(String name, ResourceLocation texture, StairBlock block) {
        stairsBlock(block, texture);
        itemModels().withExistingParent(name, Artisanal.location("block/" + name));
    }

    private void cubeSlab(String name, ResourceLocation full, ResourceLocation texture, SlabBlock block) {
        slabBlock(block, full, texture);
        itemModels().withExistingParent(name, Artisanal.location("block/" + name));
    }

    private void trim(String name, ResourceLocation end, TrimBlock block) {
        Map<TrimType, ModelFile> models = new EnumMap<>(TrimType.class);
        for (TrimType type : TrimType.values()) {
            models.put(type, models().cubeColumn(name + "_" + type.getSerializedName(),
                            Artisanal.location("block/" + name + "_" + type.getSerializedName()), end)
                    .texture("particle", end));
        }
        getVariantBuilder(block).forAllStates(state -> {
            Direction.Axis axis = state.getValue(TrimBlock.AXIS);
            return ConfiguredModel.builder()
                    .modelFile(models.get(state.getValue(TrimBlock.TYPE)))
                    .rotationX(axis == Direction.Axis.Y ? 0 : axis == Direction.Axis.X ? 90 : 270)
                    .rotationY(axis == Direction.Axis.X ? 90 : 0)
                    .build();
        });
        simpleBlockItem(block, models.get(TrimType.SINGLE));
    }

    private void stairs(String name, StairBlock block) {
        getVariantBuilder(block).forAllStatesExcept(state -> ConfiguredModel.builder()
                .modelFile(existing(name + shape(state) + (state.getValue(StairBlock.HALF) == Half.TOP ? "_top" : "")))
                .rotationY(rotation(state))
                .uvLock(true)
                .build(), StairBlock.WATERLOGGED);
        simpleBlockItem(block, existing(name));
    }

    private void trimStairs(String name, TrimStairBlock block) {
        getVariantBuilder(block).forAllStatesExcept(state -> ConfiguredModel.builder()
                .modelFile(existing(name + shape(state) + "_stairs"
                        + (state.getValue(TrimStairBlock.TYPE) == TrimType.SINGLE ? "_single" : "")))
                .rotationX(state.getValue(StairBlock.HALF) == Half.TOP ? 180 : 0)
                .rotationY(rotation(state))
                .build(), StairBlock.WATERLOGGED);
        simpleBlockItem(block, existing(name + "_stairs_single"));
    }

    private static String shape(BlockState state) {
        return switch (state.getValue(StairBlock.SHAPE)) {
            case STRAIGHT -> "";
            case INNER_LEFT, INNER_RIGHT -> "_inner";
            case OUTER_LEFT, OUTER_RIGHT -> "_outer";
        };
    }

    private static int rotation(BlockState state) {
        boolean top = state.getValue(StairBlock.HALF) == Half.TOP;
        int offset = switch (state.getValue(StairBlock.SHAPE)) {
            case STRAIGHT -> 0;
            case INNER_LEFT, OUTER_LEFT -> top ? 0 : 270;
            case INNER_RIGHT, OUTER_RIGHT -> top ? 90 : 0;
        };
        return (((int) state.getValue(StairBlock.FACING).toYRot() + 90) + offset) % 360;
    }

    private void slab(String name, ResourceLocation doubleModel, SlabBlock block) {
        ModelFile bottom = existing(name);
        ModelFile top = existing(name + "_top");
        ModelFile full = models().getExistingFile(doubleModel);
        getVariantBuilder(block).forAllStatesExcept(state -> ConfiguredModel.builder()
                .modelFile(switch (state.getValue(SlabBlock.TYPE)) {
                    case BOTTOM -> bottom;
                    case TOP -> top;
                    case DOUBLE -> full;
                })
                .build(), SlabBlock.WATERLOGGED);
        simpleBlockItem(block, bottom);
    }

    private ModelFile existing(String name) {
        return models().getExistingFile(Artisanal.location("block/" + name));
    }
}
