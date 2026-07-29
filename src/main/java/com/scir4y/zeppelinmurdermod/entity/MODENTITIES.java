package com.scir4y.zeppelinmurdermod.entity;

import com.scir4y.zeppelinmurdermod.ZeppelinMurderMod;
import com.scir4y.zeppelinmurdermod.entity.custom.MovingElevatorEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MODENTITIES {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = 
            DeferredRegister.create(Registries.ENTITY_TYPE, ZeppelinMurderMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<MovingElevatorEntity>> MOVING_ELEVATOR =
            ENTITY_TYPES.register("moving_elevator", () -> EntityType.Builder.<MovingElevatorEntity>of(MovingElevatorEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f)
                    .build("moving_elevator"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
