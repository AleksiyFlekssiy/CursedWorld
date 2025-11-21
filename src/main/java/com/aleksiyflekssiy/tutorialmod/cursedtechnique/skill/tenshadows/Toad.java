package com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.tenshadows;

import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.tutorialmod.entity.ModEntities;
import com.aleksiyflekssiy.tutorialmod.entity.Shikigami;
import com.aleksiyflekssiy.tutorialmod.entity.ToadEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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

import java.util.List;

public class Toad extends ShikigamiSkill {
    private ToadEntity toad;
    private byte orderIndex = 0;

    public Toad() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void use(LivingEntity entity, UseType type, int charge) {
        switch (type) {
            case ACTIVATION -> this.activate(entity);
        }
    }

    @Override
    public void activate(LivingEntity entity) {
        if (isDead) {
            System.out.println("DEAD");
            return;
        }
        if (!entity.isCrouching()) {
            if (!isActive) {
                System.out.println("ACTIVATE");
                BlockPos spawnPos = entity.blockPosition();
                if (toad == null || !toad.isAlive()) { // Проверяем, жива ли сущность
                    toad = new ToadEntity(ModEntities.TOAD.get(), entity.level());
                    toad.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                    entity.level().addFreshEntity(toad);
                }
                if (isTamed) {
                    toad.tame((Player) entity);
                }
                else toad.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, entity);
                isActive = !isActive;
            } else if (isActive && isTamed) {
                HitResult result = ProjectileUtil.getHitResultOnViewVector(toad.getOwner(), target -> !target.equals(toad), 50);
                if (result.getType() == HitResult.Type.ENTITY) {
                    EntityHitResult hitResult = (EntityHitResult) result;
                    if (hitResult.getEntity() instanceof LivingEntity target) {
                        System.out.println(target.getClass().getSimpleName());
                        toad.followOrder(target, null, ToadEntity.ToadOrder.values()[orderIndex]);
                    }
                } else if (result.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult hitResult = (BlockHitResult) result;
                    System.out.println(hitResult.getBlockPos());
                    toad.followOrder(null, hitResult.getBlockPos(), ToadEntity.ToadOrder.values()[orderIndex]);
                } else {
                    System.out.println("MISS");
                }
            }
        } else {
            if (isActive && isTamed) {
                System.out.println("DEACTIVATE");
                stopBehaviors();
                toad.discard();
                toad = null;
                isActive = !isActive;
            }
        }
    }

    @Override
    public void switchOrder(LivingEntity owner, int direction) {
        if (isTamed) {
            switch (direction){
                case -1 -> {
                    if (--orderIndex <= -1) orderIndex = 4;
                }
                case 1 -> {
                    if (++orderIndex >= 5) orderIndex = 0;
                }
            }
            switch (orderIndex) {
                case 0 -> owner.sendSystemMessage(Component.literal("NONE"));
                case 1 -> owner.sendSystemMessage(Component.literal("PULL"));
                case 2 -> owner.sendSystemMessage(Component.literal("SWING"));
                case 3 -> owner.sendSystemMessage(Component.literal("IMMOBILIZE"));
                case 4 -> owner.sendSystemMessage(Component.literal("MOVE"));
            }
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ToadEntity entity && entity.equals(toad)) {
            if (toad != null && !toad.isAlive()) {
                if (isTamed) {
                    isDead = true;
                    toad.getOwner().sendSystemMessage(Component.literal("Toad has died"));
                } else if (event.getSource().getEntity() instanceof Player player) {
                    if (!isTamed) {
                        isTamed = true;
                        System.out.println("TAMED");
                        player.sendSystemMessage(Component.literal("You tamed Toad"));
                    }
                }
                stopBehaviors();
                toad = null;
            }
            isActive = false;
        }
        //MinecraftForge.EVENT_BUS.unregister(this);
    }

    private void stopBehaviors(){
        if (toad != null) toad.getBrain().stopAll((ServerLevel) toad.level(), toad);
    }

    @Override
    public List<Shikigami> getShikigami() {
        return List.of(this.toad);
    }

    @Override
    public String getName() {
        return "Toad";
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/gui/toad.png");
    }
}
