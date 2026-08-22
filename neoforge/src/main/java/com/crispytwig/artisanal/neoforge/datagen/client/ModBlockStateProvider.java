package com.crispytwig.artisanal.neoforge.datagen.client;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.block.BeamBlock;
import com.crispytwig.artisanal.block.ChairBlock;
import com.crispytwig.artisanal.block.FrameBlock;
import com.crispytwig.artisanal.block.ShutterBlock;
import com.crispytwig.artisanal.block.CrossType;
import com.crispytwig.artisanal.block.TableBlock;
import com.crispytwig.artisanal.block.TimberFrameBlock;
import com.crispytwig.artisanal.block.TrimBlock;
import com.crispytwig.artisanal.block.TrimStairBlock;
import com.crispytwig.artisanal.block.TrimType;
import com.crispytwig.artisanal.block.WindowBlock;
import com.crispytwig.artisanal.block.WindowPaneBlock;
import com.crispytwig.artisanal.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.Half;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
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
        cube(ModBlocks.OAK_BOARDS.get());
        cubeStairs(ModBlocks.OAK_BOARD_STAIRS.get(), Artisanal.location("block/oak_boards"));
        cubeSlab(ModBlocks.OAK_BOARD_SLAB.get(), Artisanal.location("block/oak_boards"), Artisanal.location("block/oak_boards"));
        cube(ModBlocks.POLISHED_OAK.get());
        slab("polished_oak_slab", Artisanal.location("block/polished_oak_slab_full"), ModBlocks.POLISHED_OAK_SLAB.get());

        trim("oak_trim", Artisanal.location("block/oak_boards"), ModBlocks.OAK_TRIM.get());
        trimStairs("oak_trim", ModBlocks.OAK_TRIM_STAIRS.get());

        pillar(ModBlocks.OAK_PILLAR.get(), Artisanal.location("block/oak_pillar"), Artisanal.location("block/oak_pillar_top"));
        pillarStairs("oak_pillar", ModBlocks.OAK_PILLAR_STAIRS.get());
        pillarSlab(ModBlocks.OAK_PILLAR_SLAB.get(), Artisanal.location("block/oak_pillar"), Artisanal.location("block/oak_pillar"), Artisanal.location("block/oak_pillar_top"));
        beam(ModBlocks.OAK_BEAM.get(), Artisanal.location("block/oak_pillar"), Artisanal.location("block/oak_pillar_top"));

        cube(ModBlocks.PRISMARINE_TILES.get());
        cubeStairs(ModBlocks.PRISMARINE_TILE_STAIRS.get(), Artisanal.location("block/prismarine_tiles"));
        cubeSlab(ModBlocks.PRISMARINE_TILE_SLAB.get(), Artisanal.location("block/prismarine_tiles"), Artisanal.location("block/prismarine_tiles"));

        table(ModBlocks.OAK_TABLE.get(), Artisanal.location("block/oak_boards"));
        chair(ModBlocks.OAK_CHAIR.get(), Artisanal.location("block/oak_boards"));
        frame(ModBlocks.OAK_FRAME.get());
        shutter(ModBlocks.OAK_SHUTTER.get());
        timberFrame(ModBlocks.OAK_TIMBER_FRAME.get());
        window(ModBlocks.OAK_WINDOW.get());
        windowPane(ModBlocks.OAK_WINDOW_PANE.get(), "oak_window", Artisanal.location("block/oak_trim_end"));

        ModBlocks.TERRACOTTA.forEach(colored -> colored.sets().forEach(this::cubeSet));
        ModBlocks.PLASTER.forEach(colored -> colored.sets().forEach(this::cubeSet));
    }

    private void cubeSet(ModBlocks.BlockSet set) {
        ResourceLocation texture = blockTexture(set.block().get());
        cube(set.block().get());
        cubeStairs(set.stairs().get(), texture);
        cubeSlab(set.slab().get(), texture, texture);
    }

    private void table(TableBlock block, ResourceLocation particle) {
        String name = Artisanal.name(block);
        ResourceLocation texture = blockTexture(block);

        ModelFile inventory = templateModel(name + "_inventory", "table_full", texture, particle);
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(inventory).build());
        itemModels().withExistingParent(name, Artisanal.location("block/" + name + "_inventory"));

        templateModel(name + "_" + TableBlock.TOP_PART, "table_" + TableBlock.TOP_PART, texture, particle);
        for (String part : TableBlock.LEG_PARTS) {
            templateModel(name + "_" + part, "table_" + part, texture, particle);
        }
    }

    private void frame(FrameBlock block) {
        String name = Artisanal.name(block);
        ResourceLocation texture = blockTexture(block);

        ModelFile inventory = templateModel(name + "_inventory", "frame_inventory", texture, texture);
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(inventory).build());
        itemModels().withExistingParent(name, Artisanal.location("block/" + name + "_inventory"));

        for (String part : FrameBlock.PARTS) {
            templateModel(name + "_" + part, "frame_" + part, texture, texture);
        }
    }

    private void chair(ChairBlock block, ResourceLocation particle) {
        String name = Artisanal.name(block);
        ResourceLocation texture = blockTexture(block);

        ModelFile normal = templateModel(name, "chair", texture, particle).renderType("cutout");
        ModelFile tucked = templateModel(name + "_tucked", "chair_tucked", texture, particle).renderType("cutout");
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(ChairBlock.TUCKED) ? tucked : normal)
                .rotationY(((int) state.getValue(ChairBlock.FACING).toYRot() + 180) % 360)
                .build());
        simpleBlockItem(block, normal);
    }

    private void pillar(RotatedPillarBlock block, ResourceLocation side, ResourceLocation end) {
        String name = Artisanal.name(block);
        axisBlock(block, side, end);
        itemModels().withExistingParent(name, Artisanal.location("block/" + name));
    }

    private void pillarStairs(String name, StairBlock block) {
        getVariantBuilder(block).forAllStatesExcept(state -> ConfiguredModel.builder()
                .modelFile(existing(name + shape(state) + "_stairs"))
                .rotationX(state.getValue(StairBlock.HALF) == Half.TOP ? 180 : 0)
                .rotationY(rotation(state))
                .build(), StairBlock.WATERLOGGED);
        simpleBlockItem(block, existing(name + "_stairs"));
    }

    private void pillarSlab(SlabBlock block, ResourceLocation full, ResourceLocation side, ResourceLocation end) {
        String name = Artisanal.name(block);
        slabBlock(block, full, side, end, end);
        itemModels().withExistingParent(name, Artisanal.location("block/" + name));
    }

    private void beam(BeamBlock block, ResourceLocation side, ResourceLocation end) {
        String name = Artisanal.name(block);
        ModelFile bottom = beamModel(name + "_bottom", "beam_bottom", side, end);
        ModelFile center = beamModel(name + "_center", "beam_center", side, end);
        ModelFile top = beamModel(name + "_top", "beam_top", side, end);
        beamModel(name + "_inventory", "beam_inventory", side, end);

        var builder = getMultipartBuilder(block);
        builder.part().modelFile(bottom).addModel().condition(BeamBlock.Y, true).end();
        builder.part().modelFile(top).addModel().condition(BeamBlock.Y, true).end();
        builder.part().modelFile(bottom).rotationX(90).addModel().condition(BeamBlock.Z, true).end();
        builder.part().modelFile(top).rotationX(90).addModel().condition(BeamBlock.Z, true).end();
        builder.part().modelFile(bottom).rotationX(90).rotationY(90).addModel().condition(BeamBlock.X, true).end();
        builder.part().modelFile(top).rotationX(90).rotationY(90).addModel().condition(BeamBlock.X, true).end();
        builder.part().modelFile(center).addModel().condition(BeamBlock.Y, true).end();
        builder.part().modelFile(center).rotationX(90).addModel().condition(BeamBlock.Y, false).condition(BeamBlock.Z, true).end();
        builder.part().modelFile(center).rotationX(90).rotationY(90).addModel().condition(BeamBlock.X, true).condition(BeamBlock.Y, false).condition(BeamBlock.Z, false).end();

        itemModels().withExistingParent(name, Artisanal.location("block/" + name + "_inventory"));
    }

    private BlockModelBuilder beamModel(String name, String template, ResourceLocation side, ResourceLocation end) {
        return models().withExistingParent(name, Artisanal.location("block/template/" + template))
                .texture("side", side)
                .texture("end", end)
                .texture("particle", side);
    }

    private void shutter(ShutterBlock block) {
        String name = Artisanal.name(block);
        ResourceLocation texture = blockTexture(block);

        ModelFile closed = templateModel(name, "shutter", texture, texture).renderType("cutout");
        ModelFile leftOpen = templateModel(name + "_left_open", "shutter_left_open", texture, texture).renderType("cutout");
        ModelFile rightOpen = templateModel(name + "_right_open", "shutter_right_open", texture, texture).renderType("cutout");
        templateModel(name + "_inventory", "shutter_inventory", texture, texture).renderType("cutout");

        getVariantBuilder(block).forAllStatesExcept(state -> ConfiguredModel.builder()
                .modelFile(!state.getValue(ShutterBlock.OPEN) ? closed
                        : state.getValue(ShutterBlock.HINGE) == DoorHingeSide.LEFT ? leftOpen : rightOpen)
                .rotationY((int) state.getValue(ShutterBlock.FACING).toYRot())
                .build(), ShutterBlock.POWERED);
        itemModels().withExistingParent(name, Artisanal.location("block/" + name + "_inventory"));
    }

    private void timberFrame(TimberFrameBlock block) {
        String name = Artisanal.name(block);
        ResourceLocation end = Artisanal.location("block/" + name);

        Map<TrimType, ModelFile> models = new EnumMap<>(TrimType.class);
        for (TrimType type : TrimType.values()) {
            models.put(type, models().cubeColumn(name + suffix(type), Artisanal.location("block/" + name + suffix(type)), end).renderType("cutout"));
        }

        Map<CrossType, ModelFile> crossModels = new EnumMap<>(CrossType.class);
        for (CrossType cross : CrossType.values()) {
            if (cross != CrossType.NONE) {
                String crossName = name + "_" + cross.getSerializedName();
                crossModels.put(cross, models().cubeColumn(crossName, Artisanal.location("block/" + crossName), end).renderType("cutout"));
            }
        }

        getVariantBuilder(block).forAllStates(state -> {
            TrimType type = state.getValue(TimberFrameBlock.TYPE);
            CrossType cross = state.getValue(TimberFrameBlock.CROSS);
            return ConfiguredModel.builder()
                    .modelFile(type == TrimType.SINGLE && cross != CrossType.NONE ? crossModels.get(cross) : models.get(type))
                    .build();
        });
        itemModels().withExistingParent(name, Artisanal.location("block/" + name));
    }

    private void window(WindowBlock block) {
        String name = Artisanal.name(block);
        ResourceLocation end = Artisanal.location("block/" + name);

        Map<TrimType, ModelFile> models = new EnumMap<>(TrimType.class);
        for (TrimType type : TrimType.values()) {
            models.put(type, models().cubeColumn(name + suffix(type), Artisanal.location("block/" + name + suffix(type)), end).renderType("translucent"));
        }
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(models.get(state.getValue(WindowBlock.TYPE))).build());
        itemModels().withExistingParent(name, Artisanal.location("block/" + name));
    }

    private void windowPane(WindowPaneBlock block, String paneTexture, ResourceLocation edge) {
        String name = Artisanal.name(block);
        BooleanProperty[] sides = {WindowPaneBlock.NORTH, WindowPaneBlock.EAST, WindowPaneBlock.SOUTH, WindowPaneBlock.WEST};
        int[][] sideVariants = {{0, 0}, {0, 90}, {1, 0}, {1, 90}};
        int[][] noSideVariants = {{0, 0}, {1, 0}, {1, 90}, {0, 270}};

        var builder = getMultipartBuilder(block);
        for (TrimType type : TrimType.values()) {
            ResourceLocation pane = Artisanal.location("block/" + paneTexture + suffix(type));

            ModelFile[] sideModels = {
                    models().paneSide(name + "_side" + suffix(type), pane, edge).renderType("translucent"),
                    models().paneSideAlt(name + "_side_alt" + suffix(type), pane, edge).renderType("translucent")
            };
            ModelFile[] noSideModels = {
                    models().singleTexture(name + "_noside" + suffix(type), Artisanal.location("block/template_pane_noside"), "pane", pane).renderType("translucent"),
                    models().singleTexture(name + "_noside_alt" + suffix(type), Artisanal.location("block/template_pane_noside_alt"), "pane", pane).renderType("translucent")
            };

            builder.part().modelFile(models().panePost(name + "_post" + suffix(type), pane, edge).renderType("translucent")).addModel()
                    .condition(WindowPaneBlock.TYPE, type).end();

            for (int i = 0; i < sides.length; i++) {
                builder.part().modelFile(sideModels[sideVariants[i][0]]).rotationY(sideVariants[i][1]).addModel()
                        .condition(sides[i], true).condition(WindowPaneBlock.TYPE, type).end();
                builder.part().modelFile(noSideModels[noSideVariants[i][0]]).rotationY(noSideVariants[i][1]).addModel()
                        .condition(sides[i], false).condition(WindowPaneBlock.TYPE, type).end();
            }
        }
        itemModels().withExistingParent(name, mcLoc("item/generated")).texture("layer0", Artisanal.location("item/" + name));
    }

    private void trim(String name, ResourceLocation end, TrimBlock block) {
        Map<TrimType, ModelFile> models = new EnumMap<>(TrimType.class);
        for (TrimType type : TrimType.values()) {
            models.put(type, models().cubeColumn(name + "_" + type.getSerializedName(), Artisanal.location("block/" + name + "_" + type.getSerializedName()), end).texture("particle", end));
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

    private void trimStairs(String name, TrimStairBlock block) {
        getVariantBuilder(block).forAllStatesExcept(state -> ConfiguredModel.builder()
                .modelFile(existing(name + shape(state) + "_stairs" + (state.getValue(TrimStairBlock.TYPE) == TrimType.SINGLE ? "_single" : "")))
                .rotationX(state.getValue(StairBlock.HALF) == Half.TOP ? 180 : 0)
                .rotationY(rotation(state))
                .build(), StairBlock.WATERLOGGED);
        simpleBlockItem(block, existing(name + "_stairs_single"));
    }

    private void cube(Block block) {
        String name = Artisanal.name(block);
        simpleBlockWithItem(block, models().cubeAll(name, Artisanal.location("block/" + name)));
    }

    private void cubeStairs(StairBlock block, ResourceLocation texture) {
        String name = Artisanal.name(block);
        stairsBlock(block, texture);
        itemModels().withExistingParent(name, Artisanal.location("block/" + name));
    }

    private void cubeSlab(SlabBlock block, ResourceLocation full, ResourceLocation texture) {
        String name = Artisanal.name(block);
        slabBlock(block, full, texture);
        itemModels().withExistingParent(name, Artisanal.location("block/" + name));
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

    private BlockModelBuilder templateModel(String name, String template, ResourceLocation texture, ResourceLocation particle) {
        return models().withExistingParent(name, Artisanal.location("block/template/" + template)).texture("all", texture).texture("particle", particle);
    }

    private ModelFile existing(String name) {
        return models().getExistingFile(Artisanal.location("block/" + name));
    }

    private static String suffix(TrimType type) {
        return type == TrimType.SINGLE ? "" : "_" + type.getSerializedName();
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
}
