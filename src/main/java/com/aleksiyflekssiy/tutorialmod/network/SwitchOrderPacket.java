package com.aleksiyflekssiy.tutorialmod.network;

import com.aleksiyflekssiy.tutorialmod.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill.ShikigamiSkill;
import io.netty.buffer.ByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SwitchOrderPacket {
    private final int direction;

    public SwitchOrderPacket(int direction) {
        this.direction = direction;
    }

    public static void encode(SwitchOrderPacket msg, ByteBuf buf) {
        buf.writeInt(msg.direction);
    }

    public static SwitchOrderPacket decode(ByteBuf buf) {
        return new SwitchOrderPacket(buf.readInt());
    }

    public static void handle(SwitchOrderPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ShikigamiSkill shikigamiSkill = (ShikigamiSkill) CursedTechniqueCapability.getCurrentSkill(player);
                shikigamiSkill.switchOrder(player, msg.direction);
            }
        });
    }
}
