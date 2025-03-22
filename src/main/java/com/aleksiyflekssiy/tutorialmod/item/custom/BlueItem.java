package com.aleksiyflekssiy.tutorialmod.item.custom;

import com.aleksiyflekssiy.tutorialmod.animation.BlueControlAnimation;
import com.aleksiyflekssiy.tutorialmod.capability.CursedEnergyCapability;
import com.aleksiyflekssiy.tutorialmod.entity.BlueEntity;
import com.aleksiyflekssiy.tutorialmod.entity.ModEntities;
import com.aleksiyflekssiy.tutorialmod.particle.ModParticles;
import com.aleksiyflekssiy.tutorialmod.sound.ModSoundEvents;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;


@Mod.EventBusSubscriber(modid = "tutorialmod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlueItem extends Item {
    private static final float PULL_RANGE = 10;
    private static final float PULL_FORCE = 1.5F;
    private static final float TELEPORT_RANGE = 25;
    private static final int USE_DURATION = 2000;
    // Длительность следа: 0.5 секунды (10 тиков)

    public BlueItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            // Серверная логика: спавн сущности и управление кулдауном
            if (!player.getCooldowns().isOnCooldown(this)) {
                player.startUsingItem(hand);
                stack.getOrCreateTag().putInt("Phase", 0);
            }
        }
        return InteractionResultHolder.consume(stack);
    }

    private void pullEntities(Player player){
        AABB pullArea = new AABB(player.position().add(-PULL_RANGE, -PULL_RANGE, -PULL_RANGE), player.position().add(PULL_RANGE, PULL_RANGE, PULL_RANGE));
        List<Entity> entitiesSelf = player.level().getEntitiesOfClass(Entity.class, pullArea);
        for (Entity entity : entitiesSelf) {
            Vec3 toPlayer = player.position().subtract(entity.position()).normalize().scale(PULL_FORCE * 2);
            entity.setDeltaMovement(entity.getDeltaMovement().add(toPlayer));
            spawnTrailParticles((ServerLevel) player.level(), entity);
        }
        player.getCooldowns().addCooldown(this, 20);
        player.sendSystemMessage(Component.literal("Blue LMB"));
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
                player.sendSystemMessage(Component.literal("Eyes of Wisdom"));
                stack.getOrCreateTag().putInt("Phase", 3);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundEvents.CHANT_3.get(), SoundSource.NEUTRAL, 1f, 1f);
                CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 10);
            } else if (holdTicks == 75 && currentPhase == 1) {
                if (!CursedEnergyCapability.isEnoughEnergy(player, 10)) return;
                player.sendSystemMessage(Component.literal("Twilight"));
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
            spawnRotatingParticles(serverLevel, player.position(), 5f, 10, holdTicks);
        }
    }

    private void teleport(Player player){
            Vec3 eyePos = player.getEyePosition(1.0F); // Позиция глаз игрока
            Vec3 lookVec = player.getViewVector(1.0F); // Вектор взгляда
            Vec3 endPos = eyePos.add(lookVec.x * TELEPORT_RANGE, lookVec.y * TELEPORT_RANGE, lookVec.z * TELEPORT_RANGE);

            // Трассировка взгляда
            ClipContext context = new ClipContext(eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
            HitResult result = player.level().clip(context);

            if (result.getType() == HitResult.Type.BLOCK) {
                // Если попали в блок, телепортируемся на верхнюю поверхность
                BlockHitResult blockHit = (BlockHitResult) result;
                Vec3 hitPos = blockHit.getLocation();
                int blockX = blockHit.getBlockPos().getX();
                int blockY = blockHit.getBlockPos().getY();
                int blockZ = blockHit.getBlockPos().getZ();

                // Устанавливаем точку на верхней грани блока
                Vec3 teleportPos = new Vec3(blockX + 0.5, blockY + 1.0, blockZ + 0.5);
                player.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
            } else {
                // Если в воздухе, телепортируемся на максимальную дистанцию
                player.teleportTo(endPos.x, endPos.y, endPos.z);
            }
            player.getCooldowns().addCooldown(this, 20);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (!(player.level() instanceof ServerLevel)) return false;
            ItemStack hand = player.getMainHandItem();
            if (!(hand.getItem() instanceof BlueItem)) return false;
            if (player.getCooldowns().isOnCooldown(this)) return false;
            if (!CursedEnergyCapability.isEnoughEnergy(player, 10)) return false;
            if (!player.isCrouching()) pullEntities(player);
            else teleport(player);
            CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 10);
            return true;
        }
        return false;
    }

    private void createSimpleBlue(Level level, Player player, BlueEntity blue) {
        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 lookVec = player.getLookAngle();
        Vec3 maxReach = eyePos.add(lookVec.scale(10.0));
        ClipContext clipContext = new ClipContext(eyePos, maxReach, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
        BlockHitResult hitResult = level.clip(clipContext);

        Vec3 targetPos = hitResult.getType() == HitResult.Type.BLOCK ?
                hitResult.getLocation() :
                eyePos.add(lookVec.scale(10.0));

        blue.setPos(targetPos.x, targetPos.y, targetPos.z);
        level.addFreshEntity(blue);
    }

    private void playAnimation(AbstractClientPlayer player){
        ModifierLayer<BlueControlAnimation> animationLayer = (ModifierLayer<BlueControlAnimation>) PlayerAnimationAccess.getPlayerAssociatedData(player)
                .get(new ResourceLocation("tutorialmod", "animation"));
        BlueControlAnimation animation = new BlueControlAnimation(player);
        animation.setUsing(true); // Анимация активна
        animationLayer.setAnimation(animation);
    }

    public static void stopAnimation(AbstractClientPlayer player){
        ModifierLayer<BlueControlAnimation> animationLayer = (ModifierLayer<BlueControlAnimation>) PlayerAnimationAccess.getPlayerAssociatedData(player)
                .get(new ResourceLocation("tutorialmod", "animation"));
        BlueControlAnimation animation = new BlueControlAnimation(player);
        animation.setUsing(false); // Анимация активна
        animationLayer.setAnimation(null);
    }

    public static void spawnTrailParticles(ServerLevel level, Entity entity) {
        // Спавним частицы в текущей позиции сущности
        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() / 2; // Центр сущности по высоте
        double z = entity.getZ();

        // Спавним синие частицы (например, minecraft:witch с синим оттенком)
        level.sendParticles(ModParticles.BLUE_PULL.get(), x, y, z, 3, 0.2, 0.2, 0.2, 0.0);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    public static void registerAnimation(Player player) {
        if (player instanceof AbstractClientPlayer clientPlayer) {
            PlayerAnimationAccess.getPlayerAssociatedData(clientPlayer)
                    .set(new ResourceLocation("tutorialmod", "animation"), new ModifierLayer<>());
        }
    }
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!level.isClientSide()) {
            int phase = stack.getOrCreateTag().getInt("Phase");
            boolean isFollowing = false;
            if (entity instanceof Player player) {
                if (!CursedEnergyCapability.isEnoughEnergy(player, 15)) return;
                if (player.isCrouching()) {
                    isFollowing = true;
                }
                BlueEntity blue = new BlueEntity(ModEntities.BLUE_ENTITY.get(), level, player, isFollowing, 60, 5, 3, 1.5f, phase);
                createSimpleBlue(level, player, blue);
                player.getCooldowns().addCooldown(this,  phase > 0 ? 60 * (phase + 1) : 60);
                stack.getOrCreateTag().putInt("Phase", 0);
                CursedEnergyCapability.setCursedEnergy(player, CursedEnergyCapability.getCursedEnergy(player) - 10);
            }
        }
        else {
            //if (entity instanceof AbstractClientPlayer clientPlayer) playAnimation(clientPlayer);
        }
    }

    private void spawnRotatingParticles(ServerLevel level, Vec3 center, float radius, float density, int holdTicks) {
        // Угол вращения зависит от времени (holdTicks), чтобы создать эффект спирали
        double angle = (holdTicks % 40) * Math.PI / 20; // Полный оборот каждые 2 секунды (40 тиков)
        double particleRadius = radius * 0.5; // Радиус вращения частиц меньше радиуса притяжения

        // Спавним частицы по кругу
        for (int i = 0; i < density; i++) {
            double offsetAngle = angle + (i * 2 * Math.PI / density);
            double x = center.x + particleRadius * Math.cos(offsetAngle);
            double z = center.z + particleRadius * Math.sin(offsetAngle);
            double y = center.y + (Math.sin(angle + i) * 0.5); // Добавляем небольшое колебание по Y

            level.sendParticles(ModParticles.BLUE_PULL.get(), x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    public static class ClientEventHandler {
        @SubscribeEvent
        public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof AbstractClientPlayer player) {
                registerAnimation(player);
                System.out.println("Registered animation for player: " + player.getName().getString());
            }
        }
    }


}