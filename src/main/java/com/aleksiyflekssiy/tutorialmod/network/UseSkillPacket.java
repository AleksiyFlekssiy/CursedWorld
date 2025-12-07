package com.aleksiyflekssiy.tutorialmod.network;

import com.aleksiyflekssiy.tutorialmod.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.*;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.limitless.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UseSkillPacket {
    private final String skillName;
    private final Skill.UseType useType;
    private final int charge;

    public UseSkillPacket(String skillName, Skill.UseType useType, int charge) {
        this.skillName = skillName;
        this.useType = useType;
        this.charge = charge;
    }

    public static void encode(UseSkillPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.skillName);
        buf.writeInt(msg.useType.ordinal());
        buf.writeInt(msg.charge);
    }

    public static UseSkillPacket decode(FriendlyByteBuf buf) {
        return new UseSkillPacket(buf.readUtf(), Skill.UseType.values()[buf.readInt()], buf.readInt());
    }

    public static void handle(UseSkillPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                Skill activeSkill = CursedTechniqueCapability.getCurrentSkill(player);
                activeSkill.use(player, msg.useType, msg.charge);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}