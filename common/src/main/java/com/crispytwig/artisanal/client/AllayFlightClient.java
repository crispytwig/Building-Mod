package com.crispytwig.artisanal.client;

import com.crispytwig.artisanal.network.AllayFlightPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class AllayFlightClient {

    private static final Map<Integer, Anim> ANIMS = new HashMap<>();
    private static final Anim NONE = new Anim();

    private AllayFlightClient() {
    }

    public static final class Anim {
        private long end;
        private float time;
        private float timeO;
        private float amount;
        private float amountO;
        private float pitch;
        private float pitchO;
        private float roll;
        private float rollO;

        public float remaining(Entity player, float partialTick) {
            return (float) (this.end - player.level().getGameTime()) - partialTick;
        }

        public float time(float partialTick) {
            return Mth.lerp(partialTick, this.timeO, this.time);
        }

        public float amount(float partialTick) {
            return Mth.lerp(partialTick, this.amountO, this.amount);
        }

        public float pitch(float partialTick) {
            return Mth.lerp(partialTick, this.pitchO, this.pitch);
        }

        public float roll(float partialTick) {
            return Mth.lerp(partialTick, this.rollO, this.roll);
        }
    }

    public static Anim of(Entity player) {
        return ANIMS.getOrDefault(player.getId(), NONE);
    }

    public static void handleSync(AllayFlightPayload payload) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        if (payload.remainingTicks() <= 0) {
            ANIMS.remove(payload.entityId());
        } else {
            ANIMS.computeIfAbsent(payload.entityId(), id -> new Anim()).end =
                    level.getGameTime() + payload.remainingTicks();
        }
    }

    public static void tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            ANIMS.clear();
            return;
        }

        Iterator<Map.Entry<Integer, Anim>> entries = ANIMS.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<Integer, Anim> entry = entries.next();
            Anim anim = entry.getValue();
            if (anim.end <= level.getGameTime() || !(level.getEntity(entry.getKey()) instanceof Player player)) {
                entries.remove();
                continue;
            }

            double dx = player.getX() - player.xo;
            double dy = player.getY() - player.yo;
            double dz = player.getZ() - player.zo;
            float moved = (float) Mth.length(dx, dy, dz);
            float rise = Mth.clamp((float) dy / 0.12F, 0.0F, 1.0F);

            anim.timeO = anim.time;
            anim.time += Mth.lerp(rise, 1.0F, 2.5F);
            anim.amountO = anim.amount;
            anim.amount += (Mth.clamp(moved / 0.12F, 0.0F, 1.0F) - anim.amount) * 0.25F;

            double cloakX = player.xCloak - player.getX();
            double cloakY = player.yCloak - player.getY();
            double cloakZ = player.zCloak - player.getZ();
            double sin = Mth.sin(player.yBodyRot * Mth.DEG_TO_RAD);
            double cos = -Mth.cos(player.yBodyRot * Mth.DEG_TO_RAD);
            float lift = Mth.clamp((float) cloakY * 10.0F, -6.0F, 32.0F)
                    + Mth.sin(player.walkDist * 6.0F) * 32.0F * player.bob;
            if (player.isCrouching()) {
                lift += 25.0F;
            }
            float swing = Mth.clamp((float) (cloakX * sin + cloakZ * cos) * 100.0F, 0.0F, 150.0F);
            float twist = Mth.clamp((float) (cloakX * cos - cloakZ * sin) * 100.0F, -20.0F, 20.0F);

            anim.pitchO = anim.pitch;
            anim.rollO = anim.roll;
            anim.pitch += (Mth.clamp(6.0F + swing / 2.0F + lift, -20.0F, 45.0F) * Mth.DEG_TO_RAD - anim.pitch) * 0.35F;
            anim.roll += (twist / 2.0F * Mth.DEG_TO_RAD - anim.roll) * 0.15F;
        }
    }
}
