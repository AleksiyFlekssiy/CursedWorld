package com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.tenshadows;

import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.tutorialmod.entity.GreatSerpentEntity;
import com.aleksiyflekssiy.tutorialmod.entity.ModEntities;
import com.aleksiyflekssiy.tutorialmod.entity.NueEntity;
import com.aleksiyflekssiy.tutorialmod.entity.Shikigami;
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
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class GreatSerpent extends ShikigamiSkill {
    private GreatSerpentEntity greatSerpent = null;

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
        if (!isActive){
            greatSerpent = new GreatSerpentEntity(ModEntities.GREAT_SERPENT.get(), entity.level());
            HitResult result = ProjectileUtil.getHitResultOnViewVector(entity, target -> !target.equals(greatSerpent), 50);
            if (result.getType() == HitResult.Type.ENTITY) {
                EntityHitResult hitResult = (EntityHitResult) result;
                if (hitResult.getEntity() instanceof LivingEntity target) {
                    greatSerpent.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, 1, 1));
                }
            }
            else if (result.getType().equals(HitResult.Type.BLOCK)){
                BlockHitResult hitResult = (BlockHitResult) result;
                greatSerpent.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(hitResult.getBlockPos(), 1, 1));
                System.out.println(hitResult.getBlockPos());
            }
            else System.out.println("SOSAL");
            greatSerpent.setPos(entity.getX(), entity.getY() - 3, entity.getZ());
            greatSerpent.tame((Player) entity);
            entity.level().addFreshEntity(greatSerpent);
            isActive = true;
        }
        else isActive = false;
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
    public void switchOrder(LivingEntity owner) {

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
