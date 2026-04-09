package com.aleksiyflekssiy.tutorialmod.network;

import com.aleksiyflekssiy.tutorialmod.client.renderer.RabbitSwarmRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class SkillRenderPacket {
    private final UUID uuid;
    private final boolean isActive;

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
                    System.out.println(Objects.requireNonNull(player.level().getPlayerByUUID(packet.uuid)).getDisplayName() + " is charging");
                    RabbitSwarmRenderer.addUser(packet.uuid);
                }
                else {
                    System.out.println(Objects.requireNonNull(player.level().getPlayerByUUID(packet.uuid)).getDisplayName() + " is releasing");
                    RabbitSwarmRenderer.removeUser(packet.uuid);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
