package com.crispytwig.artisanal.item;

import com.crispytwig.artisanal.network.AllayFlightPayload;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AllayFlightHandler {

    public static final int FLIGHT_TICKS = 15 * 20;
    public static final int COOLDOWN_TICKS = 30 * 20;
    public static final float FLY_SPEED = 0.03F;

    private static final Map<UUID, Integer> REMAINING = new HashMap<>();

    private AllayFlightHandler() {
    }

    public static void start(ServerPlayer player) {
        REMAINING.put(player.getUUID(), FLIGHT_TICKS);
        grant(player);
        poof(player, SoundEvents.ILLUSIONER_CAST_SPELL, 0.5F, 1.5F);
        sync(player, FLIGHT_TICKS);
    }

    public static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tickPlayer(player);
        }
        if (server.getTickCount() % 200 == 0) {
            REMAINING.keySet().removeIf(id -> server.getPlayerList().getPlayer(id) == null);
        }
    }

    private static void tickPlayer(ServerPlayer player) {
        Integer remaining = REMAINING.get(player.getUUID());
        if (remaining == null) {
            revoke(player);
            return;
        }

        int left = remaining - 1;
        if (left <= 0) {
            REMAINING.remove(player.getUUID());
            revoke(player);
            poof(player, SoundEvents.ALLAY_DEATH, 0.3F, 1.0F);
            sync(player, 0);
            return;
        }

        REMAINING.put(player.getUUID(), left);
        if (!player.getAbilities().mayfly) {
            grant(player);
        }
        if (left % 20 == 0) {
            sync(player, left);
        }
    }

    private static void grant(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        Abilities abilities = player.getAbilities();
        abilities.mayfly = true;
        abilities.flying = true;
        abilities.setFlyingSpeed(FLY_SPEED);
        player.onUpdateAbilities();
    }

    private static void revoke(ServerPlayer player) {
        Abilities abilities = player.getAbilities();
        if (!abilities.mayfly || abilities.getFlyingSpeed() != FLY_SPEED
                || player.isCreative() || player.isSpectator()) {
            return;
        }
        abilities.mayfly = false;
        abilities.flying = false;
        abilities.setFlyingSpeed(0.05F);
        player.onUpdateAbilities();
    }

    private static void poof(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        ServerLevel level = player.serverLevel();
        double y = player.getY() + player.getBbHeight() + 0.35;
        level.sendParticles(ParticleTypes.POOF, player.getX(), y, player.getZ(), 8, 0.15, 0.15, 0.15, 0.01);
        level.playSound(null, player.getX(), y, player.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static void sync(ServerPlayer player, int remaining) {
        player.serverLevel().getChunkSource().broadcastAndSend(player,
                new ClientboundCustomPayloadPacket(new AllayFlightPayload(player.getId(), remaining)));
    }
}
