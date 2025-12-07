package com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.tenshadows;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.tutorialmod.entity.DivineDogEntity;
import com.aleksiyflekssiy.tutorialmod.entity.ModEntities;
import com.aleksiyflekssiy.tutorialmod.entity.Shikigami;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DivineDogs extends ShikigamiSkill {
    private DivineDogEntity whiteDivineDog = null;
    private DivineDogEntity blackDivineDog = null;
    private boolean whiteDogDead, blackDogDead;
    private byte orderIndex = 0;

    public DivineDogs() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void use(LivingEntity entity, UseType type, int charge) {
        System.out.println("USING");
        switch (type) {
            case ACTIVATION -> {
                this.activate(entity);
            }
        }
    }

    @Override
    public void activate(LivingEntity entity) {
        if (isDead() || entity.level().isClientSide) {
            System.out.println("DEAD");
            return;
        }

        if (!entity.isCrouching()) {
        if (!isActive) {
            System.out.println("ACTIVATE");
            BlockPos spawnPos = entity.blockPosition();
            if (!whiteDogDead) { // Проверяем, жива ли сущность
                whiteDivineDog = new DivineDogEntity(ModEntities.DIVINE_DOG.get(), entity.level(), (Player) entity);
                whiteDivineDog.setColor(DivineDogEntity.Color.WHITE);
                whiteDivineDog.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                entity.level().addFreshEntity(whiteDivineDog);
                shikigamiUUIDList.add(whiteDivineDog.getUUID());
            }
            if (!blackDogDead) {
                blackDivineDog = new DivineDogEntity(ModEntities.DIVINE_DOG.get(), entity.level(), (Player) entity);
                blackDivineDog.setColor(DivineDogEntity.Color.BLACK);
                blackDivineDog.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                entity.level().addFreshEntity(blackDivineDog);
                shikigamiUUIDList.add(blackDivineDog.getUUID());
            }
            if (isTamed) {
                if (!whiteDogDead) whiteDivineDog.tame((Player) entity);
                if (!blackDogDead) blackDivineDog.tame((Player) entity);
            }
            else {
                if (!whiteDogDead) whiteDivineDog.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, entity);
                if (!blackDogDead) blackDivineDog.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, entity);
            }
            isActive = true;
        }
        else if (isActive && isTamed) {
            HitResult result = ProjectileUtil.getHitResultOnViewVector(entity, target -> !target.equals(entity), 100);
            if (result.getType() == HitResult.Type.ENTITY) {
                EntityHitResult hitResult = (EntityHitResult) result;
                if (hitResult.getEntity() instanceof LivingEntity target) {
                    System.out.println(target.getClass().getSimpleName());
                    if (!whiteDogDead) whiteDivineDog.followOrder(target, null, DivineDogEntity.DivineDogOrder.values()[orderIndex]);
                    if (!blackDogDead) blackDivineDog.followOrder(target, null, DivineDogEntity.DivineDogOrder.values()[orderIndex]);
                }
            } else if (result.getType() == HitResult.Type.BLOCK) {
                BlockHitResult hitResult = (BlockHitResult) result;
                System.out.println(hitResult.getBlockPos());
                if (!whiteDogDead) whiteDivineDog.followOrder(null, hitResult.getBlockPos(), DivineDogEntity.DivineDogOrder.values()[orderIndex]);
                if (!blackDogDead) blackDivineDog.followOrder(null, hitResult.getBlockPos(), DivineDogEntity.DivineDogOrder.values()[orderIndex]);
            } else {
                System.out.println("MISS");
            }
        }
        }
        else {
            if (isActive && isTamed) {
                System.out.println("DEACTIVATE");
                if (!whiteDogDead) whiteDivineDog.discard();
                whiteDivineDog = null;
                if (!blackDogDead) blackDivineDog.discard();
                blackDivineDog = null;
                shikigamiUUIDList.clear();
                isActive = false;
            }
        }
    }

    @Override
    public void setShikigami(List<Shikigami> shikigamiList) {
        if (shikigamiList.size() == 1 && shikigamiList.get(0) instanceof DivineDogEntity divineDogEntity) {
            if (whiteDogDead && blackDivineDog == null) this.blackDivineDog = divineDogEntity;
            else if (blackDogDead && whiteDivineDog == null) this.whiteDivineDog = divineDogEntity;
        }
        else if (shikigamiList.size() == 2){
            this.whiteDivineDog = (DivineDogEntity) shikigamiList.get(0);
            this.blackDivineDog = (DivineDogEntity) shikigamiList.get(1);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("whiteDogDead", whiteDogDead);
        tag.putBoolean("blackDogDead", blackDogDead);
    }

    @Override
    public void loadAdditional(CompoundTag tag) {
        super.loadAdditional(tag);
        whiteDogDead = tag.getBoolean("whiteDogDead");
        blackDogDead = tag.getBoolean("blackDogDead");
    }

    public boolean isDead(){
        return whiteDogDead && blackDogDead;
    }

    @Override
    public void setDead(boolean dead) {
        super.setDead(dead);
        if (!dead) {
            whiteDogDead = blackDogDead = false;
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
         if (event.getEntity() instanceof DivineDogEntity divineDogEntity && !divineDogEntity.level().isClientSide) {
            if (divineDogEntity.equals(whiteDivineDog)) {
                whiteDogDead = true;
                shikigamiUUIDList.remove(divineDogEntity.getUUID());
                if (isTamed) whiteDivineDog.getOwner().sendSystemMessage(Component.literal("Your White Divine Dog has died"));
            } else if (divineDogEntity.equals(blackDivineDog)) {
                blackDogDead = true;
                shikigamiUUIDList.remove(divineDogEntity.getUUID());
                if (isTamed) blackDivineDog.getOwner().sendSystemMessage(Component.literal("Your Black Divine Dog has died"));
            }
            if (whiteDogDead && blackDogDead) {
                if (isTamed) {
                    isDead = true;
                    if (whiteDivineDog != null) whiteDivineDog.getOwner().sendSystemMessage(Component.literal("Your Divine Dogs have died"));
                    else if (blackDivineDog != null) blackDivineDog.getOwner().sendSystemMessage(Component.literal("Your Divine Dogs have died"));
                } else {
                    whiteDogDead = blackDogDead = false;
                    if (event.getSource().getEntity() instanceof Player player && player.equals(divineDogEntity.getOwner())) {
                        isTamed = true;
                        player.sendSystemMessage(Component.literal("You tamed divine dogs"));
                    }
                }
                whiteDivineDog = null;
                blackDivineDog = null;
                shikigamiUUIDList.clear();
                isActive = false;
            }
        }
        //MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public List<Shikigami> getShikigami() {
        List<Shikigami> shikigami = new ArrayList<>();
        shikigami.add(whiteDivineDog);
        shikigami.add(blackDivineDog);
        return shikigami;
    }

    @Override
    public void switchOrder(LivingEntity owner, int direction) {
        if (isTamed) {
            switch (direction){
                case -1 -> {
                    if (--orderIndex <= -1) orderIndex = 2;
                }
                case 1 -> {
                    if (++orderIndex >= 3) orderIndex = 0;
                }
            }
            switch (orderIndex) {
                case 0 -> owner.sendSystemMessage(Component.literal("NONE"));
                case 1 -> owner.sendSystemMessage(Component.literal("ATTACK"));
                case 2 -> owner.sendSystemMessage(Component.literal("MOVE"));
            }
        }
    }

    @Override
    public String getName() {
        return "divine_dogs";
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "textures/gui/divine_dogs.png");
    }
}
