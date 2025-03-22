package com.aleksiyflekssiy.tutorialmod.capability;

import com.aleksiyflekssiy.tutorialmod.network.CursedEnergySyncPacket;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CursedEnergyCapability {
    public static final Capability<ICursedEnergy> CURSED_ENERGY = CapabilityManager.get(new CapabilityToken<>() {});

    public static int getCursedEnergy(Player player) {
        return player.getCapability(CURSED_ENERGY)
                .map(ICursedEnergy::getCursedEnergy)
                .orElse(0);
    }

    public static void setCursedEnergy(Player player, int energy) {
        player.getCapability(CURSED_ENERGY).ifPresent(e -> {
            e.setCursedEnergy(energy);
            CursedEnergySyncPacket.updateToClient(e, player);
        });
    }

    public static boolean isEnoughEnergy(Player player, int amount) {
        return getCursedEnergy(player) >= amount;
    }

    public static class Provider implements ICapabilitySerializable<CompoundTag>{
        private final ICursedEnergy cursedEnergy = new CursedEnergy(100);
        private final LazyOptional<ICursedEnergy> holder = LazyOptional.of(() -> cursedEnergy);

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side){
            return capability == CURSED_ENERGY ? holder.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("cursed_energy", cursedEnergy.getCursedEnergy());
            nbt.putInt("tick_counter", ((CursedEnergy) cursedEnergy).getTickCounter()); // Сохраняем счётчик
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            cursedEnergy.setCursedEnergy(nbt.getInt("cursed_energy"));
            ((CursedEnergy) cursedEnergy).setTickCounter(nbt.getInt("tick_counter")); // Загружаем счётчик
        }
    }
}
