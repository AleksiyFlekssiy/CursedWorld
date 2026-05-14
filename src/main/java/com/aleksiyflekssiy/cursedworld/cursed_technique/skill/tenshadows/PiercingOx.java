package com.aleksiyflekssiy.cursedworld.cursed_technique.skill.tenshadows;

import com.aleksiyflekssiy.cursedworld.CursedWorld;
import com.aleksiyflekssiy.cursedworld.cursed_technique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.cursedworld.entity.ModEntities;
import com.aleksiyflekssiy.cursedworld.entity.PiercingOxEntity;
import com.aleksiyflekssiy.cursedworld.entity.Shikigami;
import com.aleksiyflekssiy.cursedworld.entity.ShikigamiOrder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class PiercingOx extends ShikigamiSkill {
    private Shikigami piercingOx;

    @Override
    public void activate(LivingEntity entity) {
        if (isDead) return;
        if (!entity.isCrouching()){
            if (!isActive){
                BlockPos spawnPos = entity.blockPosition();
                piercingOx = new PiercingOxEntity(ModEntities.PIERCING_OX.get(), entity.level());
                piercingOx.setPos(spawnPos.getCenter());

                shikigamiUUIDList.add(piercingOx.getUUID());
                shikigamiList.add(piercingOx);
                entity.level().addFreshEntity(piercingOx);

                if (isTamed){
                    piercingOx.tame((Player) entity);
                }

                isActive = !isActive;
            }
            else if (isActive && isTamed){
                setTarget(entity,
                        blockPos -> piercingOx.followOrder(null, blockPos, this.getOrders().get(orderIndex)),
                        target -> piercingOx.followOrder(target, null, this.getOrders().get(orderIndex)));
            }
        }
        else {
            if (isActive && isTamed){
                piercingOx.discard();
                piercingOx = null;
                isActive = !isActive;
                shikigamiUUIDList.clear();
                shikigamiList.clear();
            }
        }
    }

    @Override
    public List<Shikigami> getShikigami() {
        return List.of(piercingOx);
    }

    @Override
    public void setShikigami(List<Shikigami> shikigamiList) {
        piercingOx = shikigamiList.get(0);
    }

    @Override
    public List<ShikigamiOrder> getOrders() {
        return List.of(
                ShikigamiOrder.NONE,
                ShikigamiOrder.ATTACK,
                ShikigamiOrder.MOVE
        );
    }

    @Override
    public String getName() {
        return "piercing_ox";
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return ResourceLocation.fromNamespaceAndPath(CursedWorld.MOD_ID, "textures/gui/piercing_ox.png");
    }
}
