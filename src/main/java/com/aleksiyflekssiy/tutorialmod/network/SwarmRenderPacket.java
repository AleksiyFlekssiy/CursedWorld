package com.aleksiyflekssiy.tutorialmod.network;

import com.aleksiyflekssiy.tutorialmod.client.renderer.RabbitSwarmRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SwarmRenderPacket {
    private final int id;
    private final boolean isActive;
    private final boolean selfUse;

    public SwarmRenderPacket(int id, boolean isActive, boolean selfUse){
        this.id = id;
        this.isActive = isActive;
        this.selfUse = selfUse;
    }

    public static void encode(SwarmRenderPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.id);
        buffer.writeBoolean(packet.isActive);
        buffer.writeBoolean(packet.selfUse);
    }

    public static SwarmRenderPacket decode(FriendlyByteBuf buffer) {
        return new SwarmRenderPacket(buffer.readInt(), buffer.readBoolean(), buffer.readBoolean());
    }

    public static void handle(SwarmRenderPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null){
                if (packet.isActive) RabbitSwarmRenderer.addUser(packet.id, packet.selfUse);
                else RabbitSwarmRenderer.removeUser(packet.id);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
