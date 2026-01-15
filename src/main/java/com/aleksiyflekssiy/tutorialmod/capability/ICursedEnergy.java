package com.aleksiyflekssiy.tutorialmod.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;

@AutoRegisterCapability
public interface ICursedEnergy extends INBTSerializable<CompoundTag> {
    int getCursedEnergy();
    void setCursedEnergy(int cursedEnergy);
    int getMaxCursedEnergy();
    void setMaxCursedEnergy(int maxCursedEnergy);
    int getRegenerationSpeed();
    void setRegenerationSpeed(int regenerationSpeed);
    int getRegenerationAmount();
    void setRegenerationAmount(int regenerationAmount);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag tag);
}
