package com.aleksiyflekssiy.cursedworld.cursed_technique.skill.tenshadows;

import com.aleksiyflekssiy.cursedworld.CursedWorld;
import com.aleksiyflekssiy.cursedworld.cursed_technique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.cursedworld.entity.ModEntities;
import com.aleksiyflekssiy.cursedworld.entity.RoundDeerEntity;
import com.aleksiyflekssiy.cursedworld.entity.Shikigami;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class RoundDeer extends ShikigamiSkill {
    private Shikigami roundDeer;

    @Override
    public void activate(LivingEntity entity) {
        if (isDead) return;
        if (!entity.isCrouching()){
            if (!isActive){
                BlockPos spawnPos = entity.blockPosition();
                roundDeer = new RoundDeerEntity(ModEntities.ROUND_DEER.get(), entity.level(), (Player) entity);
                roundDeer.setPos(spawnPos.getCenter());

                shikigamiUUIDList.add(roundDeer.getUUID());
                shikigamiList.add(roundDeer);
                entity.level().addFreshEntity(roundDeer);

                if (isTamed){
                    roundDeer.tame((Player) entity);
                }

                isActive = !isActive;
            }
            else if (isActive && isTamed){
                setTarget(entity,
                        blockPos -> roundDeer.followOrder(null, blockPos, this.getOrders().get(orderIndex)),
                        target -> roundDeer.followOrder(target, null, this.getOrders().get(orderIndex)));
            }
        }
        else {
            if (isActive && isTamed){
                roundDeer.discard();
                roundDeer = null;
                isActive = !isActive;
                shikigamiUUIDList.clear();
                shikigamiList.clear();
            }
        }
    }

    @Override
    public List<Shikigami> getShikigami() {
        return List.of(roundDeer);
    }

    @Override
    public void setShikigami(List<Shikigami> shikigamiList) {
        roundDeer = shikigamiList.get(0);
    }

    @Override
    public String getName() {
        return "round_deer";
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return ResourceLocation.fromNamespaceAndPath(CursedWorld.MOD_ID, "textures/gui/round_deer.png");
    }
}
