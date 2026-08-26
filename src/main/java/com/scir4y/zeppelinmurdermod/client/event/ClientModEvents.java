package com.scir4y.zeppelinmurdermod.client.event;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.registry.ModEntities;
import com.scir4y.zeppelinmurdermod.client.renderer.elevator.MovingElevatorRenderer;
import com.scir4y.zeppelinmurdermod.client.renderer.note.NoteEntityRenderer;
import com.scir4y.zeppelinmurdermod.content.footstool.entity.FootstoolEntity;
import com.scir4y.zeppelinmurdermod.network.payload.SaveNotePayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import com.scir4y.zeppelinmurdermod.client.renderer.elevator.MovingElevatorRenderer;
import com.scir4y.zeppelinmurdermod.client.renderer.note.NoteEntityRenderer;

@EventBusSubscriber(modid = ZeppelinMurderMod.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    // rendering registration event on client
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MOVING_ELEVATOR.get(), MovingElevatorRenderer::new);
        event.registerEntityRenderer(ModEntities.NOTE.get(), NoteEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.FOOTSTOOL_SEAT.get(), FootstoolEntity.Render::new);
    }
}