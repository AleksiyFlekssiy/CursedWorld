package com.aleksiyflekssiy.cursedworld.network;

import com.aleksiyflekssiy.cursedworld.client.renderer.CustomDebugRenderer;
import com.aleksiyflekssiy.cursedworld.phys.OBB;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AddOBBToRenderPacket {
    private final OBB obb;
    private final boolean renderState;

    public AddOBBToRenderPacket(OBB obb, boolean renderState) {
        this.obb = obb;
        this.renderState = renderState;
    }

    public static void encode(AddOBBToRenderPacket packet, FriendlyByteBuf buf){
        buf.writeDouble(packet.obb.getMinX());
        buf.writeDouble(packet.obb.getMinY());
        buf.writeDouble(packet.obb.getMinZ());
        buf.writeDouble(packet.obb.getMaxX());
        buf.writeDouble(packet.obb.getMaxY());
        buf.writeDouble(packet.obb.getMaxZ());
        buf.writeDouble(packet.obb.getPitch());
        buf.writeDouble(packet.obb.getYaw());
        buf.writeDouble(packet.obb.getRoll());
        buf.writeBoolean(packet.renderState);
    }

    public static AddOBBToRenderPacket decode(FriendlyByteBuf buf){
        return new AddOBBToRenderPacket(new OBB(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble()),
                buf.readBoolean()
        );
    }

    public static void handle(AddOBBToRenderPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            if (packet.renderState) {
                CustomDebugRenderer.OBB_LIST.keySet().forEach(CustomDebugRenderer::removeOBB);
                CustomDebugRenderer.addOBB(packet.obb);
            }

            else {
                CustomDebugRenderer.removeOBB(packet.obb);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
