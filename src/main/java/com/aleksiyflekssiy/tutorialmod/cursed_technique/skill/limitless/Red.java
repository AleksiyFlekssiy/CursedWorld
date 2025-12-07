package com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.limitless;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.config.ModConfig;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.Skill;
import com.aleksiyflekssiy.tutorialmod.entity.ModEntities;
import com.aleksiyflekssiy.tutorialmod.entity.RedEntity;
import com.aleksiyflekssiy.tutorialmod.sound.ModSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Red extends Skill {
    private static final int LIFETIME = 80;
    private static final float EXPLOSION_POWER = 2.5F;
    private static final float SPEED = 2.5F;
    private int CHANT = 0;
    public final Push push = new Push();
    public final RedSummon redSummon = new RedSummon();

    public void use(LivingEntity entity, UseType type, int charge){
        if (!(entity instanceof ServerPlayer)) return;
        switch (type){
            case ACTIVATION -> push.activate(entity);
            case CHARGING -> redSummon.charge(entity, charge);
            case RELEASING -> redSummon.release(entity);
        }
    }

    public class Push extends Skill {

        public void activate(LivingEntity entity) {
            if (entity instanceof Player player) {
                if (!spendCursedEnergy(entity, 15)) return;
                RedEntity redEntity = new RedEntity(ModEntities.RED_ENTITY.get(), player.level(), player, 60, 5, 5f, RedEntity.ATTACK_TYPE.MELEE, 0);
                createRed(player.level(), player, redEntity);
            }
        }

        @Override
        public String getName() {
            return "push";
        }

        @Override
        public ResourceLocation getSkillIcon() {
            return null;
        }
    }

    public class RedSummon extends Skill{

        public void charge(LivingEntity entity, int charge){
            chant(entity, charge);
        }

        public boolean chant(LivingEntity entity, int charge) {
            Level serverLevel = entity.level();
            boolean bool = ModConfig.SERVER.FAST_CHANT.get() == true || (charge == 50 || charge == 75 || charge == 100);
            if (bool && CHANT == 2) {
                if (!spendCursedEnergy(entity, 10)) return false;
                entity.sendSystemMessage(Component.literal("Pillar of Light"));
                CHANT = 3;
                serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSoundEvents.CHANT_3.get(), SoundSource.NEUTRAL, 1f, 1f);
            } else if (bool && CHANT == 1) {
                if (!spendCursedEnergy(entity, 10)) return false;
                entity.sendSystemMessage(Component.literal("Paramita"));
                CHANT = 2;
                serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSoundEvents.CHANT_2.get(), SoundSource.NEUTRAL, 1f, 1f);
            } else if (bool && CHANT == 0) {
                if (!spendCursedEnergy(entity, 10)) return false;
                entity.sendSystemMessage(Component.literal("Phase"));
                CHANT = 1;
                serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSoundEvents.CHANT_1.get(), SoundSource.NEUTRAL, 1f, 1f);
            }
            return true;
        }

        public void release(LivingEntity entity) {
            if (entity instanceof Player player && !player.level().isClientSide()){
                RedEntity orb = new RedEntity(ModEntities.RED_ENTITY.get(), player.level(), player, LIFETIME, SPEED, EXPLOSION_POWER, RedEntity.ATTACK_TYPE.RANGED, CHANT    );
                createRed(player.level(), player, orb);
                CHANT = 0;
            }
        }

        @Override
        public String getName() {
            return "red_summon";
        }

        @Override
        public ResourceLocation getSkillIcon() {
            return null;
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

    @Override
    public String getName() {
        return "red";
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "textures/gui/red.png");
    }
}
