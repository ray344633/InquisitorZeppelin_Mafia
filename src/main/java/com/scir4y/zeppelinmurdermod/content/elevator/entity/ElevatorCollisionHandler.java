package com.scir4y.zeppelinmurdermod.content.elevator.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ElevatorCollisionHandler {

    public static void handleCollisions(MovingElevatorEntity elevator, Vec3 motion) {
        Level level = elevator.level();
        Map<BlockPos, BlockState> blocks = elevator.getBlocks();
        if (blocks.isEmpty()) return;

        // 1. Формуємо списки локальних AABB для кожного блоку ліфта
        List<AABB> localBoxes = new ArrayList<>();
        for (BlockPos relPos : blocks.keySet()) {
            localBoxes.add(new AABB(
                    relPos.getX() - 0.5, relPos.getY(), relPos.getZ() - 0.5,
                    relPos.getX() + 0.5, relPos.getY() + 1.0, relPos.getZ() + 0.5
            ));
        }

        Vec3 position = elevator.position();
        AABB bounds = elevator.getBoundingBox();

        // 2. Зона пошуку сутностей з невеликим запасом
        AABB searchBounds = new AABB(
                bounds.minX, bounds.minY + Math.min(0, motion.y) - 0.5, bounds.minZ,
                bounds.maxX, bounds.maxY + Math.max(0, motion.y) + 0.5, bounds.maxZ
        ).inflate(1.0);

        List<? extends Entity> entities = level.getEntities((Entity) null, searchBounds, ElevatorCollisionHandler::canCollideWith);

        for (Entity entity : entities) {
            if (entity == elevator) continue;

            // Горизонтальне виштовхування (якщо ліфт врізається збоку)
            for (AABB localBox : localBoxes) {
                AABB worldBox = localBox.move(position);
                handleHorizontalCollision(entity, worldBox);
            }

            // Вертикальне утримання (підйом та спуск)
            for (AABB localBox : localBoxes) {
                AABB worldBox = localBox.move(position);
                handleVerticalCollision(entity, worldBox, motion);
            }
        }
    }

    private static void handleHorizontalCollision(Entity entity, AABB box) {
        Vec3 entityPos = entity.position();
        Vec3 oldEntityPos = new Vec3(entity.xo, entity.yo, entity.zo);
        Vec3 entityMotion = entityPos.subtract(oldEntityPos);

        AABB entityBox = entity.getBoundingBox().deflate(1E-7d);
        AABB oldEntityBox = entityBox.move(-entityMotion.x, 0, -entityMotion.z);

        if (oldEntityBox.maxY > box.minY && oldEntityBox.minY < box.maxY) {
            if (oldEntityBox.maxX > box.minX && oldEntityBox.minX < box.maxX) {
                if (oldEntityBox.maxZ < box.minZ && entityBox.maxZ > box.minZ) {
                    entity.setPos(entityPos.x, entityPos.y, box.minZ - entity.getBbWidth() / 2.0);
                    entity.setDeltaMovement(entity.getDeltaMovement().x, entity.getDeltaMovement().y, 0);
                } else if (oldEntityBox.minZ > box.maxZ && entityBox.minZ < box.maxZ) {
                    entity.setPos(entityPos.x, entityPos.y, box.maxZ + entity.getBbWidth() / 2.0);
                    entity.setDeltaMovement(entity.getDeltaMovement().x, entity.getDeltaMovement().y, 0);
                }
            } else if (oldEntityBox.maxZ > box.minZ && oldEntityBox.minZ < box.maxZ) {
                if (oldEntityBox.maxX < box.minX && entityBox.maxX > box.minX) {
                    entity.setPos(box.minX - entity.getBbWidth() / 2.0, entityPos.y, entityPos.z);
                    entity.setDeltaMovement(0, entity.getDeltaMovement().y, entity.getDeltaMovement().z);
                } else if (oldEntityBox.minX > box.maxX && entityBox.minX < box.maxX) {
                    entity.setPos(box.maxX + entity.getBbWidth() / 2.0, entityPos.y, entityPos.z);
                    entity.setDeltaMovement(0, entity.getDeltaMovement().y, entity.getDeltaMovement().z);
                }
            }
        }
    }

    private static void handleVerticalCollision(Entity entity, AABB box, Vec3 motion) {
        AABB newBox = box.move(motion);
        boolean movingUp = motion.y > 0;

        Vec3 entityPos = entity.position();
        Vec3 oldEntityPos = new Vec3(entity.xo, entity.yo, entity.zo);
        Vec3 entityMotion = entityPos.subtract(oldEntityPos);

        AABB entityBox = entity.getBoundingBox().deflate(1E-7d);
        AABB oldEntityBox = entityBox.move(-entityMotion.x, -entityMotion.y, -entityMotion.z);

        // Перевіряємо горизонтальне перекриття (чи стоїть гравець над блоком)
        if (oldEntityBox.maxX > box.minX && oldEntityBox.minX < box.maxX &&
                oldEntityBox.maxZ > box.minZ && oldEntityBox.minZ < box.maxZ) {

            // 1. Зіткнення головою з нижньою частиною блоку (при русі вгору або стрибку в ліфт)
            if (oldEntityBox.maxY <= box.minY && entityBox.maxY > newBox.minY) {
                entity.setPos(entityPos.x, newBox.minY - entity.getBbHeight(), entityPos.z);
                entity.setDeltaMovement(entity.getDeltaMovement().x, Math.min(0, entity.getDeltaMovement().y), entity.getDeltaMovement().z);
            }
            // 2. Стояння на підлозі ліфта (Підйом і Спуск)
            else {
                // Гравець знаходився вище або в межах верхньої половини блоку підлоги
                boolean wasAboveFloor = oldEntityBox.minY >= box.minY - 0.2;
                // Ноги гравця знаходяться біля нової позиції підлоги (з урахуванням руху)
                boolean isNearFloor = entityBox.minY <= Math.max(box.maxY, newBox.maxY) + 0.3;

                if (wasAboveFloor && isNearFloor) {
                    // Фіксуємо позицію ніг точно на верхній грані ліфта
                    entity.setPos(entityPos.x, newBox.maxY, entityPos.z);

                    // ПЕРЕДАЧА ІМПУЛЬСУ: даємо гравцеві вертикальну швидкість ліфта.
                    // Це компенсує гравітацію і не дає провалюватися на наступному тику.
                    double targetYMotion = movingUp ? Math.max(entity.getDeltaMovement().y, motion.y) : motion.y;
                    entity.setDeltaMovement(entity.getDeltaMovement().x, targetYMotion, entity.getDeltaMovement().z);

                    entity.setOnGround(true);
                    entity.resetFallDistance();
                }
            }
        }
    }

    private static boolean canCollideWith(Entity entity) {
        return !entity.isSpectator() && !entity.noPhysics && !entity.isPassenger() && entity.getPistonPushReaction() == PushReaction.NORMAL;
    }
}