package com.aleksiyflekssiy.tutorialmod.item.custom;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.capability.CursedEnergy;
import com.aleksiyflekssiy.tutorialmod.capability.CursedEnergyCapability;
import com.aleksiyflekssiy.tutorialmod.capability.ICursedEnergy;
import com.aleksiyflekssiy.tutorialmod.entity.ModEntities;
import com.aleksiyflekssiy.tutorialmod.entity.RedEntity;
import com.aleksiyflekssiy.tutorialmod.network.CursedEnergySyncPacket;
import com.aleksiyflekssiy.tutorialmod.sound.ModSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;

public class RedItem extends Item {
    private static final int USE_DURATION = 2000;
    private static final int LIFETIME = 80;
    private static final float EXPLOSION_POWER = 2.5F;
    private static final float SPEED = 2.5F;

    public RedItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            if (!player.getCooldowns().isOnCooldown(this)) {
                    player.startUsingItem(hand);
                    stack.getOrCreateTag().putInt("Phase", 0);
            }
            return InteractionResultHolder.consume(stack);
        }
        else return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity instanceof Player player){
            if (!(player.level() instanceof ServerLevel)) return false;
            ItemStack holdItem = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!(holdItem.getItem() instanceof RedItem)) return false;
            if (player.getCooldowns().isOnCooldown(this)) return false;
            repelEntities(player);
            player.getCooldowns().addCooldown(this, 40);
            return true;
        }
        return false;
    }

    private void repelEntities(Player player) {
        if (!CursedEnergyCapability.isEnoughEnergy(player, 15)) return;
        RedEntity entity = new RedEntity(ModEntities.RED_ENTITY.get(), player.level(), player, 60, 5, 5f, RedEntity.ATTACK_TYPE.MELEE, 0);
        createRed(player.level(), player, entity);
        CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 15);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int pRemainingUseDuration) {
        if (livingEntity instanceof Player player && level instanceof ServerLevel serverLevel) {
            int holdTicks = USE_DURATION - pRemainingUseDuration; // Сколько тиков прошло
            int currentPhase = stack.getOrCreateTag().getInt("Phase");
            System.out.println(holdTicks + " " + currentPhase);
            // Проверка достижения фаз
            if (holdTicks == 100 && currentPhase == 2) {
                if (!CursedEnergyCapability.isEnoughEnergy(player, 10)) return;
                player.sendSystemMessage(Component.literal("Pillar of Light"));
                stack.getOrCreateTag().putInt("Phase", 3);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundEvents.CHANT_3.get(), SoundSource.NEUTRAL, 1f, 1f);
                CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 10);
            } else if (holdTicks == 75 && currentPhase == 1) {
                if (!CursedEnergyCapability.isEnoughEnergy(player, 10)) return;
                player.sendSystemMessage(Component.literal("Paramita"));
                stack.getOrCreateTag().putInt("Phase", 2);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundEvents.CHANT_2.get(), SoundSource.NEUTRAL, 1f, 1f);
                CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 10);
            } else if (holdTicks == 50 && currentPhase == 0) {
                if (!CursedEnergyCapability.isEnoughEnergy(player, 10)) return;
                player.sendSystemMessage(Component.literal("Phase"));
                stack.getOrCreateTag().putInt("Phase", 1);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundEvents.CHANT_1.get(), SoundSource.NEUTRAL, 1f, 1f);
                CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 10);
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int pTimeCharged) {
        if (!level.isClientSide()){
            int phase = stack.getOrCreateTag().getInt("Phase");
            if (entity instanceof Player player){
                if (!CursedEnergyCapability.isEnoughEnergy(player, 10)) return;
                RedEntity orb = new RedEntity(ModEntities.RED_ENTITY.get(), level, player, LIFETIME, SPEED, EXPLOSION_POWER, RedEntity.ATTACK_TYPE.RANGED, phase);
                createRed(level, player, orb);
                player.getCooldowns().addCooldown(this,  phase > 0 ? 60 * (phase + 1) : 60);
                stack.getOrCreateTag().putInt("Phase", 0);
                CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 10);
            }
        }
    }

    private void createRed(Level level, Player player, RedEntity entity) {
        Vec3 eyePos = player.getEyePosition(1f);
        Vec3 lookVec = player.getLookAngle();
        Vec3 maxReach = eyePos.add(lookVec.scale(1f));
        ClipContext clipContext = new ClipContext(eyePos, maxReach, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
        BlockHitResult hitResult = level.clip(clipContext);

        Vec3 targetPos = hitResult.getType() == HitResult.Type.BLOCK ?
                hitResult.getLocation() :
                eyePos.add(lookVec.scale(1f));
        entity.setPos(targetPos);
        level.addFreshEntity(entity);
    }
}
