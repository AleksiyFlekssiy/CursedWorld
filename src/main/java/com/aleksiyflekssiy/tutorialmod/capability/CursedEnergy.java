package com.aleksiyflekssiy.tutorialmod.capability;

import net.minecraft.nbt.CompoundTag;

public class CursedEnergy implements ICursedEnergy {
    private int cursedEnergy;
    private int maxCursedEnergy;
    private boolean fastTick;
    private int tickCounter;
    //регенерация будет раз в секунду (20 тиков)
    private int regenerationSpeed = 20;
    private int regenerationAmount = 1;

    public CursedEnergy(int cursedEnergy, int maxCursedEnergy) {
        this.cursedEnergy = cursedEnergy;
        this.maxCursedEnergy = maxCursedEnergy;
        this.tickCounter = 0;
    }

    @Override
    public int getCursedEnergy() {
        return this.cursedEnergy;
    }

    @Override
    public void setCursedEnergy(int cursedEnergy) {
        this.cursedEnergy = cursedEnergy;
    }

    @Override
    public int getMaxCursedEnergy() {
        return this.maxCursedEnergy;
    }

    @Override
    public void setMaxCursedEnergy(int maxCursedEnergy) {
        this.maxCursedEnergy = maxCursedEnergy;
    }

    @Override
    public int getRegenerationSpeed() {
        return this.regenerationSpeed;
    }

    @Override
    public void setRegenerationSpeed(int regenerationSpeed) {
        this.regenerationSpeed = regenerationSpeed;
    }

    @Override
    public int getRegenerationAmount() {
        return this.regenerationAmount;
    }

    @Override
    public void setRegenerationAmount(int regenerationAmount) {
        this.regenerationAmount = regenerationAmount;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("cursed_energy", getCursedEnergy());
        nbt.putInt("max_cursed_energy", getMaxCursedEnergy());
        nbt.putInt("regeneration_speed", getRegenerationSpeed());
        nbt.putInt("regeneration_amount", getRegenerationAmount());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        cursedEnergy = nbt.getInt("cursed_energy");
        maxCursedEnergy = nbt.getInt("max_cursed_energy");
        regenerationSpeed = nbt.getInt("regeneration_speed");
        regenerationAmount = nbt.getInt("regeneration_amount");
        System.out.println("CE: " + cursedEnergy + " Max CE: " + maxCursedEnergy + " Speed:  " + regenerationSpeed + " Amount: " + regenerationAmount);
    }

    public void tick() {
        if (fastTick) tickCounter += 5;
        else tickCounter++;
        if (tickCounter >= regenerationSpeed) {
            if (cursedEnergy < maxCursedEnergy) {
                cursedEnergy += regenerationAmount;
                if (cursedEnergy > maxCursedEnergy) {
                    cursedEnergy = maxCursedEnergy;
                }
            }
            tickCounter = 0;
        }
    }

    public boolean isFastTick() {
        return fastTick;
    }

    public void setFastTick(boolean fastTick) {
        this.fastTick = fastTick;
    }
}
