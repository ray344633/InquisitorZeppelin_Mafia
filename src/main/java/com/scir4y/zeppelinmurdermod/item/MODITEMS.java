package com.scir4y.zeppelinmurdermod.item;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.item.custom.KnifeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.minecraft.world.item.Tiers.IRON;

public class MODITEMS {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ZeppelinMurderMod.MODID);

    //Registering items
        //  Knife
    public static final DeferredItem<KnifeItem> KNIFE = ITEMS.register("knife",
            ()-> new KnifeItem(IRON, new Item.Properties().attributes(SwordItem.createAttributes(IRON, 3.5f, -2.5f))));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
