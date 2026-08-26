package com.crispytwig.bbb.block;

import com.crispytwig.bbb.BuildingButBetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FacadeData extends SavedData {
    private final Map<BlockPos, Map<Direction, Block>> faces = new HashMap<>();

    public static FacadeData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(FacadeData::new, (tag, registries) -> load(tag), null), BuildingButBetter.MOD_ID + "_facades");
    }

    private static FacadeData load(CompoundTag tag) {
        FacadeData data = new FacadeData();
        ListTag list = tag.getList("facades", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            BlockPos pos = BlockPos.of(entry.getLong("pos"));
            for (Direction face : Direction.values()) {
                ResourceLocation id = ResourceLocation.tryParse(entry.getString(face.getSerializedName()));
                if (id != null) {
                    BuiltInRegistries.BLOCK.getOptional(id).ifPresent(block ->
                            data.faces.computeIfAbsent(pos, ignored -> new EnumMap<>(Direction.class)).put(face, block));
                }
            }
        }
        return data;
    }

    @Nullable
    public Block get(BlockPos pos, Direction face) {
        Map<Direction, Block> blocks = faces.get(pos);
        return blocks == null ? null : blocks.get(face);
    }

    public boolean set(BlockPos pos, Direction face, Block block) {
        if (faces.computeIfAbsent(pos.immutable(), ignored -> new EnumMap<>(Direction.class)).putIfAbsent(face, block) != null) {
            return false;
        }
        setDirty();
        return true;
    }

    @Nullable
    public Block remove(BlockPos pos, Direction face) {
        Map<Direction, Block> blocks = faces.get(pos);
        if (blocks == null) {
            return null;
        }
        Block removed = blocks.remove(face);
        if (removed != null) {
            if (blocks.isEmpty()) {
                faces.remove(pos);
            }
            setDirty();
        }
        return removed;
    }

    public Map<Direction, Block> removeAll(BlockPos pos) {
        Map<Direction, Block> removed = faces.remove(pos);
        if (removed == null) {
            return Map.of();
        }
        setDirty();
        return removed;
    }

    public List<FacadeEntry> entries() {
        List<FacadeEntry> entries = new ArrayList<>();
        faces.forEach((pos, blocks) -> blocks.forEach((face, block) -> entries.add(new FacadeEntry(pos, face, Optional.of(block)))));
        return entries;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        ListTag list = new ListTag();
        faces.forEach((pos, blocks) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong("pos", pos.asLong());
            blocks.forEach((face, block) -> entry.putString(face.getSerializedName(), BuiltInRegistries.BLOCK.getKey(block).toString()));
            list.add(entry);
        });
        tag.put("facades", list);
        return tag;
    }
}
