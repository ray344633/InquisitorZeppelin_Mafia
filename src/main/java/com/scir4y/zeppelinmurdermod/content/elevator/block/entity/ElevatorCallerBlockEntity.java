package com.scir4y.zeppelinmurdermod.content.elevator.block.entity;

import com.scir4y.zeppelinmurdermod.content.elevator.item.ElevatorCallLinkerItem;
import com.scir4y.zeppelinmurdermod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Кнопка вызова лифта на конкретный этаж.
 * Elevator call button to current floor
 * Contains the ShaftId of the associated shaft and the index of the floor it calls.
 *
 * Linking is performed by the item {@link ElevatorCallLinkerItem}.
 */

public class ElevatorCallerBlockEntity extends BlockEntity {

    @Nullable
    private UUID shaftId;
    private int floorIndex = -1;

    public ElevatorCallerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELEVATOR_CALLER_BE.get(), pos, state);
    }

    // linking
    public void link(UUID shaftId, int floorIndex) {
        this.shaftId = shaftId;
        this.floorIndex = floorIndex;
        setChanged();
    }
    // unlinking
    public void unlink() {
        this.shaftId = null;
        this.floorIndex = -1;
        setChanged();
    }

    public boolean isLinked() {
        return shaftId != null && floorIndex >= 0;
    }

    @Nullable
    public UUID getShaftId() {
        return shaftId;
    }

    public int getFloorIndex() {
        return floorIndex;
    }

    // save in tags
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.shaftId != null) {
            tag.putUUID("ShaftId", this.shaftId);
        }
        tag.putInt("FloorIndex", this.floorIndex);
    }

    // load tags
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.shaftId = tag.hasUUID("ShaftId") ? tag.getUUID("ShaftId") : null;
        this.floorIndex = tag.contains("FloorIndex") ? tag.getInt("FloorIndex") : -1;
    }
}
