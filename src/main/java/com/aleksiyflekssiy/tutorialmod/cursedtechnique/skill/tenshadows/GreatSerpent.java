package com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.tenshadows;

import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.tutorialmod.entity.DivineDogEntity;
import com.aleksiyflekssiy.tutorialmod.entity.GreatSerpentEntity;
import com.aleksiyflekssiy.tutorialmod.entity.ModEntities;
import com.aleksiyflekssiy.tutorialmod.entity.Shikigami;
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

    public List<Shikigami> getShikigami() {
        return List.of(greatSerpent);
    }

    @Override
    public void activate(LivingEntity entity) {
        if (!entity.isCrouching()) {
            if (!isActive) {
                greatSerpent = new GreatSerpentEntity(ModEntities.GREAT_SERPENT.get(), entity.level());
                HitResult result = ProjectileUtil.getHitResultOnViewVector(entity, target -> !target.equals(greatSerpent), 50);
                if (result.getType() == HitResult.Type.ENTITY) {
                    EntityHitResult hitResult = (EntityHitResult) result;
                    if (hitResult.getEntity() instanceof LivingEntity target) {
                        greatSerpent.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, 1, 1));
                    }
                } else if (result.getType().equals(HitResult.Type.BLOCK)) {
                    BlockHitResult hitResult = (BlockHitResult) result;
                    greatSerpent.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(hitResult.getBlockPos(), 1, 1));
                    System.out.println(hitResult.getBlockPos());
                } else System.out.println("SOSAL");
                BlockPos initialPos = entity.blockPosition().below(3);
                greatSerpent.setSpawnPos(initialPos);
                greatSerpent.setPos(initialPos.getCenter());
                greatSerpent.tame((Player) entity);
                entity.level().addFreshEntity(greatSerpent);
                isActive = true;
            }
            else if (isActive && isTamed){
                HitResult result = ProjectileUtil.getHitResultOnViewVector(entity, target -> !target.equals(entity), 50);
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
                    isActive = false;
                }
            }
        }
    }

    @SubscribeEvent
    public void tick(LivingEvent.LivingTickEvent event) {
        if (!isActive) return;
        if (greatSerpent == null || !greatSerpent.isAlive()) {
            greatSerpent = null;
            isActive = false;
        }
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
        return ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/gui/nue.png");
    }
}
