package com.aleksiyflekssiy.tutorialmod.network;
import com.aleksiyflekssiy.tutorialmod.capability.CursedTechniqueCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncSkillPacket {
    private final String skillName;

    public SyncSkillPacket(String skillName) {
        this.skillName = skillName;
    }

    public static void encode(SyncSkillPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.skillName);
    }

    public static SyncSkillPacket decode(FriendlyByteBuf buf) {
        return new SyncSkillPacket(buf.readUtf());
    }

    public static void handle(SyncSkillPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(CursedTechniqueCapability.CURSED_TECHNIQUE).ifPresent(technique -> {
                    technique.getSkillSet().stream()
                            .filter(skill -> skill.getName().equals(msg.skillName))
                            .findFirst()
                            .ifPresent(technique::setCurrentSkill);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
