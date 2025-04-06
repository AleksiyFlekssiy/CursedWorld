package com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.tenshadows;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.tutorialmod.entity.ModEntities;
import com.aleksiyflekssiy.tutorialmod.entity.NueEntity;
import com.aleksiyflekssiy.tutorialmod.entity.Shikigami;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Nue extends ShikigamiSkill {
    private NueEntity nueEntity = null;

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
        if (!isActive) {
            System.out.println("ACTIVATE");
            BlockPos spawnPos = entity.blockPosition();
            if (nueEntity == null || !nueEntity.isAlive()) { // Проверяем, жива ли сущность
                nueEntity = new NueEntity(ModEntities.NUE.get(), entity.level());
                nueEntity.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                entity.level().addFreshEntity(nueEntity);
            }
            if (isTamed){
                nueEntity.tame((Player) entity);
            }
        }
        else if(isActive && isTamed) {
            if (nueEntity.getControllingPassenger() == null) {
                System.out.println("DEACTIVATE");
                nueEntity.discard();
                nueEntity = null;
            }
            else {
                nueEntity.tryGrabEntityBelow();
                return;
            }
        }
        isActive = !isActive;
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof NueEntity entity && entity.equals(nueEntity)) {
            if (nueEntity != null && !nueEntity.isAlive()) {
                if (isTamed) {
                    isDead = true;
                    nueEntity.getOwner().sendSystemMessage(Component.literal("Nue has died"));
                }
                else if (event.getSource().getEntity() instanceof Player player) {
                    if (!isTamed) {
                        isTamed = true;
                        System.out.println("TAMED");
                        player.sendSystemMessage(Component.literal("You tamed Nue"));
                    }
                }
                nueEntity = null;
            }
            isActive = false;
        }
        //MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public List<Shikigami> getShikigami() {
        return List.of(nueEntity);
    }

    @Override
    public void switchOrder(LivingEntity owner) {

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
