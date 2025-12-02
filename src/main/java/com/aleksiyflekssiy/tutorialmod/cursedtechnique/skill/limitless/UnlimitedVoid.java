package com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.limitless;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.capability.CursedEnergyCapability;
import com.aleksiyflekssiy.tutorialmod.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.LimitlessCursedTechnique;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.Skill;
import com.aleksiyflekssiy.tutorialmod.event.SkillEvent;
import com.aleksiyflekssiy.tutorialmod.item.custom.*;
import com.aleksiyflekssiy.tutorialmod.network.InputLockPacket;
import com.aleksiyflekssiy.tutorialmod.network.ModMessages;
import com.aleksiyflekssiy.tutorialmod.util.MovementUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UnlimitedVoid extends Skill {
    private static final float DOMAIN_RADIUS = 15.0F;
    private static final float BOUNDARY_THICKNESS = 1.2F;
    private static final int DOMAIN_DURATION = 600;

    private boolean domainActive = false;
    private int charge = 0;
    private int domainTicks = 0;
    private final Map<BlockPos, BlockState> originalBlocks = new HashMap<>();
    private final List<BlockPos> barrierBlocks = new ArrayList<>();
    private AABB domainArea = null;
    private Vec3 domainCenter = null;
    private final Set<LivingEntity> trappedEntities = new HashSet<>();
    private Player domainOwner = null;

    public UnlimitedVoid(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void use(LivingEntity entity, UseType type, int charge) {
        if (!(entity instanceof ServerPlayer)) return;
        switch (type){
            case ACTIVATION -> {
                if (!domainActive) this.activate((Player) entity, entity.level());
                else this.deactivate(entity);
            }
            case CHARGING -> this.charge(entity, charge);
            case RELEASING -> this.release(entity);
        }
    }

    public void activate(Player player, Level level) {
        if (level.isClientSide()) return;

        if (!domainActive) {
            if (!CursedEnergyCapability.isEnoughEnergy(player, 50)) return;
            // Центр сферы над игроком, пол под ногами
            activateDomainExpansion((ServerLevel) level, player);
            player.sendSystemMessage(Component.literal("Domain Expansion: Unlimited Void activated!"));

            CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 50);
        }
    }

    public void deactivate(LivingEntity entity) {
        if (domainActive) {
            for (LivingEntity trapped : trappedEntities) {
                trapped.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.1);
                if (trapped instanceof Player affectedPlayer) {
                    affectedPlayer.getAbilities().mayBuild = true;
                    affectedPlayer.getAbilities().mayfly = affectedPlayer.getAbilities().instabuild;
                    ModMessages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) affectedPlayer), new InputLockPacket(false, 0, 0));
                }
            }
            applyTechniqueBurnout(domainOwner);
            restoreOriginalBlocks((ServerLevel) entity.level());
        }
    }

    public void charge(LivingEntity entity, int charge){
        if (charge <= 200){
            if (charge % 100 == 0) {
                this.charge++;
                entity.sendSystemMessage(Component.literal("Charge: " + this.charge));
            }
        }
    }

    public void release(LivingEntity entity){
        if (!domainActive) activateDomainExpansion((ServerLevel) entity.level(), (Player) entity);
        this.charge = 0;
    }

    private void activateDomainExpansion(ServerLevel level, Player player) {
        float radius = DOMAIN_RADIUS + DOMAIN_RADIUS * charge;
        double floorY = player.blockPosition().getY() - 0.1; // Пол на уровне ног игрока

        Vec3 center = new Vec3(player.getX(), floorY + radius / 2.0, player.getZ());
        level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SPAWN, player.getSoundSource(), 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 1, 0, 0, 0, 0);

        domainActive = true;
        domainCenter = center;
        domainTicks = 0;
        domainOwner = player;

        BlockPos centerPos = new BlockPos((int) center.x, (int) center.y, (int) center.z);
        int radiusInt = (int) Math.ceil(radius);
        player.setPos(player.getX(), floorY + 1.1, player.getZ());

        for (int x = -radiusInt; x <= radiusInt; x++) {
            for (int y = -radiusInt; y <= radiusInt; y++) {
                for (int z = -radiusInt; z <= radiusInt; z++) {
                    BlockPos pos = centerPos.offset(x, y, z);
                    double distanceSq = center.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    double distance = Math.sqrt(distanceSq);

                    if (distance <= radius) {
                        originalBlocks.put(pos.immutable(), level.getBlockState(pos));
                        if (distance >= radius - BOUNDARY_THICKNESS && distance <= radius + BOUNDARY_THICKNESS) {
                            level.setBlock(pos, Blocks.BLACK_CONCRETE.defaultBlockState(), 3);
                            barrierBlocks.add(pos);
                        } else if (Math.abs(pos.getY() - floorY) < 1.0) {
                            level.setBlock(pos, Blocks.BLACK_CONCRETE.defaultBlockState(), 3);
                            barrierBlocks.add(pos);
                        } else {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }

        // Фиксация всех сущностей, включая игроков (кроме владельца)
        domainArea = new AABB(center.add(-radius, -radius, -radius),
                center.add(radius, radius, radius));
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, domainArea)) {
            if (entity != player && canAffect(entity)) {
                double newY = floorY + 1.0;
                entity.setPos(entity.getX(), newY, entity.getZ());
                entity.setDeltaMovement(0, 0, 0);
                trappedEntities.add(entity);
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

    private void affectEntities(Level level) {
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, domainArea)) {
            if (entity == domainOwner || !canAffect(entity)) {
                if (entity instanceof Player player && trappedEntities.contains(player)) ModMessages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new InputLockPacket(false, 0, 0));
                trappedEntities.remove(entity);
                continue;
            }
            if (entity instanceof Player player && !trappedEntities.contains(player)) {
                ModMessages.INSTANCE.send(PacketDistributor.PLAYER.with(() ->  (ServerPlayer) player), new InputLockPacket(true, player.getYRot(), player.getXRot()));
            }
            trappedEntities.add(entity);
        }
        for (LivingEntity entity : trappedEntities){
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

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
            if (domainOwner == null || event.phase == TickEvent.Phase.END) return;
            ServerLevel serverLevel = (ServerLevel) domainOwner.level();
            domainOwner.sendSystemMessage(Component.literal(String.valueOf(domainTicks)));
            if (domainActive && domainCenter != null) {
                domainTicks++;
                spawnBarrierParticles(serverLevel, domainCenter, DOMAIN_RADIUS);
                affectEntities(serverLevel);
                if (domainTicks >= DOMAIN_DURATION || checkBarrierDamage(serverLevel)) this.deactivate(domainOwner);
            }
    }

    private void restoreOriginalBlocks(ServerLevel level) {
        for (Map.Entry<BlockPos, BlockState> entry : originalBlocks.entrySet()) {
            level.setBlock(entry.getKey(), entry.getValue(), 3);
        }
        level.playSound(null, new BlockPos((int) domainCenter.x, (int) domainCenter.y, (int) domainCenter.z),
                SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 1.0F, 1.0F);
        spawnParticles(level, domainCenter, DOMAIN_RADIUS);

        for (LivingEntity trapped : trappedEntities) {
            trapped.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.1);
            if (trapped instanceof Mob mob) {
                mob.setNoAi(false);
            }
            if (trapped instanceof Player player) {
                player.getAbilities().mayBuild = true;
                player.getAbilities().mayfly = player.getAbilities().instabuild;
            }
        }
        trappedEntities.clear();
        barrierBlocks.clear();
        originalBlocks.clear();
        domainActive = false;
        domainCenter = null;
        domainOwner = null;
        domainArea = null;
    }

    private boolean checkBarrierDamage(Level level) {
        int blocks = barrierBlocks.size();
        int brokenBlocks = 0;
        for (BlockPos barrierBlock : barrierBlocks) {
            if (level.getBlockState(barrierBlock).getBlock() != Blocks.BLACK_CONCRETE) brokenBlocks++;
        }
        return ((float) brokenBlocks / blocks) >= 0.125f;
    }

    private void applyTechniqueBurnout(Player player){
        for (ItemStack stack : player.getInventory().items){
            if (stack.getItem() instanceof InfinityItem item) player.getCooldowns().addCooldown(item, 1200);
            else if (stack.getItem() instanceof BlueItem item) player.getCooldowns().addCooldown(item, 1200);
            else if (stack.getItem() instanceof RedItem item) player.getCooldowns().addCooldown(item, 1200);
            else if (stack.getItem() instanceof HollowPurpleItem item) player.getCooldowns().addCooldown(item, 1200);
            else if (stack.getItem() instanceof UnlimitedVoidItem item) player.getCooldowns().addCooldown(item, 1200);
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

                    mc.options.keyInventory.setDown(false);
                    mc.options.keyChat.setDown(false);
                    mc.options.keyCommand.setDown(false);

                    Arrays.stream(mc.options.keyHotbarSlots).toList().forEach(keyMapping -> keyMapping.setDown(false));

                    mc.options.keyPlayerList.setDown(false);
                    mc.options.keyTogglePerspective.setDown(false);

                    player.setYRot(player.getPersistentData().getFloat("yaw"));
                    player.setXRot(player.getPersistentData().getFloat("pitch"));
                    System.out.println("SHOULD WORK");
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
