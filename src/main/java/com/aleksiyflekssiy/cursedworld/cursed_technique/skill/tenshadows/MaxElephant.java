package com.aleksiyflekssiy.cursedworld.cursed_technique.skill.tenshadows;

import com.aleksiyflekssiy.cursedworld.CursedWorld;
import com.aleksiyflekssiy.cursedworld.client.renderer.CustomDebugRenderer;
import com.aleksiyflekssiy.cursedworld.cursed_technique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.cursedworld.entity.MaxElephantEntity;
import com.aleksiyflekssiy.cursedworld.entity.RabbitEscapeEntity;
import com.aleksiyflekssiy.cursedworld.entity.ModEntities;
import com.aleksiyflekssiy.cursedworld.entity.Shikigami;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Random;

public class MaxElephant extends ShikigamiSkill {
    private Shikigami maxElephant;
    private int orderIndex = 0;

    @Override
    public void use(LivingEntity entity, UseType type, int charge) {
        if (entity.level().isClientSide) return;
        if (type == UseType.ACTIVATION) this.activate(entity);
        else if (type == UseType.CHARGING) this.charge(entity, charge);
        else if (type == UseType.RELEASING) this.release(entity);
    }

    @Override
    public void activate(LivingEntity entity) {
        if (isDead) return;
        if (!entity.isCrouching()){
            if (!isActive){
                BlockPos spawnPos = entity.blockPosition();
                maxElephant = new MaxElephantEntity(ModEntities.MAX_ELEPHANT.get(), entity.level());
                maxElephant.setPos(spawnPos.getCenter());

                shikigamiUUIDList.add(maxElephant.getUUID());
                entity.level().addFreshEntity(maxElephant);

                if (isTamed){
                    maxElephant.tame((Player) entity);
                }

                isActive = !isActive;
            }
            else if (isActive && isTamed){
                setTarget(entity,
                        blockPos -> maxElephant.followOrder(null, blockPos, MaxElephantEntity.MaxElephantOrder.values()[orderIndex]),
                        target -> maxElephant.followOrder(target, null, MaxElephantEntity.MaxElephantOrder.values()[orderIndex]));
            }
        }
        else {
            if (isActive && isTamed){
                maxElephant.discard();
                maxElephant = null;
                CustomDebugRenderer.AABB_LIST.keySet().forEach(CustomDebugRenderer::removeAABB);
                CustomDebugRenderer.OBB_LIST.keySet().forEach(CustomDebugRenderer::removeOBB);
                isActive = !isActive;
            }
        }
    }

    @Override
    public List<Shikigami> getShikigami() {
        return List.of(maxElephant);
    }

    @Override
    public void setShikigami(List<Shikigami> shikigamiList) {
        maxElephant = shikigamiList.get(0);
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
                case 1 -> owner.sendSystemMessage(Component.literal("PUSH"));
                case 2 -> owner.sendSystemMessage(Component.literal("MOVE"));
            }
        }
    }

    @Override
    public String getName() {
        return "max_elephant";
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return ResourceLocation.fromNamespaceAndPath(CursedWorld.MOD_ID, "textures/gui/max_elephant.png");
    }
}
