package com.scir4y.zeppelinmurdermod.registry;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.registry.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;
import com.scir4y.zeppelinmurdermod.registry.ModItems;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ZeppelinMurderMod.MODID);

    public static final Supplier<CreativeModeTab> ZEPPELIN_MURDER_TAB = CREATIVE_MODE_TAB.register("zeppelin_murder_tab",
            ()-> CreativeModeTab.builder().icon(()-> new ItemStack(ModItems.KNIFE.get()))
                    .title(Component.translatable("creativetab.zeppelinmurder.zeppelin_items"))
                    .displayItems((itemDisplayParameters, output) -> {

                        //All items add to here
                        output.accept(ModItems.KNIFE);
                        output.accept(ModItems.NOTE);
                        output.accept(ModItems.WRITTEN_NOTE);
                        output.accept(ModItems.ELEVATOR_GLUE);
                        output.accept(ModItems.ELEVATOR_FLOOR_POINT);
                        output.accept(ModItems.ELEVATOR_CALL_LINKER);
                        output.accept(ModBlocks.POLISHED_BRASS_BLOCK);
                        output.accept(ModBlocks.ELEVATOR_CONTROLLER_BLOCK);
                        output.accept(ModBlocks.ELEVATOR_CALLER_BLOCK);
                        output.accept(ModBlocks.BRASS_CHAIN);
                        output.accept(ModBlocks.SCONE_BLOCK);

                    }).build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
