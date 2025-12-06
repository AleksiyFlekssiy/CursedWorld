package com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill;

import com.aleksiyflekssiy.tutorialmod.capability.CursedEnergyCapability;
import com.aleksiyflekssiy.tutorialmod.config.ModConfig;
import com.aleksiyflekssiy.tutorialmod.util.AdaptationUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

public abstract class Skill {
    protected Set<Skill> subSkills;
    protected boolean isOnCooldown;
    protected int cooldownTicks;

    public abstract String getName();
    public abstract ResourceLocation getSkillIcon();

    public void use(LivingEntity entity, UseType type, int charge){}
    public void activate(LivingEntity entity){}
    public void deactivate(LivingEntity entity){}
    public void charge(LivingEntity entity, int charge) {}
    public void release(LivingEntity entity){}

    public boolean spendCursedEnergy(LivingEntity entity, int amount){
        if (ModConfig.SERVER.NO_CE_COST.get() == true) {
            System.out.println("NOT SPEND CE");
            return true;
        }
        if (entity.getCapability(CursedEnergyCapability.CURSED_ENERGY).isPresent()){
            if (CursedEnergyCapability.isEnoughEnergy(entity, amount)){
                CursedEnergyCapability.setCursedEnergy(entity, CursedEnergyCapability.getCursedEnergy(entity) - amount);
                System.out.println("SPEND CE");
                return true;
            }
        }
        return false;
    }

    public boolean chant(LivingEntity entity, int charge){
        return false;
    }

    public boolean canAffect(Entity entity){
        if (entity instanceof Player player) {
            if (!player.isSpectator() && !player.isCreative()) return !AdaptationUtil.checkAdaptation(this, player);
            else return false;
        }
        else if (entity instanceof LivingEntity livingEntity) {
            return !AdaptationUtil.checkAdaptation(this, livingEntity);
        }
        else return true;
    }

    public CompoundTag save(){
        CompoundTag tag = new CompoundTag();
        tag.putString("skill_name", this.getName());
        saveAdditional(tag);
        return tag;
    }

    public void load(CompoundTag tag){
        loadAdditional(tag);
    }

    protected void saveAdditional(CompoundTag tag){}
    protected void loadAdditional(CompoundTag tag){}

    public enum UseType{
        ACTIVATION,
        DEACTIVATION,
        CHARGING,
        RELEASING
    }

    public int getCooldownDuration() {
        return cooldownTicks;
    }

    public boolean canUse(Entity entity) {
        CompoundTag data = entity.getPersistentData();
        String cooldownKey = "cooldown_" + getName().toLowerCase();
        long lastUsed = data.getLong(cooldownKey);
        long currentTime = entity.level().getGameTime();
        int cooldownTicks = getCooldownDuration();

        if (currentTime >= lastUsed + cooldownTicks) {
            return true;
        } else {
            int ticksLeft = (int) ((lastUsed + cooldownTicks) - currentTime);
            entity.sendSystemMessage(Component.literal(getName() + " on cooldown for " + (ticksLeft / 20) + " seconds!"));
            return false;
        }
    }

    public void setCooldown(Entity entity, int cd) {
        CompoundTag data = entity.getPersistentData();
        String cooldownKey = "cooldown_" + getName().toLowerCase();
        data.putLong(cooldownKey, entity.level().getGameTime());
        this.cooldownTicks = cd;
    }

    public int getRemainingCooldown(Player player) {
        CompoundTag data = player.getPersistentData();
        String cooldownKey = "cooldown_" + getName().toLowerCase();
        long lastUsed = data.getLong(cooldownKey);
        long currentTime = player.level().getGameTime();
        int cooldownTicks = getCooldownDuration();
        int ticksLeft = (int) ((lastUsed + cooldownTicks) - currentTime);
        return Math.max(0, ticksLeft);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Skill other)) return false;
        return this.getName().equals(other.getName());
    }
}
