package com.scir4y.zeppelinmurdermod.registry;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.content.elevator.entity.MovingElevatorEntity;
import com.scir4y.zeppelinmurdermod.content.note.entity.NoteEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = 
            DeferredRegister.create(Registries.ENTITY_TYPE, ZeppelinMurderMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<MovingElevatorEntity>> MOVING_ELEVATOR =
            ENTITY_TYPES.register("moving_elevator", () -> EntityType.Builder.<MovingElevatorEntity>of(MovingElevatorEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f)
                    .build("moving_elevator"));

    public static final DeferredHolder<EntityType<?>, EntityType<NoteEntity>> NOTE =
            ENTITY_TYPES.register("note", () -> EntityType.Builder.<NoteEntity>of(NoteEntity::new, MobCategory.MISC)
                    .sized(0.8f, 0.8f)
                    .build("note"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
