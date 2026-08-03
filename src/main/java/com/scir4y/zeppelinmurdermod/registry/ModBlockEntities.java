package com.scir4y.zeppelinmurdermod.registry;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.registry.ModBlocks;
import com.scir4y.zeppelinmurdermod.content.elevator.block.entity.ElevatorCallerBlockEntity;
import com.scir4y.zeppelinmurdermod.content.elevator.block.entity.ElevatorControllerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ZeppelinMurderMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElevatorControllerBlockEntity>> ELEVATOR_CONTROLLER_BE =
            BLOCK_ENTITIES.register("elevator_controller_be", () ->
                    BlockEntityType.Builder.of(ElevatorControllerBlockEntity::new,
                            ModBlocks.ELEVATOR_CONTROLLER_BLOCK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElevatorCallerBlockEntity>> ELEVATOR_CALLER_BE =
            BLOCK_ENTITIES.register("elevator_caller_be", () ->
                    BlockEntityType.Builder.of(ElevatorCallerBlockEntity::new,
                            ModBlocks.ELEVATOR_CALLER_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
