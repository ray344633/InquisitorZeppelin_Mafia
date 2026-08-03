package com.scir4y.zeppelinmurdermod.content.note.item;

import com.scir4y.zeppelinmurdermod.client.gui.NoteViewScreen;
import com.scir4y.zeppelinmurdermod.registry.ModDataComponents;
import com.scir4y.zeppelinmurdermod.content.note.component.NoteContent;
import com.scir4y.zeppelinmurdermod.registry.ModEntities;
import com.scir4y.zeppelinmurdermod.content.note.entity.NoteEntity;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class WrittenNoteItem extends Item {
    public WrittenNoteItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // do not use unless a main hand
        if (context.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        // use if player uses an item with shift
        if (context.isSecondaryUseActive()) {
            Level level = context.getLevel(); // get a world(dimension) and some interaction info
            Player player = context.getPlayer(); // get a player who interacts

            // continue unless player is null
            if (player == null) {
                return InteractionResult.PASS;
            }

            ItemStack stack = context.getItemInHand(); // get an item in hand
            NoteContent content = stack.get(ModDataComponents.NOTE_CONTENT.get());

            // if content is empty return
            if (content == null) {
                return InteractionResult.FAIL;
            }

            BlockPos clickedPos = context.getClickedPos(); // get the coords of the block the player is interacting with
            BlockState clickedState = level.getBlockState(clickedPos); // get the states of the block the player is interacting with

            // return unless block has not collision
            if (clickedState.getCollisionShape(level, clickedPos).isEmpty()) {
                return InteractionResult.FAIL;
            }

            // do nothing unless it is client side
            if (!level.isClientSide()) {
                try {
                    Direction face = context.getClickedFace(); // get the face of the block the player is interacting with
                    String text = content.text();

                    Vec3 clickLoc = context.getClickLocation(); // get the coords of interaction(not block's coords)
                    Vec3 normal = Vec3.atLowerCornerOf(face.getNormal()); // get Normal(UP, DOWN, NORTH, WEST, SOUTH, EAST)
                    Vec3 spawnPos = clickLoc.add(normal.scale(0.05)); // shift note entity outwards (it does it for avoiding entity in blocks)

                    /*
                    If placed on floor or ceiling (Y-axis), snap rotation to the nearest
                    90 degrees based on player's yaw. Otherwise, set roll to 0.
                     */
                    float roll = face.getAxis() == Direction.Axis.Y
                            ? Math.round(Mth.wrapDegrees(player.getYRot()) / 90.0f) * 90.0f
                            : 0f;

                    NoteEntity note = new NoteEntity(ModEntities.NOTE.get(), level); // note entity
                    note.setPos(spawnPos.x, spawnPos.y, spawnPos.z); // set note entity pos
                    note.setFacing(face, roll); // set note entity face and rotation
                    note.setNoteContent(text); // set text
                    level.addFreshEntity(note); // spawn entity

                    // need to change sound ...
                    // play sound
                    level.playSound(null, clickedPos, SoundEvents.ITEM_FRAME_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);

                    // decrease item count by 1 unless in creative mode
                    if (!player.getAbilities().instabuild) {
                        stack.setCount(stack.getCount() - 1);
                    }
                } catch (Exception e) {
                    return InteractionResult.FAIL;
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        // hide author's signature
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // do not use unless main hand
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        // get item in hand
        ItemStack itemStack = player.getItemInHand(hand);

        // use unless player uses item with shift
        if (!player.isSecondaryUseActive()) {
            // if it is client side ...
            if (level.isClientSide()) {
                NoteViewScreen.BookAccess bookAccess = NoteViewScreen.BookAccess.fromItem(itemStack);
                if (bookAccess != null) {
                    Minecraft.getInstance().setScreen(new NoteViewScreen(bookAccess));
                }
            }
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}