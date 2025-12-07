package com.aleksiyflekssiy.tutorialmod.event;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SwordBlockHandler {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            Item item = player.getMainHandItem().getItem();
            boolean isHoldingSword = item instanceof SwordItem;
            boolean isBlocking = Minecraft.getInstance().options.keyUse.isDown();
            if (isHoldingSword && isBlocking) {
                player.startUsingItem(player.getUsedItemHand());
                if (!player.getCooldowns().isOnCooldown(item)) {
                    event.setCanceled(true);
                    ItemStack sword = player.getMainHandItem();
                    playBlockingSound(player);
                    player.getCooldowns().addCooldown(sword.getItem(), 50);
                    player.sendSystemMessage(Component.literal("Sword blocking"));
                    if (event.getSource().getDirectEntity() instanceof Projectile projectile){
                        Vec3 angle = player.getLookAngle().normalize();
                        projectile.setOwner(player);
                        projectile.setDeltaMovement(-angle.x * 20, -angle.y * 20, -angle.z * 20);
                        player.sendSystemMessage(Component.literal("Projectile"));
                    }
                    else player.sendSystemMessage(Component.literal("This is not projectile"));
                }
            }
        }
    }

    private static void playBlockingSound(Player player) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @SubscribeEvent
    public static void blocking(ShieldBlockEvent event) {
        Player player = (Player) event.getEntity();
        player.sendSystemMessage(Component.literal("Shield blocking"));
    }
}
