package com.scir4y.zeppelinmurdermod.item;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MODITEMS {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ZeppelinMurderMod.MODID);

    //Registering items
    public static final DeferredItem<Item> KNIFE = ITEMS.register("knife",
            ()-> new Item(new Item.Properties()));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
