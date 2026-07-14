package com.scir4y.zeppelinmurdermod.item;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.block.MODBLOCKS;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ZeppelinMurderMod.MODID);

    public static final Supplier<CreativeModeTab> ZEPPELIN_MURDER_TAB = CREATIVE_MODE_TAB.register("zeppelin_murder_tab",
            ()-> CreativeModeTab.builder().icon(()-> new ItemStack(MODITEMS.KNIFE.get()))
                    .title(Component.translatable("creativetab.zeppelinmurder.zeppelin_items"))
                    .displayItems((itemDisplayParameters, output) -> {

                        //All items add to here
                        output.accept(MODITEMS.KNIFE);
                        output.accept(MODBLOCKS.POLISHED_BRASS_BLOCK);

                    }).build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
