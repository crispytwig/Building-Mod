package com.crispytwig.bbb.neoforge.datagen.client;

import com.crispytwig.bbb.BuildingButBetter;
import com.crispytwig.bbb.block.BeamBlock;
import com.crispytwig.bbb.block.ChairBlock;
import com.crispytwig.bbb.block.FrameBlock;
import com.crispytwig.bbb.block.ShutterBlock;
import com.crispytwig.bbb.block.SofaBlock;
import com.crispytwig.bbb.block.CrossType;
import com.crispytwig.bbb.block.TableBlock;
import com.crispytwig.bbb.block.TimberFrameBlock;
import com.crispytwig.bbb.block.TrimBlock;
import com.crispytwig.bbb.block.TrimStairBlock;
import com.crispytwig.bbb.block.TrimType;
import com.crispytwig.bbb.block.WindowBlock;
import com.crispytwig.bbb.block.WindowPaneBlock;
import com.crispytwig.bbb.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
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
import java.util.HashMap;
import java.util.Map;

public class ModBlockStateProvider extends BlockStateProvider {
    private static final String[] SHAPES = {"", "_inner", "_outer"};


    public ModBlockStateProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, BuildingButBetter.MOD_ID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModBlocks.WOOD.forEach(this::woodSet);

        ResourceLocation stoneTiles = BuildingButBetter.location("block/stone_tiles");
        cubeSet(ModBlocks.STONE_TILES);
        trim("stone_pillar", stoneTiles, ModBlocks.STONE_PILLAR.get());
        trimStairs("stone_pillar", ModBlocks.STONE_PILLAR_STAIRS.get(), stoneTiles, stoneTiles, stoneTiles);
        polishedSlab(ModBlocks.STONE_PILLAR_SLAB.get(), stoneTiles, BuildingButBetter.location("block/stone_pillar_single"));

        cube(ModBlocks.PRISMARINE_TILES.get());
        cubeStairs(ModBlocks.PRISMARINE_TILE_STAIRS.get(), BuildingButBetter.location("block/prismarine_tiles"));
        cubeSlab(ModBlocks.PRISMARINE_TILE_SLAB.get(), BuildingButBetter.location("block/prismarine_tiles"), BuildingButBetter.location("block/prismarine_tiles"));

        ModBlocks.TERRACOTTA.forEach(colored -> colored.sets().forEach(this::cubeSet));
        ModBlocks.PLASTER.forEach(colored -> colored.sets().forEach(this::cubeSet));

        ModBlocks.SOFAS.forEach((color, holder) -> sofa(holder.get(), color));
    }

    private void woodSet(ModBlocks.WoodSet set) {
        String wood = set.name();
        ResourceLocation boards = BuildingButBetter.location("block/" + wood + "_boards");
        ResourceLocation pillar = BuildingButBetter.location("block/" + wood + "_pillar");
        ResourceLocation pillarTop = BuildingButBetter.location("block/" + wood + "_pillar_top");

        cube(set.boards().get());
        cubeStairs(set.boardStairs().get(), boards);
        cubeSlab(set.boardSlab().get(), boards, boards);
        cube(set.polished().get());
        polishedSlab(set.polishedSlab().get(), BuildingButBetter.location("block/polished_" + wood), pillar);

        trim(wood + "_trim", boards, set.trim().get());
        trimStairs(wood + "_trim", set.trimStairs().get(), boards, pillar, BuildingButBetter.location("block/polished_" + wood));

        pillar(set.pillar().get(), pillar, pillarTop);
        pillarStairs(wood + "_pillar", set.pillarStairs().get(), pillar, pillarTop);
        pillarSlab(set.pillarSlab().get(), pillar, pillar, pillarTop);
        beam(set.beam().get(), pillar, pillarTop);

        table(set.table().get(), boards);
        chair(set.chair().get(), boards);
        frame(set.frame().get());
        shutter(set.shutter().get());
        timberFrame(set.timberFrame().get());
        window(set.window().get());
        windowPane(set.windowPane().get(), wood + "_window", BuildingButBetter.location("block/" + wood + "_trim_end"));
    }

    private void cubeSet(ModBlocks.BlockSet set) {
        ResourceLocation texture = blockTexture(set.block().get());
        cube(set.block().get());
        cubeStairs(set.stairs().get(), texture);
        cubeSlab(set.slab().get(), texture, texture);
    }

    private void table(TableBlock block, ResourceLocation particle) {
        String name = BuildingButBetter.name(block);
        ResourceLocation texture = blockTexture(block);

        ModelFile inventory = templateModel(name + "_inventory", "table_full", texture, particle);
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(inventory).build());
        itemModels().withExistingParent(name, BuildingButBetter.location("block/" + name + "_inventory"));

        templateModel(name + "_" + TableBlock.TOP_PART, "table_" + TableBlock.TOP_PART, texture, particle);
        for (String part : TableBlock.LEG_PARTS) {
            templateModel(name + "_" + part, "table_" + part, texture, particle);
        }
    }

    private void sofa(SofaBlock block, DyeColor color) {
        String name = BuildingButBetter.name(block);
        ResourceLocation texture = BuildingButBetter.location("block/sofa/" + name);
        ResourceLocation particle = mcLoc("block/" + color.getName() + "_wool");

        Map<String, ModelFile> parts = new HashMap<>();
        for (String part : SofaBlock.PARTS) {
            parts.put(part, templateModel(name + "_" + part, "sofa_" + part, texture, particle));
        }

        var builder = getMultipartBuilder(block);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            int yRot = Math.floorMod((int) facing.toYRot() + 180, 360);
            builder.part().modelFile(parts.get("backrest")).rotationY(yRot).addModel()
                    .condition(SofaBlock.FACING, facing).end();
            builder.part().modelFile(parts.get("seat")).rotationY(yRot).addModel()
                    .condition(SofaBlock.FACING, facing).end();
            builder.part().modelFile(parts.get("legs_left")).rotationY(yRot).addModel()
                    .condition(SofaBlock.FACING, facing).condition(SofaBlock.LEFT, false).end();
            builder.part().modelFile(parts.get("arm_left")).rotationY(yRot).addModel()
                    .condition(SofaBlock.FACING, facing).condition(SofaBlock.LEFT, false).end();
            builder.part().modelFile(parts.get("legs_right")).rotationY(yRot).addModel()
                    .condition(SofaBlock.FACING, facing).condition(SofaBlock.RIGHT, false).end();
            builder.part().modelFile(parts.get("arm_right")).rotationY(yRot).addModel()
                    .condition(SofaBlock.FACING, facing).condition(SofaBlock.RIGHT, false).end();
        }

        itemModels().withExistingParent(name, BuildingButBetter.location("block/template/sofa"))
                .texture("all", texture)
                .texture("particle", particle);
    }

    private void frame(FrameBlock block) {
        String name = BuildingButBetter.name(block);
        ResourceLocation texture = blockTexture(block);

        ModelFile inventory = templateModel(name + "_inventory", "frame_inventory", texture, texture);
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(inventory).build());
        itemModels().withExistingParent(name, BuildingButBetter.location("block/" + name + "_inventory"));

        for (String part : FrameBlock.PARTS) {
            templateModel(name + "_" + part, "frame_" + part, texture, texture);
        }
    }

    private void chair(ChairBlock block, ResourceLocation particle) {
        String name = BuildingButBetter.name(block);
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
        String name = BuildingButBetter.name(block);
        axisBlock(block, side, end);
        itemModels().withExistingParent(name, BuildingButBetter.location("block/" + name));
    }

    private void pillarStairs(String name, StairBlock block, ResourceLocation side, ResourceLocation end) {
        Map<String, ModelFile> models = new HashMap<>();
        for (String shape : SHAPES) {
            models.put(shape, models().withExistingParent(name + shape + "_stairs", BuildingButBetter.location("block/template/pillar" + shape + "_stairs"))
                    .texture("side", side)
                    .texture("bottom", end));
        }
        getVariantBuilder(block).forAllStatesExcept(state -> ConfiguredModel.builder()
                .modelFile(models.get(shape(state)))
                .rotationX(state.getValue(StairBlock.HALF) == Half.TOP ? 180 : 0)
                .rotationY(rotation(state))
                .build(), StairBlock.WATERLOGGED);
        simpleBlockItem(block, models.get(""));
    }

    private void pillarSlab(SlabBlock block, ResourceLocation full, ResourceLocation side, ResourceLocation end) {
        String name = BuildingButBetter.name(block);
        slabBlock(block, full, side, end, end);
        itemModels().withExistingParent(name, BuildingButBetter.location("block/" + name));
    }

    private void beam(BeamBlock block, ResourceLocation side, ResourceLocation end) {
        String name = BuildingButBetter.name(block);
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

        itemModels().withExistingParent(name, BuildingButBetter.location("block/" + name + "_inventory"));
    }

    private BlockModelBuilder beamModel(String name, String template, ResourceLocation side, ResourceLocation end) {
        return models().withExistingParent(name, BuildingButBetter.location("block/template/" + template))
                .texture("side", side)
                .texture("end", end)
                .texture("particle", side);
    }

    private void shutter(ShutterBlock block) {
        String name = BuildingButBetter.name(block);
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
        itemModels().withExistingParent(name, BuildingButBetter.location("block/" + name + "_inventory"));
    }

    private void timberFrame(TimberFrameBlock block) {
        String name = BuildingButBetter.name(block);
        ResourceLocation end = BuildingButBetter.location("block/" + name);

        Map<TrimType, ModelFile> models = new EnumMap<>(TrimType.class);
        for (TrimType type : TrimType.values()) {
            models.put(type, models().cubeColumn(name + suffix(type), BuildingButBetter.location("block/" + name + suffix(type)), end).renderType("cutout"));
        }

        Map<CrossType, ModelFile> crossModels = new EnumMap<>(CrossType.class);
        for (CrossType cross : CrossType.values()) {
            if (cross != CrossType.NONE) {
                String crossName = name + "_" + cross.getSerializedName();
                crossModels.put(cross, models().cubeColumn(crossName, BuildingButBetter.location("block/" + crossName), end).renderType("cutout"));
            }
        }

        getVariantBuilder(block).forAllStatesExcept(state -> {
            TrimType type = state.getValue(TimberFrameBlock.TYPE);
            CrossType cross = state.getValue(TimberFrameBlock.CROSS);
            return ConfiguredModel.builder()
                    .modelFile(type == TrimType.SINGLE && cross != CrossType.NONE ? crossModels.get(cross) : models.get(type))
                    .build();
        }, TimberFrameBlock.FILLED);
        itemModels().withExistingParent(name, BuildingButBetter.location("block/" + name));
    }

    private void window(WindowBlock block) {
        String name = BuildingButBetter.name(block);
        ResourceLocation end = BuildingButBetter.location("block/" + name);

        Map<TrimType, ModelFile> models = new EnumMap<>(TrimType.class);
        for (TrimType type : TrimType.values()) {
            models.put(type, models().cubeColumn(name + suffix(type), BuildingButBetter.location("block/" + name + suffix(type)), end).renderType("translucent"));
        }
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(models.get(state.getValue(WindowBlock.TYPE))).build());
        itemModels().withExistingParent(name, BuildingButBetter.location("block/" + name));
    }

    private void windowPane(WindowPaneBlock block, String paneTexture, ResourceLocation edge) {
        String name = BuildingButBetter.name(block);
        BooleanProperty[] sides = {WindowPaneBlock.NORTH, WindowPaneBlock.EAST, WindowPaneBlock.SOUTH, WindowPaneBlock.WEST};
        int[][] sideVariants = {{0, 0}, {0, 90}, {1, 0}, {1, 90}};
        int[][] noSideVariants = {{0, 0}, {1, 0}, {1, 90}, {0, 270}};

        var builder = getMultipartBuilder(block);
        for (TrimType type : TrimType.values()) {
            ResourceLocation pane = BuildingButBetter.location("block/" + paneTexture + suffix(type));

            ModelFile[] sideModels = {
                    models().paneSide(name + "_side" + suffix(type), pane, edge).renderType("translucent"),
                    models().paneSideAlt(name + "_side_alt" + suffix(type), pane, edge).renderType("translucent")
            };
            ModelFile[] noSideModels = {
                    models().singleTexture(name + "_noside" + suffix(type), BuildingButBetter.location("block/template_pane_noside"), "pane", pane).renderType("translucent"),
                    models().singleTexture(name + "_noside_alt" + suffix(type), BuildingButBetter.location("block/template_pane_noside_alt"), "pane", pane).renderType("translucent")
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
        itemModels().withExistingParent(name, mcLoc("item/generated")).texture("layer0", BuildingButBetter.location("item/" + name));
    }

    private void trim(String name, ResourceLocation end, TrimBlock block) {
        Map<TrimType, ModelFile> models = new EnumMap<>(TrimType.class);
        for (TrimType type : TrimType.values()) {
            models.put(type, models().cubeColumn(name + "_" + type.getSerializedName(), BuildingButBetter.location("block/" + name + "_" + type.getSerializedName()), end).texture("particle", end));
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

    private void trimStairs(String name, TrimStairBlock block, ResourceLocation boards, ResourceLocation pillar, ResourceLocation polished) {
        Map<String, ModelFile> models = new HashMap<>();
        for (String shape : SHAPES) {
            for (String type : new String[]{"", "_single"}) {
                models.put(shape + type, models().withExistingParent(name + shape + "_stairs" + type, BuildingButBetter.location("block/template/trim" + shape + "_stairs" + type))
                        .texture("particle", boards)
                        .texture("top", boards)
                        .texture("side", BuildingButBetter.location("block/" + name + (type.isEmpty() ? "_top" : "_single")))
                        .texture("stair", shape.isEmpty() ? pillar : polished));
            }
        }
        getVariantBuilder(block).forAllStatesExcept(state -> ConfiguredModel.builder()
                .modelFile(models.get(shape(state) + (state.getValue(TrimStairBlock.TYPE) == TrimType.SINGLE ? "_single" : "")))
                .rotationX(state.getValue(StairBlock.HALF) == Half.TOP ? 180 : 0)
                .rotationY(rotation(state))
                .build(), StairBlock.WATERLOGGED);
        simpleBlockItem(block, models.get("_single"));
    }

    private void cube(Block block) {
        String name = BuildingButBetter.name(block);
        simpleBlockWithItem(block, models().cubeAll(name, BuildingButBetter.location("block/" + name)));
    }

    private void cubeStairs(StairBlock block, ResourceLocation texture) {
        String name = BuildingButBetter.name(block);
        stairsBlock(block, texture);
        itemModels().withExistingParent(name, BuildingButBetter.location("block/" + name));
    }

    private void cubeSlab(SlabBlock block, ResourceLocation full, ResourceLocation texture) {
        String name = BuildingButBetter.name(block);
        slabBlock(block, full, texture);
        itemModels().withExistingParent(name, BuildingButBetter.location("block/" + name));
    }

    private void polishedSlab(SlabBlock block, ResourceLocation polished, ResourceLocation side) {
        String name = BuildingButBetter.name(block);
        ModelFile bottom = polishedSlabModel(name, "polished_slab", polished, side);
        ModelFile top = polishedSlabModel(name + "_top", "polished_slab_top", polished, side);
        ModelFile full = polishedSlabModel(name + "_full", "polished_slab_full", polished, side);
        getVariantBuilder(block).forAllStatesExcept(state -> ConfiguredModel.builder()
                .modelFile(switch (state.getValue(SlabBlock.TYPE)) {
                    case BOTTOM -> bottom;
                    case TOP -> top;
                    case DOUBLE -> full;
                })
                .build(), SlabBlock.WATERLOGGED);
        simpleBlockItem(block, bottom);
    }

    private BlockModelBuilder polishedSlabModel(String name, String template, ResourceLocation polished, ResourceLocation side) {
        return models().withExistingParent(name, BuildingButBetter.location("block/template/" + template))
                .texture("particle", polished)
                .texture("bottom", polished)
                .texture("top", polished)
                .texture("side", side);
    }

    private BlockModelBuilder templateModel(String name, String template, ResourceLocation texture, ResourceLocation particle) {
        return models().withExistingParent(name, BuildingButBetter.location("block/template/" + template)).texture("all", texture).texture("particle", particle);
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
