package com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.tenshadows;

import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.tutorialmod.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class GreatSerpent extends ShikigamiSkill {
    private GreatSerpentEntity greatSerpent = null;
    private int orderIndex = 0;

    public GreatSerpent(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void use(LivingEntity entity, UseType type, int charge) {
        switch (type){
            case ACTIVATION -> this.activate(entity);
        }
    }

    public void setShikigami(GreatSerpentEntity greatSerpent){
        if (this.greatSerpent == null) {
            this.greatSerpent = greatSerpent;
            System.out.println("The Great Serpent has set");
        }
    }

    public List<Shikigami> getShikigami() {
        return List.of(greatSerpent);
    }

    @Override
    public void activate(LivingEntity entity) {
        if (isDead){
            entity.sendSystemMessage(Component.literal("Your Great Serpent is dead"));
            return;
        }

        if (!entity.isCrouching()) {
            if (!isActive) {
                if (shikigamiUUIDList.isEmpty()) greatSerpent = new GreatSerpentEntity(ModEntities.GREAT_SERPENT.get(), entity.level(), (Player) entity);
                BlockPos initialPos = entity.blockPosition().below(3);
                greatSerpent.setPos(initialPos.getCenter());
                greatSerpent.setSpawnPos(initialPos);
                shikigamiUUIDList.add(greatSerpent.getUUID());
                if (isTamed) greatSerpent.tame((Player) entity);

                HitResult result = ProjectileUtil.getHitResultOnViewVector(entity, target -> !target.equals(greatSerpent), 100);
                if (result.getType() == HitResult.Type.ENTITY) {
                    EntityHitResult hitResult = (EntityHitResult) result;
                    if (hitResult.getEntity() instanceof LivingEntity target) {
                        greatSerpent.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, 2, 4));
                        greatSerpent.calculateAndSetSegmentCount(target.blockPosition());
                    }
                } else if (result.getType().equals(HitResult.Type.BLOCK)) {
                    BlockHitResult hitResult = (BlockHitResult) result;
                    greatSerpent.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(hitResult.getBlockPos(), 2, 4));
                    greatSerpent.calculateAndSetSegmentCount(hitResult.getBlockPos());
                    System.out.println(hitResult.getBlockPos());
                } else System.out.println("SOSAL");

                entity.level().addFreshEntity(greatSerpent);
                isActive = true;
            }
            else if (isActive && isTamed){
                HitResult result = ProjectileUtil.getHitResultOnViewVector(entity, target -> !target.equals(entity), 100);
                if (result.getType() == HitResult.Type.ENTITY) {
                    EntityHitResult hitResult = (EntityHitResult) result;
                    if (hitResult.getEntity() instanceof LivingEntity target) {
                        System.out.println(target.getClass().getSimpleName());
                        if (greatSerpent.isAlive()) greatSerpent.followOrder(target, null, GreatSerpentEntity.GreatSerpentOrder.values()[orderIndex]);
                    }
                } else if (result.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult hitResult = (BlockHitResult) result;
                    System.out.println(hitResult.getBlockPos());
                    if (greatSerpent.isAlive()) greatSerpent.followOrder(null, hitResult.getBlockPos(), GreatSerpentEntity.GreatSerpentOrder.values()[orderIndex]);
                } else {
                    System.out.println("MISS");
                }
            }
        }
        else {
            if (isActive && isTamed) {
                if (greatSerpent != null && greatSerpent.isAlive()) {
                    greatSerpent.discard();
                    greatSerpent = null;
                    shikigamiUUIDList.clear();
                    isActive = false;
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof GreatSerpentEntity greatSerpentEntity && !greatSerpentEntity.level().isClientSide){
            if (greatSerpent != null && greatSerpent.equals(greatSerpentEntity)){
                if (isTamed){
                    isDead = true;
                    greatSerpent.getOwner().sendSystemMessage(Component.literal("Your Great Serpent has died"));
                }
                else if (!isTamed && event.getSource().getEntity() instanceof Player player && player.equals(greatSerpent.getOwner())){
                    isTamed = true;
                    player.sendSystemMessage(Component.literal("You have tamed Great Serpent"));
                }
                isActive = false;
                greatSerpent = null;
                shikigamiUUIDList.clear();
            }
        }
    }

    @Override
    public void setShikigami(List<Shikigami> shikigamiList) {
        if (this.greatSerpent == null && shikigamiList.get(0) instanceof GreatSerpentEntity greatSerpentEntity) this.greatSerpent = greatSerpentEntity;
    }

    @Override
    public void switchOrder(LivingEntity owner, int direction) {
        if (isTamed) {
            switch (direction){
                case -1 -> {
                    if (--orderIndex <= -1) orderIndex = 1;
                }
                case 1 -> {
                    if (++orderIndex >= 2) orderIndex = 0;
                }
            }
            switch (orderIndex) {
                case 0 -> owner.sendSystemMessage(Component.literal("NONE"));
                case 1 -> owner.sendSystemMessage(Component.literal("MOVE"));
            }
        }
    }

    @Override
    public String getName() {
        return "GreatSerpent";
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/gui/great_serpent.png");
    }
}
