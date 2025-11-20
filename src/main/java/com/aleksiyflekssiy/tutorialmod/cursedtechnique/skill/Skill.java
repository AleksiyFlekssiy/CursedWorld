package com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Set;

public abstract class Skill {
    protected Set<Skill> subSkills;
    protected boolean isOnCooldown;
    protected int cooldownTicks;

    public abstract String getName();
    public abstract ResourceLocation getSkillIcon();

    public void use(LivingEntity entity, UseType type, int charge){}
    public void tick(Player player, Level level) {}
    public void startUsing(Player player, Level level) {}
    public void onUsing(Player player, Level level){}
    public void stopUsing(Player player, Level level){}
    public void activate(LivingEntity entity){}
    public void disactivate(LivingEntity entity){}
    public void charge(LivingEntity entity, int charge) {}
    public void release(LivingEntity entity){}

    public CompoundTag serializeNBT(){
        CompoundTag tag = new CompoundTag();
        return tag;
    }

    public void deserializeNBT(CompoundTag tag){}

    public enum UseType{
        ACTIVATION,
        DEACTIVATION,
        CHARGING,
        RELEASING
    }

    public int getCooldownDuration() {
        return cooldownTicks;
    }

    public boolean canUse(Player player) {
        CompoundTag data = player.getPersistentData();
        String cooldownKey = "cooldown_" + getName().toLowerCase();
        long lastUsed = data.getLong(cooldownKey);
        long currentTime = player.level().getGameTime();
        int cooldownTicks = getCooldownDuration();

        if (currentTime >= lastUsed + cooldownTicks) {
            return true;
        } else {
            int ticksLeft = (int) ((lastUsed + cooldownTicks) - currentTime);
            player.sendSystemMessage(Component.literal(getName() + " on cooldown for " + (ticksLeft / 20) + " seconds!"));
            return false;
        }
    }

    protected void setCooldown(Player player, int cd) {
        CompoundTag data = player.getPersistentData();
        String cooldownKey = "cooldown_" + getName().toLowerCase();
        data.putLong(cooldownKey, player.level().getGameTime());
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
}
