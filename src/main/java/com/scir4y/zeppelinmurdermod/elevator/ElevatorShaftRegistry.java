package com.scir4y.zeppelinmurdermod.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Реестр "где сейчас физически находится контроллер шахты X".
 *
 * ShaftId — стабильный идентификатор шахты, который генерируется один раз при
 * создании контроллера и не меняется никогда (переживает разборку/сборку
 * блока при поездке лифта, см. captureRideData/restoreAfterRide в
 * ElevatorControllerBlockEntity). BlockPos, наоборот, меняется каждый раз,
 * когда лифт приезжает на другой этаж — старый блок разбирается, на новом
 * месте появляется НОВЫЙ BlockEntity.
 *
 * Кнопки вызова (ElevatorCallerBlockEntity) хранят ShaftId, а не BlockPos, и
 * находят актуальное положение контроллера здесь.
 *
 * Реестр не сохраняется на диск — он не обязан, поскольку самовосстанавливается
 * по мере подгрузки чанков: каждый ElevatorControllerBlockEntity регистрирует
 * себя в onLoad() и обновляет запись сразу после restoreAfterRide(). Существует
 * только на сервере (у клиента и так нет доступа к нужным данным для линковки).
 */
public final class ElevatorShaftRegistry {

    private static final Map<ServerLevel, Map<UUID, BlockPos>> REGISTRY = new WeakHashMap<>();

    private ElevatorShaftRegistry() {
    }

    /** Регистрирует/обновляет текущее положение контроллера данной шахты. */
    public static void register(ServerLevel level, UUID shaftId, BlockPos controllerPos) {
        REGISTRY.computeIfAbsent(level, l -> new HashMap<>()).put(shaftId, controllerPos.immutable());
    }

    /** Убирает запись (например, контроллер сломан игроком, а не уехал). */
    public static void unregister(ServerLevel level, UUID shaftId) {
        Map<UUID, BlockPos> map = REGISTRY.get(level);
        if (map != null) {
            map.remove(shaftId);
        }
    }

    /** Возвращает текущий BlockPos контроллера шахты, либо null, если неизвестен. */
    public static BlockPos get(ServerLevel level, UUID shaftId) {
        Map<UUID, BlockPos> map = REGISTRY.get(level);
        return map == null ? null : map.get(shaftId);
    }
}
