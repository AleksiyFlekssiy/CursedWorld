package com.aleksiyflekssiy.cursedworld.network;

import com.aleksiyflekssiy.cursedworld.client.renderer.CustomDebugRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class AddAABBToRenderPacket {
    private final AABB aabb;
    private final boolean renderState;

    public AddAABBToRenderPacket(AABB aabb, boolean renderState) {
        this.aabb = aabb;
        this.renderState = renderState;
    }

    public static void encode(AddAABBToRenderPacket packet, FriendlyByteBuf buf){
        buf.writeDouble(packet.aabb.minX);
        buf.writeDouble(packet.aabb.minY);
        buf.writeDouble(packet.aabb.minZ);
        buf.writeDouble(packet.aabb.maxX);
        buf.writeDouble(packet.aabb.maxY);
        buf.writeDouble(packet.aabb.maxZ);
        buf.writeBoolean(packet.renderState);
    }

    public static AddAABBToRenderPacket decode(FriendlyByteBuf buf){
        return new AddAABBToRenderPacket(new AABB(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble()),
                buf.readBoolean()
        );
    }

    public static void handle(AddAABBToRenderPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            if (packet.renderState) {
                CustomDebugRenderer.AABB_LIST.keySet().forEach(CustomDebugRenderer::removeAABB);
                CustomDebugRenderer.addAABB(packet.aabb);
            }
            else CustomDebugRenderer.removeAABB(packet.aabb);
        });
        ctx.get().setPacketHandled(true);
    }
}
