package com.aleksiyflekssiy.tutorialmod.network;

import com.aleksiyflekssiy.tutorialmod.capability.CursedEnergyCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class CursedEnergySyncPacket {
    private final CompoundTag cursedEnergy;

    public CursedEnergySyncPacket(CompoundTag cursedEnergy) {
        this.cursedEnergy = cursedEnergy;
    }

    public static void encode(CursedEnergySyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.cursedEnergy);
    }

    public static CursedEnergySyncPacket decode(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        return new CursedEnergySyncPacket(tag);
    }

    public static void handle(CursedEnergySyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.getCapability(CursedEnergyCapability.CURSED_ENERGY).ifPresent(energy -> {
                    energy.setCursedEnergy(packet.cursedEnergy.getInt("cursed_energy"));
                    energy.setMaxCursedEnergy(packet.cursedEnergy.getInt("max_cursed_energy"));
                    energy.setRegenerationAmount(packet.cursedEnergy.getInt("regeneration_amount"));
                    energy.setRegenerationSpeed(packet.cursedEnergy.getInt("regeneration_speed"));
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static void updateToClient(CompoundTag tag, Entity player) {
        ModMessages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new CursedEnergySyncPacket(tag));
    }
}
