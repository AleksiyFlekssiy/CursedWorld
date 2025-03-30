package com.aleksiyflekssiy.tutorialmod.cursed_technique.skill;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.entity.DivineDogEntity;
import com.aleksiyflekssiy.tutorialmod.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DivineDogs extends Skill {
    private boolean isActive = false;
    private boolean areTamed = false;
    private boolean isDead = false;
    private DivineDogEntity whiteDivineDog = null;
    private DivineDogEntity blackDivineDog = null;

    public DivineDogs() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void use(LivingEntity entity, UseType type, int charge) {
        System.out.println("USING");
        switch (type) {
            case ACTIVATION -> {
                this.activate(entity);
            }
        }
    }

    @Override
    public void activate(LivingEntity entity) {
        if (isDead) {
            System.out.println("DEAD");
            return;
        }
        if (!isActive && areBothDead()) {
            System.out.println("ACTIVATE");
            BlockPos spawnPos = entity.blockPosition();
            if (whiteDivineDog == null || !whiteDivineDog.isAlive()) { // Проверяем, жива ли сущность
                    whiteDivineDog = new DivineDogEntity(ModEntities.DIVINE_DOG.get(), entity.level());
                    whiteDivineDog.setColor(DivineDogEntity.Color.WHITE);
                    whiteDivineDog.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                    entity.level().addFreshEntity(whiteDivineDog);
            }
            if (blackDivineDog == null || !blackDivineDog.isAlive()) {
                blackDivineDog = new DivineDogEntity(ModEntities.DIVINE_DOG.get(), entity.level());
                blackDivineDog.setColor(DivineDogEntity.Color.BLACK);
                blackDivineDog.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                entity.level().addFreshEntity(blackDivineDog);
            }
            if (areTamed){
                whiteDivineDog.tame((Player) entity);
                blackDivineDog.tame((Player) entity);
            }
        } else if(isActive && areTamed && areBothAlive()) {
            System.out.println("DEACTIVATE");
            whiteDivineDog.discard();
            whiteDivineDog = null;
            blackDivineDog.discard();
            blackDivineDog = null;
        }
        isActive = !isActive;
    }

    private boolean areBothDead(){
        if (whiteDivineDog != null && blackDivineDog != null) return !whiteDivineDog.isAlive() && !blackDivineDog.isAlive();
        return whiteDivineDog == null && blackDivineDog == null;
    }

    private boolean areBothAlive(){
        if (whiteDivineDog != null && blackDivineDog != null) return whiteDivineDog.isAlive() && blackDivineDog.isAlive();
        return false;
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof DivineDogEntity divineDogEntity && (divineDogEntity.equals(whiteDivineDog) || divineDogEntity.equals(blackDivineDog))) {
            if (blackDivineDog != null && !blackDivineDog.isAlive() && whiteDivineDog != null && !whiteDivineDog.isAlive()) {
                if (areTamed) {
                    isDead = true;
                    System.out.println("DEAD");
                }
                else if (event.getSource().getEntity() instanceof Player) {
                    if (!areTamed) {
                        areTamed = true;
                        System.out.println("TAMED");
                    }
                }
                whiteDivineDog = null;
                blackDivineDog = null;
            }
            isActive = false;
        }
        //MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public String getName() {
        return "Divine Dogs";
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return new ResourceLocation("tutorialmod", "textures/gui/divine_dogs.png");
    }
}
