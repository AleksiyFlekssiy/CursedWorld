package com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.limitless;

import com.aleksiyflekssiy.tutorialmod.capability.CursedEnergyCapability;
import com.aleksiyflekssiy.tutorialmod.config.ModConfig;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.Skill;
import com.aleksiyflekssiy.tutorialmod.entity.HollowPurpleEntity;
import com.aleksiyflekssiy.tutorialmod.entity.ModEntities;
import com.aleksiyflekssiy.tutorialmod.entity.animation.AnimationBlueEntity;
import com.aleksiyflekssiy.tutorialmod.entity.animation.AnimationRedEntity;
import com.aleksiyflekssiy.tutorialmod.sound.ModSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import static com.aleksiyflekssiy.tutorialmod.util.RotationUtil.getOffsetLookPosition;

public class HollowPurple extends Skill {
    private AnimationBlueEntity blueEntity;
    private AnimationRedEntity redEntity;
    private HollowPurpleEntity hollowPurpleEntity;
    private int CHANT = 0;
    public final HollowPurpleSummon hollowPurpleSummon = new HollowPurpleSummon();

    @Override
    public void use(LivingEntity entity, UseType type, int charge) {
        if (!(entity instanceof ServerPlayer)) return;
        switch (type){
            case CHARGING -> hollowPurpleSummon.charge(entity, charge);
            case RELEASING -> hollowPurpleSummon.release(entity);
        }
    }

    public class HollowPurpleSummon extends Skill{

        @Override
        public void charge(LivingEntity entity, int charge) {
            Level level = entity.level();
            boolean bool = ModConfig.SERVER.FAST_CHANT.get() == true ||
                    (charge == 30 || charge == 60 || charge == 120 || charge == 125 || charge == 250 || charge == 300 || charge == 350 || charge == 400);
            if (bool && CHANT == 0) {
                if (!spendCursedEnergy(entity, 10)) return;
                blueEntity = new AnimationBlueEntity(ModEntities.ANIMATION_BLUE_ENTITY.get(), level, entity);
                createBlue(level, entity, blueEntity);
                CHANT++;
            }
            else if (bool && CHANT == 1) {
                if (!spendCursedEnergy(entity, 20)) return;
                redEntity = new AnimationRedEntity(ModEntities.ANIMATION_RED_ENTITY.get(), level, (Player) entity);
                createRed(level, entity, redEntity);
                CHANT++;
            }
            else if (bool && CHANT == 2) {
                blueEntity.setFusion();
                redEntity.setFusion();
                CHANT++;
            }
            else if (bool && CHANT == 3) {
                hollowPurpleEntity = new HollowPurpleEntity(ModEntities.HOLLOW_PURPLE_ENTITY.get(), level, (Player) entity, 5, 2);
                createHollowPurple(level, entity, hollowPurpleEntity);
                CHANT++;
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSoundEvents.HOLLOW_PURPLE_MERGE.get(), SoundSource.NEUTRAL, 1, 1);
                blueEntity.discard();
                redEntity.discard();
            }
            else if (bool && CHANT == 4) {
                if (!spendCursedEnergy(entity, 10)) return;
                hollowPurpleEntity.chant();
                CHANT++;
                entity.sendSystemMessage(Component.literal("Nine Ropes"));
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSoundEvents.CHANT_1.get(), SoundSource.NEUTRAL, 1, 1);
            }
            else if (bool && CHANT == 5) {
                if (!spendCursedEnergy(entity, 10)) return;
                hollowPurpleEntity.chant();
                CHANT++;
                entity.sendSystemMessage(Component.literal("Polarized Light"));
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSoundEvents.CHANT_2.get(), SoundSource.NEUTRAL, 1, 1);
            }
            else if (bool && CHANT == 6) {
                if (!spendCursedEnergy(entity, 10)) return;
                hollowPurpleEntity.chant();
                CHANT++;
                entity.sendSystemMessage(Component.literal("Crow and Declaration"));
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSoundEvents.CHANT_2.get(), SoundSource.NEUTRAL, 1, 0.85F);
            }
            else if (bool && CHANT == 7) {
                if (!spendCursedEnergy(entity, 10)) return;
                hollowPurpleEntity.chant();
                CHANT++;
                entity.sendSystemMessage(Component.literal("Between Front and Back"));
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSoundEvents.CHANT_3.get(), SoundSource.NEUTRAL, 1, 1);
            }
        }

        @Override
        public void release(LivingEntity entity) {
            if (entity instanceof Player player && !player.level().isClientSide()) {
                if (blueEntity != null) blueEntity.discard();
                if (redEntity != null) redEntity.discard();
                if (hollowPurpleEntity != null) {
                    hollowPurpleEntity.launch(player.getLookAngle().normalize());
                    CHANT = 0;
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundEvents.HOLLOW_PURPLE_LAUNCH.get(), SoundSource.NEUTRAL, 1, 1);
                }
            }
        }

        @Override
        public String getName() {
            return "HollowPurpleSummon";
        }

        @Override
        public ResourceLocation getSkillIcon() {
            return null;
        }
    }

    private void createBlue(Level level, Entity entity, AnimationBlueEntity blueEntity) {
        Vec3 eyePos = entity.getEyePosition(1f);
        Vec3 lookVec = entity.getLookAngle();
        Vec3 maxReach = eyePos.add(lookVec.scale(2f));

        Vec3 targetPos = getOffsetLookPosition(entity, maxReach, -1.5, 0, 0);
        blueEntity.setPos(targetPos);
        level.addFreshEntity(blueEntity);
    }

    private void createRed(Level level, Entity entity, AnimationRedEntity redEntity) {
        Vec3 eyePos = entity.getEyePosition(1f);
        Vec3 lookVec = entity.getLookAngle();
        Vec3 maxReach = eyePos.add(lookVec.scale(2f));

        Vec3 targetPos = getOffsetLookPosition(entity, maxReach, 1.5, 0, 0);
        redEntity.setPos(targetPos);
        level.addFreshEntity(redEntity);
    }

    private void createHollowPurple(Level level, Entity entity, HollowPurpleEntity hollowPurpleEntity) {
        Vec3 eyePos = entity.getEyePosition(1f);
        Vec3 lookVec = entity.getLookAngle();
        Vec3 maxReach = eyePos.add(lookVec.scale(2.5f));

        Vec3 targetPos = getOffsetLookPosition(entity, maxReach, 0, 0, 0);
        hollowPurpleEntity.setPos(targetPos);
        level.addFreshEntity(hollowPurpleEntity);
    }

    @Override
    public String getName() {
        return "Hollow Purple";
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/gui/hollow_purple.png");
    }
}
