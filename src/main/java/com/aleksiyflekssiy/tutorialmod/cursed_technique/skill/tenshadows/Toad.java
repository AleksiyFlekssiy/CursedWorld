package com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.tenshadows;

import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.tutorialmod.entity.ModEntities;
import com.aleksiyflekssiy.tutorialmod.entity.NueEntity;
import com.aleksiyflekssiy.tutorialmod.entity.Shikigami;
import com.aleksiyflekssiy.tutorialmod.entity.ToadEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class Toad extends ShikigamiSkill {
    private ToadEntity toad;

    public Toad(){
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
        if (!isActive) {
            System.out.println("ACTIVATE");
            BlockPos spawnPos = entity.blockPosition();
            if (toad == null || !toad.isAlive()) { // Проверяем, жива ли сущность
                toad = new ToadEntity(ModEntities.TOAD.get(), entity.level());
                toad.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                entity.level().addFreshEntity(toad);
            }
            if (isTamed){
                toad.tame((Player) entity);
            }
        }
        else if(isActive && isTamed) {
            if (toad.getControllingPassenger() == null) {
                System.out.println("DEACTIVATE");
                toad.discard();
                toad = null;
            }
            else {
                return;
            }
        }
        isActive = !isActive;
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ToadEntity entity && entity.equals(toad)) {
            if (toad != null && !toad.isAlive()) {
                if (isTamed) {
                    isDead = true;
                    toad.getOwner().sendSystemMessage(Component.literal("Toad has died"));
                }
                else if (event.getSource().getEntity() instanceof Player player) {
                    if (!isTamed) {
                        isTamed = true;
                        System.out.println("TAMED");
                        player.sendSystemMessage(Component.literal("You tamed Toad"));
                    }
                }
                toad = null;
            }
            isActive = false;
        }
        //MinecraftForge.EVENT_BUS.unregister(this);
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
        return new ResourceLocation("tutorialmod", "textures/gui/toad.png");
    }
}
