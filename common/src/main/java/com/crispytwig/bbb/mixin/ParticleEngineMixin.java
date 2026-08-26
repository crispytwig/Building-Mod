package com.crispytwig.bbb.mixin;

import com.crispytwig.bbb.block.Contents;
import com.crispytwig.bbb.block.DestroyShaped;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @Unique
    private static final RandomSource bbb$random = RandomSource.create();

    @Redirect(method = "destroy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
    private VoxelShape bbb$destroyShape(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.getBlock() instanceof DestroyShaped destroyShaped) {
            return destroyShaped.getDestroyShape(state);
        }
        return state.getShape(level, pos);
    }

    @Inject(method = "destroy", at = @At("HEAD"), cancellable = true)
    private void bbb$mixDestroyParticles(BlockPos pos, BlockState state, CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        BlockState contents = Contents.of(level, pos, state);
        if (contents.isAir()) {
            return;
        }

        ParticleEngine engine = (ParticleEngine) (Object) this;
        state.getShape(level, pos).forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double sizeX = Math.min(1.0, maxX - minX);
            double sizeY = Math.min(1.0, maxY - minY);
            double sizeZ = Math.min(1.0, maxZ - minZ);
            int countX = Math.max(2, Mth.ceil(sizeX / 0.25));
            int countY = Math.max(2, Mth.ceil(sizeY / 0.25));
            int countZ = Math.max(2, Mth.ceil(sizeZ / 0.25));

            for (int x = 0; x < countX; x++) {
                for (int y = 0; y < countY; y++) {
                    for (int z = 0; z < countZ; z++) {
                        double offsetX = ((double) x + 0.5) / (double) countX;
                        double offsetY = ((double) y + 0.5) / (double) countY;
                        double offsetZ = ((double) z + 0.5) / (double) countZ;
                        engine.add(new TerrainParticle(level,
                                (double) pos.getX() + offsetX * sizeX + minX,
                                (double) pos.getY() + offsetY * sizeY + minY,
                                (double) pos.getZ() + offsetZ * sizeZ + minZ,
                                offsetX - 0.5, offsetY - 0.5, offsetZ - 0.5,
                                bbb$random.nextBoolean() ? state : contents, pos));
                    }
                }
            }
        });
        ci.cancel();
    }

    @Redirect(method = "crack", at = @At(value = "NEW", target = "net/minecraft/client/particle/TerrainParticle"))
    private TerrainParticle bbb$crackParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, BlockState state, BlockPos pos) {
        BlockState contents = Contents.of(level, pos, state);
        return new TerrainParticle(level, x, y, z, xSpeed, ySpeed, zSpeed,
                contents.isAir() || bbb$random.nextBoolean() ? state : contents, pos);
    }
}
