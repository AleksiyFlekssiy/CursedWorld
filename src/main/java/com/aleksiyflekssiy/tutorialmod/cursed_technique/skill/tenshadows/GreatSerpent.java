package com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.tenshadows;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.tutorialmod.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.function.Consumer;

public class GreatSerpent extends ShikigamiSkill {
    private GreatSerpentEntity greatSerpent = null;
    private int orderIndex = 0;

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
                if (isTamed) {
                    greatSerpent.tame((Player) entity);
                    if (orderIndex != 0){
                        setTarget(entity,
                                blockPos -> {
                                    greatSerpent.followOrder(null, blockPos, GreatSerpentEntity.GreatSerpentOrder.values()[orderIndex]);
                                },
                                livingEntity -> {
                                    greatSerpent.followOrder(livingEntity, null, GreatSerpentEntity.GreatSerpentOrder.values()[orderIndex]);
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
                                //greatSerpent.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockPos, 1, 1));
                        greatSerpent.calculateAndSetSegmentCount(blockPos);
                    },
                            livingEntity -> {
                        //greatSerpent.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(livingEntity, 1, 1));
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
                                greatSerpent.followOrder(null, blockPos, GreatSerpentEntity.GreatSerpentOrder.values()[orderIndex]);
                            },
                            livingEntity -> {
                                greatSerpent.followOrder(livingEntity, null, GreatSerpentEntity.GreatSerpentOrder.values()[orderIndex]);
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
        if (isTamed && isActive && greatSerpent.getOrder() == GreatSerpentEntity.GreatSerpentOrder.SMASH) {
            setTarget(entity,
                    blockPos -> greatSerpent.followOrder(null, blockPos, GreatSerpentEntity.GreatSerpentOrder.values()[orderIndex]),
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
            isActive = false;
        }
    }

    private void setTarget(LivingEntity owner, Consumer<BlockPos> blockAction, Consumer<LivingEntity> entityAction) {
        HitResult result = ProjectileUtil.getHitResultOnViewVector(owner, target -> !target.equals(owner), 100);
        if (result.getType() == HitResult.Type.ENTITY && entityAction != null) {
            EntityHitResult hitResult = (EntityHitResult) result;
            if (hitResult.getEntity() instanceof LivingEntity target) {
                entityAction.accept(target);
            }
        } else if (result.getType() == HitResult.Type.BLOCK && blockAction != null) {
            BlockHitResult hitResult = (BlockHitResult) result;
            System.out.println(hitResult.getBlockPos());
            blockAction.accept(hitResult.getBlockPos());
        }
        else if (result.getType() == HitResult.Type.MISS && blockAction != null) {
            blockAction.accept(BlockPos.containing(result.getLocation()));
        };
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
    public void setShikigami(List<Shikigami> shikigamiList) {
        if (this.greatSerpent == null && shikigamiList.get(0) instanceof GreatSerpentEntity greatSerpentEntity) this.greatSerpent = greatSerpentEntity;
    }

    @Override
    public void switchOrder(LivingEntity owner, int direction) {
        if (isTamed) {
            switch (direction){
                case -1 -> {
                    if (--orderIndex <= -1) orderIndex = 4;
                }
                case 1 -> {
                    if (++orderIndex >= 5) orderIndex = 0;
                }
            }
            switch (orderIndex) {
                case 0 -> owner.sendSystemMessage(Component.literal("NONE"));
                case 1 -> owner.sendSystemMessage(Component.literal("MOVE"));
                case 2 -> owner.sendSystemMessage(Component.literal("CATCH"));
                case 3 -> owner.sendSystemMessage(Component.literal("SMASH"));
                case 4 -> owner.sendSystemMessage(Component.literal("THROW"));
            }
        }
    }

    @Override
    public String getName() {
        return "great_serpent";
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "textures/gui/great_serpent.png");
    }
}
