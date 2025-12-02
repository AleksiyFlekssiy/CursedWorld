package com.aleksiyflekssiy.tutorialmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class InputLockPacket {
    private final boolean lock;
    private final float yaw;
    private final float pitch;

    public InputLockPacket(boolean lock,  float yaw, float pitch) {
        this.lock = lock;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static void encode(InputLockPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.lock);
        buf.writeFloat(packet.yaw);
        buf.writeFloat(packet.pitch);
    }

    public static InputLockPacket decode(FriendlyByteBuf buf) {
        return new InputLockPacket(buf.readBoolean(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(InputLockPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            Player localPlayer = mc.player;
            if (localPlayer != null){
                CompoundTag compoundTag = localPlayer.getPersistentData();
                compoundTag.putBoolean("lock", packet.lock);
                compoundTag.putFloat("yaw", packet.yaw);
                compoundTag.putFloat("pitch", packet.pitch);
            }
        });
    }
}
