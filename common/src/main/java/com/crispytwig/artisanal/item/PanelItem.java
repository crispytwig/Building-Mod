package com.crispytwig.artisanal.item;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.block.PanelData;
import com.crispytwig.artisanal.block.PanelEntry;
import com.crispytwig.artisanal.client.PanelClient;
import com.crispytwig.artisanal.network.PanelPayload;
import com.crispytwig.artisanal.registry.ModDataComponents;
import com.crispytwig.artisanal.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PanelItem extends Item {
    public static final TagKey<Block> MATERIALS = TagKey.create(Registries.BLOCK, Artisanal.location("panel_materials"));

    private static final int SYNC_BATCH = 4096;

    public PanelItem(Properties properties) {
        super(properties);
    }

    public static ItemStack of(Block block, int count) {
        ItemStack stack = new ItemStack(ModItems.PANEL.get(), count);
        stack.set(ModDataComponents.PANEL_BLOCK.get(), block);
        return stack;
    }

    @Nullable
    public static Block getBlock(ItemStack stack) {
        return stack.get(ModDataComponents.PANEL_BLOCK.get());
    }

    public static boolean isMaterial(Block block) {
        BlockState state = block.defaultBlockState();
        return state.is(MATERIALS) || state.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
    }

    public static void onBlockBroken(ServerLevel level, BlockPos pos, boolean drop) {
        Map<Direction, Block> removed = PanelData.get(level).removeAll(pos);
        if (removed.isEmpty()) {
            return;
        }
        List<PanelEntry> entries = new ArrayList<>();
        removed.forEach((face, block) -> {
            entries.add(new PanelEntry(pos, face, Optional.empty()));
            if (drop) {
                Block.popResource(level, pos, of(block, 1));
            }
        });
        broadcast(level, entries);
    }

    public static InteractionResult tryRemove(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        BlockPos pos = hit.getBlockPos();
        Direction face = hit.getDirection();
        if (!stack.is(ItemTags.AXES)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return PanelClient.has(pos, face) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        Block removed = PanelData.get(serverLevel).remove(pos, face);
        if (removed == null) {
            return InteractionResult.PASS;
        }
        broadcast(serverLevel, List.of(new PanelEntry(pos, face, Optional.empty())));
        if (!player.getAbilities().instabuild) {
            Block.popResource(level, pos.relative(face), of(removed, 1));
        }
        level.playSound(null, pos, removed.defaultBlockState().getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        return InteractionResult.CONSUME;
    }

    public static void sync(ServerPlayer player) {
        List<PanelEntry> entries = PanelData.get(player.serverLevel()).entries();
        send(player, new PanelPayload(true, entries.subList(0, Math.min(SYNC_BATCH, entries.size()))));
        for (int from = SYNC_BATCH; from < entries.size(); from += SYNC_BATCH) {
            send(player, new PanelPayload(false, entries.subList(from, Math.min(from + SYNC_BATCH, entries.size()))));
        }
    }

    private static void broadcast(ServerLevel level, List<PanelEntry> entries) {
        PanelPayload payload = new PanelPayload(false, entries);
        level.players().forEach(player -> send(player, payload));
    }

    private static void send(ServerPlayer player, PanelPayload payload) {
        player.connection.send(new ClientboundCustomPayloadPacket(payload));
    }

    private static List<Direction> exposedFaces(BlockGetter level, BlockPos pos, BlockState state, Direction first) {
        List<Direction> faces = new ArrayList<>();
        for (Direction face : Direction.values()) {
            if (Block.shouldRenderFace(state, level, pos, face, pos.relative(face))) {
                faces.add(face);
            }
        }
        if (faces.remove(first)) {
            faces.addFirst(first);
        }
        return faces;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        Block block = getBlock(stack);
        return block == null
                ? super.getName(stack)
                : Component.translatable(getDescriptionId() + ".material", block.getName());
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Block material = getBlock(context.getItemInHand());
        if (material == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getShape(level, pos, CollisionContext.empty()).isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        List<Direction> faces = context.isSecondaryUseActive()
                ? exposedFaces(level, pos, state, context.getClickedFace())
                : List.of(context.getClickedFace());
        int limit = player != null && player.getAbilities().instabuild ? faces.size() : stack.getCount();

        PanelData data = PanelData.get((ServerLevel) level);
        List<PanelEntry> applied = new ArrayList<>();
        for (Direction face : faces) {
            if (applied.size() >= limit) {
                break;
            }
            if (data.set(pos, face, material)) {
                applied.add(new PanelEntry(pos, face, Optional.of(material)));
            }
        }
        if (applied.isEmpty()) {
            return InteractionResult.PASS;
        }
        broadcast((ServerLevel) level, applied);
        level.playSound(null, pos, material.defaultBlockState().getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        stack.consume(applied.size(), player);
        return InteractionResult.CONSUME;
    }
}
