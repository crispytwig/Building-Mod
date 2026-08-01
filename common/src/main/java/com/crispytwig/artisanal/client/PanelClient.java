package com.crispytwig.artisanal.client;

import com.crispytwig.artisanal.block.PanelEntry;
import com.crispytwig.artisanal.network.PanelPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PanelClient {
    private static final Map<BlockPos, Map<Direction, Block>> PANELS = new ConcurrentHashMap<>();

    private PanelClient() {
    }

    public static void handle(PanelPayload payload) {
        if (payload.replaceAll()) {
            PANELS.clear();
        }
        for (PanelEntry entry : payload.entries()) {
            PANELS.compute(entry.pos().immutable(), (pos, present) -> {
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
            for (PanelEntry entry : payload.entries()) {
                BlockPos pos = entry.pos();
                minecraft.levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
            }
        });
    }

    public static boolean isEmpty() {
        return PANELS.isEmpty();
    }

    @Nullable
    public static Map<Direction, Block> panels(BlockPos pos) {
        return PANELS.get(pos);
    }

    public static boolean has(BlockPos pos, Direction face) {
        Map<Direction, Block> faces = PANELS.get(pos);
        return faces != null && faces.containsKey(face);
    }
}
