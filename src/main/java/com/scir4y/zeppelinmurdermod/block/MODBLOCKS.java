package com.scir4y.zeppelinmurdermod.block;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.item.MODITEMS;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MODBLOCKS {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ZeppelinMurderMod.MODID);

    //Registering blocks
    public static final DeferredBlock<Block> POLISHED_BRASS_BLOCK = registerBlock("polished_brass_block",
            ()-> new Block(BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.COPPER)
            ));

    //Registering Blocks (helper method)
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }
    //Registering BlockItems (helper method)
    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        MODITEMS.ITEMS.register(name, ()-> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
