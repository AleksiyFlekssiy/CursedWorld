package com.aleksiyflekssiy.tutorialmod.cursed_technique.skill;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.capability.CursedEnergyCapability;
import com.aleksiyflekssiy.tutorialmod.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.LimitlessCursedTechnique;
import com.aleksiyflekssiy.tutorialmod.item.custom.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResultHolder;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UnlimitedVoid extends Skill {
    private static final float DOMAIN_RADIUS = 15.0F;
    private static final float BOUNDARY_THICKNESS = 1.2F;
    private static final int DOMAIN_DURATION = 600;

    private boolean domainActive = false;
    private int domainTicks = 0;
    private Map<BlockPos, BlockState> originalBlocks = new HashMap<>();
    private List<BlockPos> barrierBlocks = new ArrayList<>();
    private AABB domainArea = null;
    private Vec3 domainCenter = null;
    private Set<LivingEntity> trappedEntities = new HashSet<>();
    private Player domainOwner = null;

    @Override
    public void use(LivingEntity entity, UseType type, int charge) {
        if (!(entity instanceof ServerPlayer)) return;
        this.activate((Player) entity, entity.level());
    }

    public void activate(Player player, Level level) {
        if (level.isClientSide()) return;

        if (!domainActive) {
            if (!CursedEnergyCapability.isEnoughEnergy(player, 50)) return;
            // Центр сферы над игроком, пол под ногами
            double floorY = player.blockPosition().getY() - 0.1; // Пол на уровне ног игрока
            Vec3 center = new Vec3(player.getX(), floorY + DOMAIN_RADIUS / 2.0, player.getZ());
            activateDomainExpansion((ServerLevel) level, center, player);
            domainActive = true;
            domainCenter = center;
            domainTicks = 0;
            domainOwner = player;
            player.sendSystemMessage(Component.literal("Domain Expansion: Unlimited Void activated!"));
            player.setPos(player.getX(), floorY + 1.1, player.getZ());
            CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 50);
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    private void activateDomainExpansion(ServerLevel level, Vec3 center, Player player) {
        level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SPAWN, player.getSoundSource(), 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 1, 0, 0, 0, 0);

        BlockPos centerPos = new BlockPos((int) center.x, (int) center.y, (int) center.z);
        int radiusInt = (int) Math.ceil(DOMAIN_RADIUS);
        double floorY = player.getY(); // Пол прямо под игроком

        for (int x = -radiusInt; x <= radiusInt; x++) {
            for (int y = -radiusInt; y <= radiusInt; y++) {
                for (int z = -radiusInt; z <= radiusInt; z++) {
                    BlockPos pos = centerPos.offset(x, y, z);
                    double distanceSq = center.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    double distance = Math.sqrt(distanceSq);

                    if (distance <= DOMAIN_RADIUS) {
                        originalBlocks.put(pos.immutable(), level.getBlockState(pos));
                        if (distance >= DOMAIN_RADIUS - BOUNDARY_THICKNESS && distance <= DOMAIN_RADIUS + BOUNDARY_THICKNESS) {
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
        domainArea = new AABB(center.add(-DOMAIN_RADIUS, -DOMAIN_RADIUS, -DOMAIN_RADIUS),
                center.add(DOMAIN_RADIUS, DOMAIN_RADIUS, DOMAIN_RADIUS));
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, domainArea)) {
            if (entity != player) {
                double newY = floorY + 1.0;
                entity.setPos(entity.getX(), newY, entity.getZ());
                entity.setDeltaMovement(0, 0, 0);
                trappedEntities.add(entity);
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DOMAIN_DURATION, 255, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, DOMAIN_DURATION, 1, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.JUMP, DOMAIN_DURATION, 128, false, false));
                if (entity instanceof Mob mob) {
                    mob.setNoAi(true);
                }
            }
        }
    }

    private void affectEntities(Level level) {
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, domainArea)) {
            if (entity == domainOwner) continue;
            trappedEntities.add(entity);
        }
        for (LivingEntity entity : trappedEntities){
            entity.setPos(entity.getX(), entity.getY(), entity.getZ());
            entity.setDeltaMovement(0, 0, 0);
            entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
            if (entity instanceof Mob mob) {
                mob.setNoAi(true);
            } else if (entity instanceof Player player) {
                player.getAbilities().mayBuild = false;
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getAbilities().invulnerable = false;
                player.setSprinting(false);
            }
        }
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
            ServerLevel serverLevel = (ServerLevel) domainOwner.level();
            domainOwner.sendSystemMessage(Component.literal(String.valueOf(domainTicks)));
            if (domainActive && domainCenter != null) {
                domainTicks++;
                spawnBarrierParticles(serverLevel, domainCenter, DOMAIN_RADIUS);
                affectEntities(serverLevel);


                if (domainTicks >= DOMAIN_DURATION || checkBarrierDamage(serverLevel)) {
                    for (LivingEntity trapped : trappedEntities) {
                        trapped.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.1);
                        if (trapped instanceof Player affectedPlayer) {
                            affectedPlayer.getAbilities().mayBuild = true;
                            affectedPlayer.getAbilities().mayfly = affectedPlayer.getAbilities().instabuild;
                        }
                    }
                    applyTechniqueBurnout(domainOwner);
                    restoreOriginalBlocks(serverLevel);
                    MinecraftForge.EVENT_BUS.unregister(this);
                }
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
    public class PlayerDisabler {

        @SubscribeEvent
        public static void onPlayerAttack(AttackEntityEvent event) {
            Player player = event.getEntity();
            checkAndCancel(player, event);
        }

        @SubscribeEvent
        public static void onPlayerInteract(PlayerInteractEvent event) {
            Player player = event.getEntity();
            checkAndCancel(player, event);
        }

        @SubscribeEvent
        public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
            Player player = event.getEntity();
            checkAndCancel(player, event);
        }

        @SubscribeEvent
        public static void onPlayerInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
            Player player = event.getEntity();
            checkAndCancel(player, event);
        }

        @SubscribeEvent
        public static void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event) {
            Player player = event.getEntity();
            checkAndCancel(player, event);
        }

        @SubscribeEvent
        public static void onPlayerInteractItem(PlayerInteractEvent.RightClickItem event) {
            Player player = event.getEntity();
            checkAndCancel(player, event);
        }

        private static void checkAndCancel(Player player, net.minecraftforge.eventbus.api.Event event) {
            System.out.println("In the method");
            if (player.level() instanceof ServerLevel serverLevel) {
                for (Player serverPlayer : serverLevel.players()) {
                    if (CursedTechniqueCapability.getCursedTechnique(serverPlayer) instanceof LimitlessCursedTechnique limitless){
                        UnlimitedVoid unlimitedVoid = (UnlimitedVoid) limitless.getDomain();
                        if (unlimitedVoid.trappedEntities.contains(player) && player != unlimitedVoid.domainOwner) event.setCanceled(true);
                    }
                }
            }
        }
    }

    private Player getDomainOwner() {
        return domainOwner;
    }

    public boolean isDomainActive() {
        return domainActive;
    }

    private Set<LivingEntity> getTrappedEntities() {
        return trappedEntities;
    }

    @Override
    public String getName() {
        return "Unlimited Void";
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return new ResourceLocation("tutorialmod", "textures/gui/unlimited_void.png");
    }
}
