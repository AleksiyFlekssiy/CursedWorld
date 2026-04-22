package com.aleksiyflekssiy.cursedworld.cursed_technique.skill.tenshadows;

import com.aleksiyflekssiy.cursedworld.CursedWorld;
import com.aleksiyflekssiy.cursedworld.cursed_technique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.cursedworld.entity.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
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
            case CHARGING -> this.charge(entity, charge);
        }
    }

    public void setShikigami(GreatSerpentEntity greatSerpent){
        if (this.greatSerpent == null) {
            this.greatSerpent = greatSerpent;
            System.out.println("The Great Serpent has set");
        }
    }

    public List<Shikigami> getShikigami() {
        return List.of(greatSerpent);
    }

    @Override
    public void activate(LivingEntity entity) {
        if (isDead){
            entity.sendSystemMessage(Component.literal("Your Great Serpent is dead"));
            return;
        }

        if (!entity.isCrouching()) {
            if (!isActive) {
                if (shikigamiUUIDList.isEmpty()) greatSerpent = new GreatSerpentEntity(ModEntities.GREAT_SERPENT.get(), entity.level(), (Player) entity);
                shikigamiUUIDList.add(greatSerpent.getUUID());
                shikigamiList.add(greatSerpent);
                if (isTamed) {
                    greatSerpent.tame((Player) entity);
                    if (orderIndex != 0){
                        setTarget(entity,
                                blockPos -> {
                                    greatSerpent.followOrder(null, blockPos, this.getOrders().get(orderIndex));
                                },
                                livingEntity -> {
                                    greatSerpent.followOrder(livingEntity, null, this.getOrders().get(orderIndex));
                                });
                        if (greatSerpent.isAddedToWorld()){
                            isActive = true;
                        }
                        return;
                    }
                }
                if (greatSerpent.positionSet) {
                    setTarget(entity,
                            blockPos -> {
                                greatSerpent.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockPos, 1, 1));
                        greatSerpent.calculateAndSetSegmentCount(blockPos);
                    },
                            livingEntity -> {
                        greatSerpent.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(livingEntity, 1, 1));
                        greatSerpent.calculateAndSetSegmentCount(livingEntity.blockPosition());
                            });
                    entity.level().addFreshEntity(greatSerpent);
                    isActive = true;
                }
                else {
                    setTarget(entity,
                            blockPos -> greatSerpent.setSpawnPos(blockPos),
                            livingEntity -> greatSerpent.setSpawnPos(livingEntity.blockPosition()));
                    greatSerpent.positionSet = true;
                    greatSerpent.getOwner().sendSystemMessage(Component.literal("Position set"));
                }
            }
            else if (isTamed){
                if (isActive) {
                    setTarget(entity,
                            blockPos -> {
                                greatSerpent.followOrder(null, blockPos, this.getOrders().get(orderIndex));
                            },
                            livingEntity -> {
                                greatSerpent.followOrder(livingEntity, null, this.getOrders().get(orderIndex));
                            });
                }
            }
        }
        else {
            deactivate(entity);
        }
    }

    @Override
    public void charge(LivingEntity entity, int charge) {
        if (isTamed && isActive && greatSerpent.getOrder() == ShikigamiOrder.SMASH) {
            setTarget(entity,
                    blockPos -> greatSerpent.followOrder(null, blockPos, this.getOrders().get(orderIndex)),
                    livingEntity -> {});
        }
    }

    @Override
    public void deactivate(LivingEntity entity) {
        if (isActive && isTamed) {
            if (greatSerpent != null && greatSerpent.isAlive()) {
                greatSerpent.discard();
            }
            greatSerpent = null;
            shikigamiUUIDList.clear();
            shikigamiList.clear();
            isActive = false;
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof GreatSerpentEntity greatSerpentEntity && !greatSerpentEntity.level().isClientSide){
            if (greatSerpent != null && greatSerpent.equals(greatSerpentEntity)){
                if (isTamed){
                    isDead = true;
                    greatSerpent.getOwner().sendSystemMessage(Component.literal("Your Great Serpent has died"));
                }
                else if (!isTamed && event.getSource().getEntity() instanceof Player player && player.equals(greatSerpent.getOwner())){
                    isTamed = true;
                    player.sendSystemMessage(Component.literal("You have tamed Great Serpent"));
                }
                isActive = false;
                greatSerpent = null;
                shikigamiUUIDList.clear();
            }
        }
    }

    @Override
    public List<ShikigamiOrder> getOrders() {
        return List.of(
                ShikigamiOrder.NONE,
                ShikigamiOrder.CATCH,
                ShikigamiOrder.SMASH,
                ShikigamiOrder.THROW,
                ShikigamiOrder.MOVE
        );
    }

    @Override
    public void setShikigami(List<Shikigami> shikigamiList) {
        if (this.greatSerpent == null && shikigamiList.get(0) instanceof GreatSerpentEntity greatSerpentEntity) this.greatSerpent = greatSerpentEntity;
    }


    @Override
    public String getName() {
        return "great_serpent";
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return ResourceLocation.fromNamespaceAndPath(CursedWorld.MOD_ID, "textures/gui/great_serpent.png");
    }
}
