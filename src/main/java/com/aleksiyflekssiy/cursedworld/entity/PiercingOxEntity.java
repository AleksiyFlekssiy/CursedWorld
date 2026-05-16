package com.aleksiyflekssiy.cursedworld.entity;

import com.aleksiyflekssiy.cursedworld.capability.CursedEnergyCapability;
import com.aleksiyflekssiy.cursedworld.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.cursedworld.cursed_technique.TenShadowsTechnique;
import com.aleksiyflekssiy.cursedworld.cursed_technique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.cursedworld.cursed_technique.skill.Skill;
import com.aleksiyflekssiy.cursedworld.cursed_technique.skill.tenshadows.PiercingOx;
import com.aleksiyflekssiy.cursedworld.entity.ai.PiercingOxAI;
import com.aleksiyflekssiy.cursedworld.entity.behavior.CustomMemoryModuleTypes;
import com.aleksiyflekssiy.cursedworld.entity.behavior.CustomSensorTypes;
import com.aleksiyflekssiy.cursedworld.registry.Skills;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

public class PiercingOxEntity extends Shikigami{

    protected static final ImmutableList<SensorType<? extends Sensor<? super PiercingOxEntity>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            CustomSensorTypes.SHIKIGAMI_OWNER_HURT.get(),
            CustomSensorTypes.SHIKIGAMI_OWNER_HURT_BY.get());

    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH,
            MemoryModuleType.ATTACK_TARGET, MemoryModuleType.WALK_TARGET,
            CustomMemoryModuleTypes.OWNER.get(), CustomMemoryModuleTypes.OWNER_HURT.get(),
            CustomMemoryModuleTypes.OWNER_HURT_BY_ENTITY.get(), MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.ATTACK_COOLING_DOWN);

    public PiercingOxEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public PiercingOxEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel, Player owner) {
        super(pEntityType, pLevel, owner);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 120)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.ATTACK_DAMAGE, 2f)
                .add(Attributes.FOLLOW_RANGE, 100)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 1)
                .add(Attributes.ARMOR_TOUGHNESS, 3)
                .add(Attributes.JUMP_STRENGTH, 1);
    }

    @Override
    protected Brain<PiercingOxEntity> makeBrain(Dynamic<?> pDynamic) {
        Brain<PiercingOxEntity> brain = this.brainProvider().makeBrain(pDynamic);
        return PiercingOxAI.makeBrain(brain);
    }

    @Override
    public Brain<PiercingOxEntity> getBrain() {
        return (Brain<PiercingOxEntity>) super.getBrain();
    }

    @Override
    protected Brain.Provider<PiercingOxEntity> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    public void tame(Player owner) {
        super.tame(owner);
        this.getBrain().setMemory(CustomMemoryModuleTypes.OWNER.get(), owner);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide){

        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
//        if (isTamed) {
//            owner.getCapability(CursedTechniqueCapability.CURSED_TECHNIQUE).ifPresent(technique ->{
//                if (technique.getTechnique() instanceof TenShadowsTechnique tenShadows){
//                    for (Skill skill : tenShadows.getSkillSet()){
//                        if (skill instanceof PiercingOx piercingOx){
//                            piercingOx.setShikigami(List.of(this));
//                            System.out.println("WORKED FOR OX");
//                        }
//                    }
//                }
//                else System.out.println("Not Ten Shadows");
//            });
//        }
//        else System.out.println("Not tamed");
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        PiercingOxAI.updateActivity(this.getBrain());
        this.getBrain().tick((ServerLevel) this.level(), this);
    }

    @Override
    public boolean followOrder(LivingEntity target, BlockPos blockPos, ShikigamiOrder order) {
        if (super.followOrder(target, blockPos, order)){
            this.getBrain().stopAll((ServerLevel) this.level(), this);
            if (order == ShikigamiOrder.ATTACK){
                this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);
            }
            else if (order == ShikigamiOrder.MOVE){
                this.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockPos, 1, 1));
            }
            return true;
        }
        return false;
    }

    @Override
    public void clearOrder() {
        this.setOrder(ShikigamiOrder.NONE);
        this.getBrain().stopAll((ServerLevel) this.level(), this);
    }

    @Override
    protected Skill getCorrespondingSkill() {
        return Skills.PIERCING_OX.get();
    }
}
