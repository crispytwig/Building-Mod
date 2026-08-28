package com.crispytwig.bbb.client;

import com.crispytwig.bbb.common.block.FacadeEntry;
import com.crispytwig.bbb.common.network.FacadePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FacadeClient {
    private static final Map<BlockPos, Map<Direction, Block>> FACADES = new ConcurrentHashMap<>();

    private FacadeClient() {
    }

    public static void handle(FacadePayload payload) {
        if (payload.replaceAll()) {
            FACADES.clear();
        }
        for (FacadeEntry entry : payload.entries()) {
            FACADES.compute(entry.pos().immutable(), (pos, present) -> {
                EnumMap<Direction, Block> faces = present == null ? new EnumMap<>(Direction.class) : new EnumMap<>(present);
                entry.block().ifPresentOrElse(block -> faces.put(entry.face(), block), () -> faces.remove(entry.face()));
                return faces.isEmpty() ? null : faces;
            });
        }
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (payload.replaceAll()) {
                minecraft.levelRenderer.allChanged();
                return;
            }
            for (FacadeEntry entry : payload.entries()) {
                BlockPos pos = entry.pos();
                minecraft.levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
            }
        });
    }

    public static boolean isEmpty() {
        return FACADES.isEmpty();
    }

    @Nullable
    public static Map<Direction, Block> facades(BlockPos pos) {
        return FACADES.get(pos);
    }

    public static boolean has(BlockPos pos, Direction face) {
        Map<Direction, Block> faces = FACADES.get(pos);
        return faces != null && faces.containsKey(face);
    }
}
