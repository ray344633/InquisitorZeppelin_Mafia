package com.scir4y.zeppelinmurdermod.event;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.entity.MODENTITIES;
import com.scir4y.zeppelinmurdermod.entity.client.MovingElevatorRenderer;
import com.scir4y.zeppelinmurdermod.entity.client.NoteEntityRenderer;
import com.scir4y.zeppelinmurdermod.network.SaveNotePayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ZeppelinMurderMod.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MODENTITIES.MOVING_ELEVATOR.get(), MovingElevatorRenderer::new);
        event.registerEntityRenderer(MODENTITIES.NOTE.get(), NoteEntityRenderer::new);
    }
}