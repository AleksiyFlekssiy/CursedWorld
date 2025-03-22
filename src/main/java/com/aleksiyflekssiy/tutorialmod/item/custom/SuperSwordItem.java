package com.aleksiyflekssiy.tutorialmod.item.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

public class SuperSwordItem extends SwordItem {
    public SuperSwordItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            if (!player.getCooldowns().isOnCooldown(this)) {
                // Получаем точку клика с ограничением в 5 блоков
                Vec3 eyePos = player.getEyePosition(1.0f);
                Vec3 lookVec = player.getLookAngle();
                Vec3 maxReach = eyePos.add(lookVec.scale(5.0));
                ClipContext clipContext = new ClipContext(eyePos, maxReach, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
                BlockHitResult hitResult = level.clip(clipContext);

                // Проверяем, попал ли клик на блок
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    Vec3 targetPos = hitResult.getLocation(); // Точка перед блоком

                    // Устанавливаем задержку и сохраняем координаты цели
                    player.getPersistentData().putInt("aoe_delay", 20);
                    player.getPersistentData().putDouble("aoe_target_x", targetPos.x);
                    player.getPersistentData().putDouble("aoe_target_y", targetPos.y);
                    player.getPersistentData().putDouble("aoe_target_z", targetPos.z);

                    initParticlePositions(player);

                    player.getCooldowns().addCooldown(this, 20);
                    return InteractionResultHolder.success(stack); // Успешная активация
                }
            }
        }
        return InteractionResultHolder.pass(stack); // Ничего не делаем, если не кликнули на блок
    }

    private void initParticlePositions(Player player) {
        List<Vec3> particlePositions = new ArrayList<>();
        int particleCount = 36;
        double radius = 5.0;
        Vec3 center = player.position();

        for (int i = 0; i < particleCount; i++) {
            double angle = Math.toRadians((360.0 / particleCount) * i);
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);
            double y = center.y;
            particlePositions.add(new Vec3(x, y, z));
        }

        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < particlePositions.size(); i++) {
            Vec3 pos = particlePositions.get(i);
            tag.putDouble("particle_" + i + "_x", pos.x);
            tag.putDouble("particle_" + i + "_y", pos.y);
            tag.putDouble("particle_" + i + "_z", pos.z);
        }
        player.getPersistentData().put("aoe_particles", tag);
    }

    private static void updateParticlePositions(Player player, ServerLevel level) {
        int delay = player.getPersistentData().getInt("aoe_delay");
        if (delay <= 0) return;

        Vec3 targetPos = new Vec3(
                player.getPersistentData().getDouble("aoe_target_x"),
                player.getPersistentData().getDouble("aoe_target_y"),
                player.getPersistentData().getDouble("aoe_target_z")
        );

        CompoundTag tag = player.getPersistentData().getCompound("aoe_particles");
        int particleCount = 36;
        double totalTicks = 20.0;
        double progress = (totalTicks - delay) / totalTicks;

        for (int i = 0; i < particleCount; i++) {
            double startX = tag.getDouble("particle_" + i + "_x");
            double startY = tag.getDouble("particle_" + i + "_y");
            double startZ = tag.getDouble("particle_" + i + "_z");

            double x = startX + (targetPos.x - startX) * progress;
            double y = startY + (targetPos.y - startY) * progress;
            double z = startZ + (targetPos.z - startZ) * progress;

            level.sendParticles(
                    ParticleTypes.FLAME,
                    x, y, z,
                    1,
                    0, 0, 0, // Без смещения
                    0.0       // Статичные частицы
            );
        }
    }

    private static void activateAoe(Player player, Level level) {
        double radius = 5.0;
        Vec3 center = new Vec3(
                player.getPersistentData().getDouble("aoe_target_x"),
                player.getPersistentData().getDouble("aoe_target_y"),
                player.getPersistentData().getDouble("aoe_target_z")
        );

        level.getEntitiesOfClass(LivingEntity.class, new AABB(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        )).forEach(entity -> {
            if (entity instanceof Mob) {
                entity.hurt(entity.damageSources().playerAttack(player), 5.0F);
                entity.addDeltaMovement(new Vec3(0,5,0));
            }
        });

        level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Mod.EventBusSubscriber(modid = "tutorialmod", bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class AoeHandler {
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            Player player = event.player;
            if (!player.level().isClientSide() && player.level() instanceof ServerLevel serverLevel) {
                int delay = player.getPersistentData().getInt("aoe_delay");
                if (delay > 0) {
                    updateParticlePositions(player, serverLevel);
                    player.getPersistentData().putInt("aoe_delay", delay - 1);
                    if (delay - 1 == 0) {
                        activateAoe(player, player.level());
                    }
                }
            }
        }
    }
}
