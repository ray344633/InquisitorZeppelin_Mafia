package com.scir4y.zeppelinmurdermod.content.note.entity;

import com.scir4y.zeppelinmurdermod.client.gui.NoteViewScreen;
import com.scir4y.zeppelinmurdermod.registry.ModDataComponents;
import com.scir4y.zeppelinmurdermod.content.note.component.NoteContent;
import com.scir4y.zeppelinmurdermod.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class NoteEntity extends Entity {

    private static final EntityDataAccessor<CompoundTag> DATA_NOTE_INFO =
            SynchedEntityData.defineId(NoteEntity.class, EntityDataSerializers.COMPOUND_TAG);

    private static final float NOTE_SIZE = 0.7f;
    private static final float NOTE_THICKNESS = 0.03f;

    private Direction facing = Direction.NORTH;
    private float rollYaw = 0f;
    private String text = "";

    public NoteEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true; // remove collision interaction
        this.setNoGravity(true);
    }

    public void setFacing(Direction facing) {
        setFacing(facing, 0f);
    }

    public void setFacing(Direction facing, float rollYaw) {
        this.facing = facing;
        this.rollYaw = rollYaw;

        // if facing is horizontal set rotation (yaw)
        if (facing.getAxis().isHorizontal()) {
            this.setXRot(0f);
            this.setYRot((float) (facing.get2DDataValue() * 90));
        } else {
            this.setXRot(-90f * facing.getAxisDirection().getStep());
            this.setYRot(0f);
        }

        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();

        // sync unless is client side
        if (!this.level().isClientSide()) {
            syncNoteTag();
        }
        this.setBoundingBox(makeBoundingBox());
    }

    public void setNoteContent(String text) {
        this.text = text == null ? "" : text;
        if (!this.level().isClientSide()) {
            syncNoteTag();
        }
    }

    public Direction getFacing() {
        return facing;
    }

    public float getRollYaw() {
        return rollYaw;
    }

    public String getNoteText() {
        return text;
    }

    private void syncNoteTag() {
        CompoundTag tag = new CompoundTag(); // create nbt component(i mean tags)
        tag.putInt("Facing", this.facing.get3DDataValue());
        tag.putFloat("Roll", this.rollYaw);
        tag.putString("Text", this.text);
        this.entityData.set(DATA_NOTE_INFO, tag); // attach this component to entity(i mean tags)
    }

    private void applyNoteTag(CompoundTag tag) {
        if (tag.contains("Facing")) {
            Direction newFacing = Direction.from3DDataValue(tag.getInt("Facing"));
            float newRoll = tag.getFloat("Roll");
            this.facing = newFacing;
            this.rollYaw = newRoll;

            if (newFacing.getAxis().isHorizontal()) {
                this.setXRot(0f);
                this.setYRot((float) (newFacing.get2DDataValue() * 90));
            } else {
                this.setXRot(-90f * newFacing.getAxisDirection().getStep());
                this.setYRot(0f);
            }
            this.xRotO = this.getXRot();
            this.yRotO = this.getYRot();
        }
        this.text = tag.getString("Text");
        this.setBoundingBox(makeBoundingBox());
    }

    @Override
    protected AABB makeBoundingBox() {
        if (this.facing == null) {
            return super.makeBoundingBox();
        }

        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        double half = NOTE_SIZE / 2.0;
        double thick = NOTE_THICKNESS;

        return switch (facing.getAxis()) {
            case X -> new AABB(x - thick, y - half, z - half, x + thick, y + half, z + half);
            case Z -> new AABB(x - half, y - half, z - thick, x + half, y + half, z + thick);
            case Y -> new AABB(x - half, y - thick, z - half, x + half, y + thick, z + half);
        };
    }

    @Override
    public Vec3 getLightProbePosition(float partialTick) {
        Vec3 base = super.getLightProbePosition(partialTick);
        if (this.facing == null) {
            return base;
        }
        Vec3 normal = Vec3.atLowerCornerOf(this.facing.getNormal());
        return base.add(normal.scale(0.1));
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        // if player interacts with entity with shift
        if (player.isSecondaryUseActive()) {
            // pickUp unless it is client side
            if (!this.level().isClientSide()) {
                pickUp(player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // if it is client side opens NoteViewScreen
        if (this.level().isClientSide()) {
            List<Component> pages = List.of(Component.literal(this.text));
            Minecraft.getInstance().setScreen(new NoteViewScreen(new NoteViewScreen.BookAccess(pages)));
        }

        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    @Override
    public boolean skipAttackInteraction(Entity attacker) {
        if (attacker instanceof Player player) {
            if (!this.level().isClientSide()) {
                dropAsItem(player);
            }
            return true;
        }
        return false;
    }

    private ItemStack buildNoteStack() {
        ItemStack stack = new ItemStack(ModItems.WRITTEN_NOTE.get());
        stack.set(ModDataComponents.NOTE_CONTENT.get(), new NoteContent(this.text));
        return stack;
    }

    private void dropAsItem(Player player) {
        if (!player.getAbilities().instabuild) {
            ItemStack drop = buildNoteStack();
            this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), drop));
        }

        this.level().playSound(null, this.blockPosition(), SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.PLAYERS, 1.0f, 1.0f);
        this.discard();
    }

    private void pickUp(Player player) {
        if (!player.getAbilities().instabuild) {
            ItemStack stack = buildNoteStack();
            if (!player.getInventory().add(stack)) {
                this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), stack));
            }
        }

        this.level().playSound(null, this.blockPosition(), SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.PLAYERS, 1.0f, 1.0f);
        this.discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_NOTE_INFO, new CompoundTag());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_NOTE_INFO.equals(key) && this.level().isClientSide()) {
            applyNoteTag(this.entityData.get(DATA_NOTE_INFO));
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        Direction loadedFacing = tag.contains("Facing")
                ? Direction.from3DDataValue(tag.getInt("Facing"))
                : this.facing;
        float loadedRoll = tag.getFloat("Roll");
        this.text = tag.getString("Text");
        this.setFacing(loadedFacing, loadedRoll);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Facing", this.facing.get3DDataValue());
        tag.putFloat("Roll", this.rollYaw);
        tag.putString("Text", this.text);
    }
}