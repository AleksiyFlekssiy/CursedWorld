package com.aleksiyflekssiy.tutorialmod.network;

import com.aleksiyflekssiy.tutorialmod.capability.CursedTechniqueCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TechniqueSyncPacket {
    private final CompoundTag techniqueData;

    public TechniqueSyncPacket(CompoundTag techniqueData) {
        this.techniqueData = techniqueData;
    }

    public static void encode(TechniqueSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.techniqueData);
    }

    public static TechniqueSyncPacket decode(FriendlyByteBuf buffer) {
        return new TechniqueSyncPacket(buffer.readNbt());
    }

    public static void handle(TechniqueSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player clientPlayer = Minecraft.getInstance().player;
            if (clientPlayer != null) {
                clientPlayer.getCapability(CursedTechniqueCapability.CURSED_TECHNIQUE).ifPresent(technique -> {
                    technique.deserializeNBT(packet.techniqueData);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
