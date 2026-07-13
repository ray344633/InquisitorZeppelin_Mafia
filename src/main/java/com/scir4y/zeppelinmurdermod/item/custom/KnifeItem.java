package com.scir4y.zeppelinmurdermod.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KnifeItem extends Item {
    public KnifeItem(Tier iron, Properties properties) {
        super(properties);
    }
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    boolean CanBeUsed = false;
    boolean WasUsed = false;

    public static ItemAttributeModifiers createAttributes(Tier tier, int attackDamage, float attackSpeed) {
        return createAttributes(tier, (float) attackDamage, attackSpeed);
    }
    public static ItemAttributeModifiers createAttributes(Tier p_330371_, float p_331976_, float p_332104_) {
        return ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, p_331976_ + p_330371_.getAttackDamageBonus(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, p_332104_, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build();
    }

    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (WasUsed == false) {
            CanBeUsed = true;
        } else {
            System.out.println("Tool wasn't used");
        }
        return super.use(level, player, usedHand);
    }

    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = attacker.level();
        return true;
        }

    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (CanBeUsed == true && WasUsed == false) {
            WasUsed = true; CanBeUsed = false;

            Vec3 vector = attacker.getLookAngle();
            double Velocity = 1.5;
            Vec3 motion = new Vec3(
                    vector.x * Velocity,
                    0,
                    vector.z * Velocity
            );
            target.setDeltaMovement(motion);

            target.hurtMarked = true;
            target.hasImpulse = true;
        }
        scheduler.schedule(()-> {
            WasUsed = false;
        }, 15, TimeUnit.SECONDS);
    }

    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_SWORD_ACTIONS.contains(itemAbility);
    }
}