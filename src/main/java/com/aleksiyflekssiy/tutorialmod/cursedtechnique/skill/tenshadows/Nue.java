package com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.tenshadows;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.tutorialmod.entity.ModEntities;
import com.aleksiyflekssiy.tutorialmod.entity.NueEntity;
import com.aleksiyflekssiy.tutorialmod.entity.Shikigami;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Nue extends ShikigamiSkill {
    private NueEntity nue = null;
    private byte orderIndex = 0;

    public Nue(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void use(LivingEntity entity, UseType type, int charge) {
        switch (type){
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
                if (nue == null || !nue.isAlive()) { // Проверяем, жива ли сущность
                    nue = new NueEntity(ModEntities.NUE.get(), entity.level());
                    nue.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                    entity.level().addFreshEntity(nue);
                }
                if (isTamed) {
                    nue.tame((Player) entity);
                } else {
                    if (nue.canAttack(entity)) nue.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, entity);
                }
                isActive = !isActive;
            } else if (isActive && isTamed) {
                if (nue.getControllingPassenger() == null) {
                    HitResult result = ProjectileUtil.getHitResultOnViewVector(entity, target -> !target.equals(nue), 50);
                    if (result.getType() == HitResult.Type.ENTITY) {
                        EntityHitResult hitResult = (EntityHitResult) result;
                        if (hitResult.getEntity() instanceof LivingEntity target) {
                            System.out.println(target.getClass().getSimpleName());
                            nue.followOrder(target, null, NueEntity.NueOrder.values()[orderIndex]);
                        }
                    } else if (result.getType() == HitResult.Type.BLOCK) {
                        BlockHitResult hitResult = (BlockHitResult) result;
                        System.out.println(hitResult.getBlockPos());
                        nue.followOrder(null, hitResult.getBlockPos(), NueEntity.NueOrder.values()[orderIndex]);
                    } else {
                        System.out.println("MISS");
                    }
                }
                else if (entity.equals(nue.getControllingPassenger())){
                    nue.tryGrabEntityBelow(null);
                }
            }
        }
        else {
            if (isActive && isTamed) {
                if (nue.getControllingPassenger() == null) {
                    System.out.println("DEACTIVATE");
                    nue.discard();
                    nue = null;
                    isActive = !isActive;
                }
            }
        }
    }


    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof NueEntity entity && entity.equals(nue)) {
            if (nue != null && !nue.isAlive()) {
                if (isTamed) {
                    isDead = true;
                    nue.getOwner().sendSystemMessage(Component.literal("Nue has died"));
                }
                else if (event.getSource().getEntity() instanceof Player player) {
                    if (!isTamed) {
                        isTamed = true;
                        System.out.println("TAMED");
                        player.sendSystemMessage(Component.literal("You tamed Nue"));
                    }
                }
                nue = null;
            }
            isActive = false;
        }
        //MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public List<Shikigami> getShikigami() {
        return List.of(nue);
    }

    @Override
    public void switchOrder(LivingEntity owner) {
        if (isTamed) {
            if (++orderIndex >= 4) {
                orderIndex = 0;
            }
            switch (orderIndex) {
                case 0 -> owner.sendSystemMessage(Component.literal("NONE"));
                case 1 -> owner.sendSystemMessage(Component.literal("ATTACK"));
                case 2 -> owner.sendSystemMessage(Component.literal("GRAB"));
                case 3 -> owner.sendSystemMessage(Component.literal("MOVE"));
            }
        }
    }

    @Override
    public String getName() {
        return "Nue";
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/gui/nue.png");
    }
}
