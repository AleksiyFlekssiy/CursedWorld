package com.aleksiyflekssiy.tutorialmod.capability;

import com.aleksiyflekssiy.tutorialmod.network.CursedEnergySyncPacket;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CursedEnergyCapability {
    public static final Capability<ICursedEnergy> CURSED_ENERGY = CapabilityManager.get(new CapabilityToken<>() {});

    public static int getCursedEnergy(Entity entity) {
        return entity.getCapability(CURSED_ENERGY)
                .map(ICursedEnergy::getCursedEnergy)
                .orElse(0);
    }

    public static void setCursedEnergy(Entity entity, int energy) {
        entity.getCapability(CURSED_ENERGY).ifPresent(e -> {
            e.setCursedEnergy(energy);
            CursedEnergySyncPacket.updateToClient(e.serializeNBT(), entity);
        });
    }

    public static int getMaxCursedEnergy(Entity entity) {
        return entity.getCapability(CURSED_ENERGY)
                .map(ICursedEnergy::getMaxCursedEnergy)
                .orElse(0);
    }

    public static void setMaxCursedEnergy(Entity entity, int energy) {
        entity.getCapability(CURSED_ENERGY).ifPresent(e -> {
            e.setMaxCursedEnergy(energy);
            CursedEnergySyncPacket.updateToClient(e.serializeNBT(), entity);
        });
    }

    public static int getRegenerationAmount(Entity entity) {
        return entity.getCapability(CURSED_ENERGY)
                .map(ICursedEnergy::getRegenerationAmount)
                .orElse(0);
    }

    public static void setRegenerationAmount(Entity entity, int energy) {
        entity.getCapability(CURSED_ENERGY).ifPresent(e -> {
            e.setRegenerationAmount(energy);
            CursedEnergySyncPacket.updateToClient(e.serializeNBT(), entity);
        });
    }public static int getRegenerationSpeed(Entity entity) {
        return entity.getCapability(CURSED_ENERGY)
                .map(ICursedEnergy::getRegenerationSpeed)
                .orElse(0);
    }

    public static void setRegenerationSpeed(Entity entity, int energy) {
        entity.getCapability(CURSED_ENERGY).ifPresent(e -> {
            e.setRegenerationSpeed(energy);
            CursedEnergySyncPacket.updateToClient(e.serializeNBT(), entity);
        });
    }

    public static boolean isEnoughEnergy(Entity entity, int amount) {
        return getCursedEnergy(entity) >= amount;
    }

    public static class Provider implements ICapabilitySerializable<CompoundTag>{
        private final ICursedEnergy cursedEnergy = new CursedEnergy(0,100);
        private final LazyOptional<ICursedEnergy> holder = LazyOptional.of(() -> cursedEnergy);

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side){
            return capability == CURSED_ENERGY ? holder.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return cursedEnergy.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            cursedEnergy.deserializeNBT(nbt);
        }
    }
}
