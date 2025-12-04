package com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.limitless;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.capability.CursedEnergyCapability;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.DomainExpansionSkill;
import com.aleksiyflekssiy.tutorialmod.event.SkillEvent;
import com.aleksiyflekssiy.tutorialmod.item.custom.*;
import com.aleksiyflekssiy.tutorialmod.network.InputLockPacket;
import com.aleksiyflekssiy.tutorialmod.network.ModMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UnlimitedVoid extends DomainExpansionSkill {

    public UnlimitedVoid(){

    }

    @Override
    protected void setupActivation(Player player) {
        for (LivingEntity entity : affectedEntities) {
            if (entity != player && canAffect(entity)) {
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, DOMAIN_DURATION, 1, false, false));
                if (entity instanceof Mob mob) {
                    mob.setNoAi(true);
                }
                else if (entity instanceof Player player1){
                    ModMessages.INSTANCE.send(PacketDistributor.PLAYER.with(() ->  (ServerPlayer) player1), new InputLockPacket(true, player.getYRot(), player.getXRot()));
                }
            }
        }
    }

    @Override
    protected void setupDeactivation(Player player) {
        for (LivingEntity trapped : affectedEntities) {
            if (trapped instanceof Player affectedPlayer) {
                affectedPlayer.getAbilities().mayBuild = true;
                affectedPlayer.getAbilities().mayfly = affectedPlayer.getAbilities().instabuild;
                ModMessages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) affectedPlayer), new InputLockPacket(false, 0, 0));
            }
        }
    }

    @Override
    protected void applySureHitEffect() {
        Level level = domainOwner.level();
        //Вторгающийся не имеет domainArea.
        if (domainArea == null) return;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, domainArea)) {
            if (entity == domainOwner || !canAffect(entity)) {
                if (entity instanceof Player player && affectedEntities.contains(player)) {
                    System.out.println(player.getDisplayName().getString() + " is either an owner or can't be affected");
                    ModMessages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new InputLockPacket(false, 0, 0));
                }
                affectedEntities.remove(entity);
                continue;
            }
            if (entity instanceof Player player && !affectedEntities.contains(player)) {
                System.out.println(player.getDisplayName().getString() + " is affected");
                ModMessages.INSTANCE.send(PacketDistributor.PLAYER.with(() ->  (ServerPlayer) player), new InputLockPacket(true, player.getYRot(), player.getXRot()));
            }
            affectedEntities.add(entity);
        }
        for (LivingEntity entity : affectedEntities){
            if (entity instanceof Mob mob) {
                mob.setNoAi(true);
            } else if (entity instanceof Player player) {
                player.getAbilities().mayBuild = false;
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.setSprinting(false);
            }
            SkillEvent.Hit hitEvent = new SkillEvent.Hit(domainOwner, this, entity);
            MinecraftForge.EVENT_BUS.post(hitEvent);
        }
    }

    private void spawnBarrierParticles(ServerLevel level, Vec3 center, double radius) {
        for (int i = 0; i < 30; i++) {
            double theta = 2 * Math.PI * new Random().nextDouble();
            double phi = Math.PI * new Random().nextDouble();
            double xOffset = radius * Math.sin(phi) * Math.cos(theta);
            double yOffset = radius * Math.sin(phi) * Math.sin(theta);
            double zOffset = radius * Math.cos(phi);
            level.sendParticles(
                    ParticleTypes.ENCHANT,
                    center.x + xOffset, center.y + yOffset, center.z + zOffset,
                    1, 0.1, 0.1, 0.1, 0.05
            );
        }
    }

    private void spawnParticles(ServerLevel level, Vec3 center, double radius) {
        for (int i = 0; i < 10; i++) {
            double theta = 2 * Math.PI * new Random().nextDouble();
            double xOffset = radius * Math.cos(theta);
            double zOffset = radius * Math.sin(theta);
            level.sendParticles(
                    ParticleTypes.GLOW,
                    center.x + xOffset, center.y + 0.5, center.z + zOffset,
                    1, 0.1, 0.1, 0.1, 0.05
            );
        }
    }

    @Mod.EventBusSubscriber(modid = "tutorialmod", bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class PlayerDisabler {

        public static boolean isScreenBanned(Screen screen) {
            return screen instanceof AbstractContainerScreen<?> ||
                    screen instanceof ChatScreen;
        }

        @SubscribeEvent
        public static void immobilize(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) return;

            if (player.getPersistentData().contains("lock")){
                if (player.getPersistentData().getBoolean("lock")) {
                    mc.options.keyUp.setDown(false);
                    mc.options.keyDown.setDown(false);
                    mc.options.keyLeft.setDown(false);
                    mc.options.keyRight.setDown(false);
                    mc.options.keyJump.setDown(false);
                    mc.options.keySprint.setDown(false);
                    mc.options.keyShift.setDown(false);

                    mc.options.keyAttack.setDown(false);
                    mc.options.keyUse.setDown(false);
                    mc.options.keyPickItem.setDown(false);
                    mc.options.keyDrop.setDown(false);
                    mc.options.keySwapOffhand.setDown(false);

                    mc.options.keyChat.setDown(false);
                    mc.options.keyCommand.setDown(false);

                    Arrays.stream(mc.options.keyHotbarSlots).toList().forEach(keyMapping -> keyMapping.setDown(false));

                    mc.options.keyPlayerList.setDown(false);
                    mc.options.keyTogglePerspective.setDown(false);

                    player.setYRot(player.getPersistentData().getFloat("yaw"));
                    player.setXRot(player.getPersistentData().getFloat("pitch"));

                    mc.options.keyInventory.setDown(false);
                    if (isScreenBanned(mc.screen)) mc.screen = null;
                    System.out.println("SHOULD WORK");
                }
            }
        }

        @SubscribeEvent
        public static void lockScreenOpening(ScreenEvent.Opening event) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) return;
            if (player.getPersistentData().contains("lock")){
                if (player.getPersistentData().getBoolean("lock")) {
                    if (isScreenBanned(event.getNewScreen())) event.setCanceled(true);
                }
            }
        }

        @SubscribeEvent
        public static void lockMouseButtonInput(InputEvent.MouseButton.Pre event) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) return;
            if (player.getPersistentData().contains("lock")){
                if (player.getPersistentData().getBoolean("lock")) {
                    if (isScreenBanned(mc.screen)) event.setCanceled(true);
                }
            }
        }

    }

    @Override
    public String getName() {
        return "Unlimited Void";
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/gui/unlimited_void.png");
    }
}
