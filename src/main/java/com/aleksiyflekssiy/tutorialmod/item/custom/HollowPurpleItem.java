package com.aleksiyflekssiy.tutorialmod.item.custom;

import com.aleksiyflekssiy.tutorialmod.capability.CursedEnergyCapability;
import com.aleksiyflekssiy.tutorialmod.entity.HollowPurpleEntity;
import com.aleksiyflekssiy.tutorialmod.entity.ModEntities;
import com.aleksiyflekssiy.tutorialmod.entity.animation.AnimationBlueEntity;
import com.aleksiyflekssiy.tutorialmod.entity.animation.AnimationRedEntity;
import com.aleksiyflekssiy.tutorialmod.sound.ModSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import static com.aleksiyflekssiy.tutorialmod.util.RotationUtil.getOffsetLookPosition;

public class HollowPurpleItem extends Item {
    private int currentAnimationTick = 0;
    private AnimationBlueEntity blueEntity;
    private AnimationRedEntity redEntity;
    private HollowPurpleEntity hollowPurpleEntity;
    private static final int USE_DURATION = 72000;

    public HollowPurpleItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            if (!player.getCooldowns().isOnCooldown(this)) {
                player.startUsingItem(hand);
                player.sendSystemMessage(Component.literal("Starting using"));
            }
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int pRemainingUseDuration) {
        if (!level.isClientSide() && entity instanceof Player player) {
                if (currentAnimationTick == 30) {
                    if (!CursedEnergyCapability.isEnoughEnergy(player, 10)) return;
                    blueEntity = new AnimationBlueEntity(ModEntities.ANIMATION_BLUE_ENTITY.get(), level, player);
                    createBlue(level, player, blueEntity);
                    CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 10);
                }
                if (currentAnimationTick == 60) {
                    if (!CursedEnergyCapability.isEnoughEnergy(player, 20)) return;
                    redEntity = new AnimationRedEntity(ModEntities.ANIMATION_RED_ENTITY.get(), level, player);
                    createRed(level, player, redEntity);
                    CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 20);
                }
                if (currentAnimationTick == 120){
                    blueEntity.setFusion();
                    redEntity.setFusion();
                }
                if (currentAnimationTick == 125){
                    hollowPurpleEntity = new HollowPurpleEntity(ModEntities.HOLLOW_PURPLE_ENTITY.get(), level, player, 5, 2);
                    createHollowPurple(level, player, hollowPurpleEntity);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundEvents.HOLLOW_PURPLE_MERGE.get(), SoundSource.NEUTRAL, 1 ,1);
                    blueEntity.discard();
                    redEntity.discard();
                }
                if (currentAnimationTick == 250){
                    if (!CursedEnergyCapability.isEnoughEnergy(player, 10)) return;
                    hollowPurpleEntity.chant();
                    player.sendSystemMessage(Component.literal("Nine Ropes"));
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundEvents.CHANT_1.get(), SoundSource.NEUTRAL, 1 ,1);
                    CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 10);
                }
            if (currentAnimationTick == 300){
                if (!CursedEnergyCapability.isEnoughEnergy(player, 10)) return;
                hollowPurpleEntity.chant();
                player.sendSystemMessage(Component.literal("Polarized Light"));
                level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundEvents.CHANT_2.get(), SoundSource.NEUTRAL, 1 ,1);
                CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 10);
            }
            if (currentAnimationTick == 350){
                if (!CursedEnergyCapability.isEnoughEnergy(player, 10)) return;
                hollowPurpleEntity.chant();
                player.sendSystemMessage(Component.literal("Crow and Declaration"));
                level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundEvents.CHANT_2.get(), SoundSource.NEUTRAL, 1 ,0.85F);
                CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 10);
            }
            if (currentAnimationTick == 400){
                if (!CursedEnergyCapability.isEnoughEnergy(player, 10)) return;
                hollowPurpleEntity.chant();
                player.sendSystemMessage(Component.literal("Between Front and Back"));
                level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundEvents.CHANT_3.get(), SoundSource.NEUTRAL, 1 ,1);
                CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 10);
            }
                if (currentAnimationTick <= 400) {
                    currentAnimationTick++;
                }
        }
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return USE_DURATION;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int pTimeCharged) {
        if (level.isClientSide()) return;
        currentAnimationTick = 0;
        if (blueEntity != null) blueEntity.discard();
        if (redEntity != null) redEntity.discard();
        if (entity instanceof Player player) {
            player.getCooldowns().addCooldown(this, 300);
            if (hollowPurpleEntity != null) {
                hollowPurpleEntity.launch(player.getLookAngle().normalize());
                level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundEvents.HOLLOW_PURPLE_LAUNCH.get(), SoundSource.NEUTRAL, 1, 1);
            }
        }
    }

    private void createBlue(Level level, Player player, AnimationBlueEntity entity) {
        Vec3 eyePos = player.getEyePosition(1f);
        Vec3 lookVec = player.getLookAngle();
        Vec3 maxReach = eyePos.add(lookVec.scale(2f));

        Vec3 targetPos = getOffsetLookPosition(player, maxReach, -1.5, 0, 0);
        entity.setPos(targetPos);
        level.addFreshEntity(entity);
    }

    private void createRed(Level level, Player player, AnimationRedEntity entity) {
        Vec3 eyePos = player.getEyePosition(1f);
        Vec3 lookVec = player.getLookAngle();
        Vec3 maxReach = eyePos.add(lookVec.scale(2f));

        Vec3 targetPos = getOffsetLookPosition(player, maxReach, 1.5, 0, 0);
        entity.setPos(targetPos);
        level.addFreshEntity(entity);
    }

    private void createHollowPurple(Level level, Player player, HollowPurpleEntity entity) {
        Vec3 eyePos = player.getEyePosition(1f);
        Vec3 lookVec = player.getLookAngle();
        Vec3 maxReach = eyePos.add(lookVec.scale(2.5f));

        Vec3 targetPos = getOffsetLookPosition(player, maxReach, 0, 0, 0);
        entity.setPos(targetPos);
        level.addFreshEntity(entity);
    }
}
