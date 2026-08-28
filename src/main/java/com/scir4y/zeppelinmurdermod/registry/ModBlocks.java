package com.scir4y.zeppelinmurdermod.registry;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.content.elevator.block.ElevatorCallerBlock;
import com.scir4y.zeppelinmurdermod.content.elevator.block.ElevatorControllerBlock;
import com.scir4y.zeppelinmurdermod.content.servicestairs.ServiceStairs;
import com.scir4y.zeppelinmurdermod.registry.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ZeppelinMurderMod.MODID);

    //Registering blocks
        //  Polished brass block
    public static final DeferredBlock<Block> POLISHED_BRASS_BLOCK = registerBlock("polished_brass_block",
            ()-> new Block(BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.COPPER)
            ));
        //  Elevator controller block
    public static final DeferredBlock<Block> ELEVATOR_CONTROLLER_BLOCK = registerBlock("elevator_controller_block",
            ()-> new ElevatorControllerBlock(BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
            ));
        //  Brass chain
    public static final DeferredBlock<ChainBlock> BRASS_CHAIN = registerBlock("brass_chain",
                ()-> new ChainBlock(BlockBehaviour.Properties.of()
                        .strength(1f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.COPPER)
                        .noOcclusion()
                ));
        //  Elevator caller block (call button, placed on a floor)
    public static final DeferredBlock<ElevatorCallerBlock> ELEVATOR_CALLER_BLOCK = registerBlock("elevator_caller_block",
            ()-> new ElevatorCallerBlock(BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
            ));
        // ServiceStairs
    public static final DeferredBlock<ServiceStairs> SERVICE_STAIRS = registerBlock(
            "service_stairs",
            () -> new ServiceStairs(BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion()
            )
    );
        // CraftBot                                         --- DELETE BEFORE PUBLISHING!!! ---


    //Registering Blocks (helper method)
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }
    //Registering BlockItems (helper method)
    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, ()-> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
