package com.aleksiyflekssiy.tutorialmod.network;

import com.aleksiyflekssiy.tutorialmod.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.*;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.limitless.Blue;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.limitless.HollowPurple;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.limitless.Infinity;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.limitless.Red;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HoldSkillPacket {
    private final String skillName;
    private final int charge;
    private final Type type;

    public HoldSkillPacket(String skillName, int charge, Type type) {
        this.skillName = skillName;
        this.charge = charge;
        this.type = type;
    }

    public static void encode(HoldSkillPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.skillName);
        buf.writeInt(msg.charge);
        buf.writeInt(msg.type.ordinal());
    }

    public static HoldSkillPacket decode(FriendlyByteBuf buf) {
        return new HoldSkillPacket(buf.readUtf(), buf.readInt(), Type.values()[buf.readInt()]);
    }

    public static void handle(HoldSkillPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                Skill activeSkill = CursedTechniqueCapability.getSkill(player);
                switch (msg.skillName) {
                    case "Infinity" -> {
                        Infinity infinity = (Infinity) activeSkill;
                        switch (msg.type) {
                            //case TICK -> infinity.activate(player, Skill.UseType.SECONDARY);
                            //case STOP -> infinity.disactivate(player, Skill.UseType.SECONDARY);
                        }
                    }
                    case "Blue" -> {
                        Blue blue = (Blue) activeSkill;
                        switch (msg.type) {
                            case TICK -> blue.charge(player, msg.charge);
                            case STOP -> blue.release(player);
                        }
                    }
                    case "Red" -> {
                        Red red = (Red) activeSkill;
                        switch (msg.type){
                            //case TICK -> red.chant(player, player.level(), msg.charge);
                            //case STOP -> red.stopUsing(player);
                        }
                    }
                    case "Hollow Purple" -> {
                        HollowPurple hollowPurple = (HollowPurple) activeSkill;
                        switch (msg.type) {
                            case TICK -> hollowPurple.onUsing(player, player.level());
                            case STOP -> hollowPurple.stopUsing(player, player.level());
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public enum Type {
        START,
        TICK,
        STOP
    }
}