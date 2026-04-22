package com.aleksiyflekssiy.cursedworld.cursed_technique.skill.tenshadows;

import com.aleksiyflekssiy.cursedworld.CursedWorld;
import com.aleksiyflekssiy.cursedworld.client.renderer.CustomDebugRenderer;
import com.aleksiyflekssiy.cursedworld.cursed_technique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.cursedworld.entity.MaxElephantEntity;
import com.aleksiyflekssiy.cursedworld.entity.ModEntities;
import com.aleksiyflekssiy.cursedworld.entity.Shikigami;
import com.aleksiyflekssiy.cursedworld.entity.ShikigamiOrder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class MaxElephant extends ShikigamiSkill {
    private Shikigami maxElephant;

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
                shikigamiList.add(maxElephant);
                entity.level().addFreshEntity(maxElephant);

                if (isTamed){
                    maxElephant.tame((Player) entity);
                }

                isActive = !isActive;
            }
            else if (isActive && isTamed){
                setTarget(entity,
                        blockPos -> maxElephant.followOrder(null, blockPos, this.getOrders().get(orderIndex)),
                        target -> maxElephant.followOrder(target, null, this.getOrders().get(orderIndex)));
            }
        }
        else {
            if (isActive && isTamed){
                maxElephant.discard();
                maxElephant = null;
                CustomDebugRenderer.AABB_LIST.keySet().forEach(CustomDebugRenderer::removeAABB);
                CustomDebugRenderer.OBB_LIST.keySet().forEach(CustomDebugRenderer::removeOBB);
                isActive = !isActive;
                shikigamiUUIDList.clear();
                shikigamiList.clear();
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
    public List<ShikigamiOrder> getOrders() {
        return List.of(
                ShikigamiOrder.NONE,
                ShikigamiOrder.ATTACK,
                ShikigamiOrder.PUSH,
                ShikigamiOrder.MOVE
        );
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
