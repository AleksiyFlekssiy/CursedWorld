package com.aleksiyflekssiy.tutorialmod.network;

import com.aleksiyflekssiy.tutorialmod.capability.CursedEnergyCapability;
import com.aleksiyflekssiy.tutorialmod.client.renderer.VortexRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SkillRenderPacket {
    private UUID uuid;
    private boolean isActive;

    public SkillRenderPacket(UUID uuid, boolean isActive) {
        this.uuid = uuid;
        this.isActive = isActive;
    }

    public static void encode(SkillRenderPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.uuid);
        buffer.writeBoolean(packet.isActive);
    }

    public static SkillRenderPacket decode(FriendlyByteBuf buffer) {
        return new SkillRenderPacket(buffer.readUUID(), buffer.readBoolean());
    }

    public static void handle(SkillRenderPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null){
                if (packet.isActive){
                    VortexRenderer.addUser(packet.uuid);
                }
                else VortexRenderer.removeUser(packet.uuid);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
