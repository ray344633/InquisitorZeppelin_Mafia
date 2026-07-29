package com.scir4y.zeppelinmurdermod.item;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.item.custom.ElevatorCallLinkerItem;
import com.scir4y.zeppelinmurdermod.item.custom.ElevatorFloorPointItem;
import com.scir4y.zeppelinmurdermod.item.custom.ElevatorGlueItem;
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

        //  Elevator Glue
    public static final DeferredItem<ElevatorGlueItem> ELEVATOR_GLUE = ITEMS.register("elevator_glue",
            ()-> new ElevatorGlueItem(new Item.Properties()));

        //  Elevator Floor Point (marks floor stop coordinates)
    public static final DeferredItem<ElevatorFloorPointItem> ELEVATOR_FLOOR_POINT = ITEMS.register("elevator_floor_point",
            ()-> new ElevatorFloorPointItem(new Item.Properties()));

        //  Elevator Call Linker (links a caller block to a controller + floor)
    public static final DeferredItem<ElevatorCallLinkerItem> ELEVATOR_CALL_LINKER = ITEMS.register("elevator_call_linker",
            ()-> new ElevatorCallLinkerItem(new Item.Properties()));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
