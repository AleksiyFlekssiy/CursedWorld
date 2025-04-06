package com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.tenshadows;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.tutorialmod.entity.DivineDogEntity;
import com.aleksiyflekssiy.tutorialmod.entity.ModEntities;
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
public class DivineDogs extends ShikigamiSkill {
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
            if (isTamed){
                whiteDivineDog.tame((Player) entity);
                blackDivineDog.tame((Player) entity);
            }
        } else if(isActive && isTamed && areBothAlive()) {
            System.out.println("DEACTIVATE");
            whiteDivineDog.discard();
            whiteDivineDog = null;
            blackDivineDog.discard();
            blackDivineDog = null;
        }
        isActive = !isActive;
    }

    public boolean areBothDead(){
        if (whiteDivineDog != null && blackDivineDog != null) return !whiteDivineDog.isAlive() && !blackDivineDog.isAlive();
        return whiteDivineDog == null && blackDivineDog == null;
    }

    public boolean areBothAlive(){
        if (whiteDivineDog != null && blackDivineDog != null) return whiteDivineDog.isAlive() && blackDivineDog.isAlive();
        return false;
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof DivineDogEntity divineDogEntity && (divineDogEntity.equals(whiteDivineDog) || divineDogEntity.equals(blackDivineDog))) {
            if (blackDivineDog != null && !blackDivineDog.isAlive() && whiteDivineDog != null && !whiteDivineDog.isAlive()) {
                if (isTamed) {
                    isDead = true;
                    divineDogEntity.getOwner().sendSystemMessage(Component.literal("Your Divine Dogs have died"));
                }
                else if (event.getSource().getEntity() instanceof Player player) {
                    if (!isTamed) {
                        isTamed = true;
                        player.sendSystemMessage(Component.literal("You tamed Divine Dogs"));
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
    public List<Shikigami> getShikigami() {
        return List.of(whiteDivineDog, blackDivineDog);
    }

    @Override
    public void switchOrder(LivingEntity owner) {

    }

    @Override
    public String getName() {
        return "Divine Dogs";
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/gui/divine_dogs.png");
    }
}
