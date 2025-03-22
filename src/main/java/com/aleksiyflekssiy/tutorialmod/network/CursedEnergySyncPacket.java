package com.aleksiyflekssiy.tutorialmod.network;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.capability.CursedEnergyCapability;
import com.aleksiyflekssiy.tutorialmod.capability.ICursedEnergy;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CursedEnergySyncPacket {
    private final int cursedEnergy;

    public CursedEnergySyncPacket(int cursedEnergy) {
        this.cursedEnergy = cursedEnergy;
    }

    public static void encode(CursedEnergySyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.cursedEnergy);
    }

    public static CursedEnergySyncPacket decode(FriendlyByteBuf buffer) {
        return new CursedEnergySyncPacket(buffer.readInt());
    }

    public static void handle(CursedEnergySyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.getCapability(CursedEnergyCapability.CURSED_ENERGY).ifPresent(energy -> {
                    energy.setCursedEnergy(packet.cursedEnergy);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static void updateToClient(ICursedEnergy energy, Player player) {
        TutorialMod.NETWORK.sendTo(new CursedEnergySyncPacket(energy.getCursedEnergy()), ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
