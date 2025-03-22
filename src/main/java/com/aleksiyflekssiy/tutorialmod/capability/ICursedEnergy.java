package com.aleksiyflekssiy.tutorialmod.capability;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface ICursedEnergy {
    int getCursedEnergy();
    void setCursedEnergy(int cursedEnergy);
    int getMaxCursedEnergy();
}
