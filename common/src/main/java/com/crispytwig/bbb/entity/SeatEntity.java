package com.crispytwig.bbb.entity;

import com.crispytwig.bbb.block.Seat;
import com.crispytwig.bbb.block.TableBlock;
import com.crispytwig.bbb.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SeatEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_FACING = SynchedEntityData.defineId(SeatEntity.class, EntityDataSerializers.INT);

    private BlockPos seatPos;

    public SeatEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public SeatEntity(Level level, BlockPos pos, Direction facing) {
        this(ModEntityTypes.SEAT.get(), level);
        this.seatPos = pos;
        this.entityData.set(DATA_FACING, facing.get2DDataValue());
        setYRot(facing.toYRot());
        refreshPosition();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_FACING, Direction.NORTH.get2DDataValue());
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        if (seatPos != null) {
            if (!(level().getBlockState(seatPos).getBlock() instanceof Seat)) {
                ejectPassengers();
                discard();
                return;
            }
            refreshPosition();
        }
        if (getPassengers().isEmpty()) {
            discard();
        }
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction move) {
        if (!hasPassenger(passenger)) {
            return;
        }
        Vec3 pos = position();
        if (passenger instanceof Player) {
            move.accept(passenger, pos.x, pos.y, pos.z);
            return;
        }
        Direction facing = getFacing();
        move.accept(passenger, pos.x + facing.getStepX() * 0.2, pos.y + 0.55, pos.z + facing.getStepZ() * 0.2);
        passenger.setYRot(facing.toYRot());
        passenger.setYBodyRot(facing.toYRot());
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        if (seatPos != null) {
            Direction facing = getFacing();
            for (Direction direction : new Direction[]{facing.getOpposite(), facing.getClockWise(), facing.getCounterClockWise(), facing}) {
                BlockPos candidate = seatPos.relative(direction);
                if (level().getBlockState(candidate).getBlock() instanceof TableBlock) {
                    continue;
                }
                Vec3 dismount = DismountHelper.findSafeDismountLocation(passenger.getType(), level(), candidate, true);
                if (dismount != null) {
                    return dismount;
                }
            }
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    private Direction getFacing() {
        return Direction.from2DDataValue(this.entityData.get(DATA_FACING));
    }

    private void refreshPosition() {
        if (seatPos == null) {
            return;
        }
        BlockState state = level().getBlockState(seatPos);
        if (state.getBlock() instanceof Seat seat) {
            Vec3 offset = seat.getSeatOffset(level(), seatPos, state);
            setPos(seatPos.getX() + offset.x, seatPos.getY() + offset.y, seatPos.getZ() + offset.z);
        }
    }

    public static boolean isOccupied(Level level, BlockPos pos) {
        return !level.getEntitiesOfClass(SeatEntity.class, new AABB(pos).inflate(0.5), seat -> pos.equals(seat.seatPos)).isEmpty();
    }

    public static void eject(Level level, BlockPos pos) {
        for (SeatEntity seat : level.getEntitiesOfClass(SeatEntity.class, new AABB(pos).inflate(0.5), entity -> pos.equals(entity.seatPos))) {
            seat.ejectPassengers();
            seat.discard();
        }
    }

    public static boolean trySit(Seat seat, BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return true;
        }
        if (!seat.canSit(state, level, pos)) {
            return false;
        }
        if (isOccupied(level, pos)) {
            eject(level, pos);
            return true;
        }
        LivingEntity leashed = findLeashedToSit(level, player);
        return sitOnSeat(level, pos, leashed != null ? leashed : player, seat);
    }

    public static boolean sitOnSeat(Level level, BlockPos pos, LivingEntity entity, Seat seatBlock) {
        if (level.isClientSide) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof Seat) || isOccupied(level, pos)) {
            return false;
        }

        SeatEntity seat = new SeatEntity(level, pos, seatBlock.getSeatFacing(state));
        level.addFreshEntity(seat);

        Vec3 seatPosition = seat.position();
        entity.setPos(seatPosition.x, seatPosition.y, seatPosition.z);
        entity.xo = seatPosition.x;
        entity.yo = seatPosition.y;
        entity.zo = seatPosition.z;
        entity.startRiding(seat);
        return true;
    }

    private static LivingEntity findLeashedToSit(Level level, Player player) {
        for (Entity entity : level.getEntities(player, player.getBoundingBox().inflate(10.0), candidate -> candidate instanceof Leashable leashable && leashable.getLeashHolder() == player)) {
            if (entity instanceof LivingEntity living && entity.getBbWidth() < 1.0 && entity.getBbHeight() < 2.0) {
                return living;
            }
        }
        return null;
    }
}
