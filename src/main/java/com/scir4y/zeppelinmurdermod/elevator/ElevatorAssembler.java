package com.scir4y.zeppelinmurdermod.elevator;

import com.scir4y.zeppelinmurdermod.entity.MODENTITIES;
import com.scir4y.zeppelinmurdermod.entity.custom.MovingElevatorEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class ElevatorAssembler {
    
    public static void assembleAndSpawn(Level level, BlockPos controllerPos) {
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        Set<BlockPos> visited = new HashSet<>();
        Stack<BlockPos> stack = new Stack<>();
        
        stack.push(controllerPos);
        
        while(!stack.isEmpty() && blocks.size() < 256) {
            BlockPos current = stack.pop();
            
            if(visited.contains(current)) continue;
            visited.add(current);
            
            BlockState state = level.getBlockState(current);
            if(state.isAir() || state.is(Blocks.BEDROCK)) continue;
            
            // Limit distance from controller
            if(Math.abs(current.getX() - controllerPos.getX()) > 5 || 
               Math.abs(current.getY() - controllerPos.getY()) > 5 || 
               Math.abs(current.getZ() - controllerPos.getZ()) > 5) {
                continue;
            }
            
            blocks.put(current.subtract(controllerPos), state);
            
            for(int x = -1; x <= 1; x++) {
                for(int y = -1; y <= 1; y++) {
                    for(int z = -1; z <= 1; z++) {
                        if(Math.abs(x) + Math.abs(y) + Math.abs(z) == 1) { // 6 directions
                            stack.push(current.offset(x, y, z));
                        }
                    }
                }
            }
        }
        
        System.out.println("Elevator assembled with " + blocks.size() + " blocks.");
        
        // Remove blocks from world
        for(Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos worldPos = controllerPos.offset(entry.getKey());
            level.removeBlock(worldPos, false);
        }
        
        // Spawn Entity
        MovingElevatorEntity entity = MODENTITIES.MOVING_ELEVATOR.get().create(level);
        if(entity != null) {
            entity.setPos(controllerPos.getX() + 0.5, controllerPos.getY(), controllerPos.getZ() + 0.5);
            entity.setBlocks(blocks);
            entity.setTargetY(controllerPos.getY() + 10);
            level.addFreshEntity(entity);
            System.out.println("Elevator entity spawned!");
        }
    }
}
