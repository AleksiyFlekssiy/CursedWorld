package com.aleksiyflekssiy.tutorialmod.network;

import com.aleksiyflekssiy.tutorialmod.capability.CursedTechniqueCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TechniqueSyncPacket {
    private final CompoundTag techniqueData;

    public TechniqueSyncPacket(CompoundTag techniqueData) {
        this.techniqueData = techniqueData;
    }

    public static void encode(TechniqueSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.techniqueData);
    }

    public static TechniqueSyncPacket decode(FriendlyByteBuf buffer) {
        return new TechniqueSyncPacket(buffer.readNbt());
    }

    public static void handle(TechniqueSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player clientPlayer = Minecraft.getInstance().player;
            CompoundTag tag = packet.techniqueData;
            System.out.println("Client NBT: "+tag);
            if (clientPlayer != null) {
                clientPlayer.getCapability(CursedTechniqueCapability.CURSED_TECHNIQUE).ifPresent(technique -> {
                    technique.deserializeNBT(packet.techniqueData);

//                    ListTag skillsTag = tag.getList("skills", ListTag.TAG_COMPOUND);
//                    for (int i = 0; i < skillsTag.size(); i++) {
//                        CompoundTag skillTag = skillsTag.getCompound(i);
//                        String skillName = skillTag.getString("skill_name");
//                        if (skillName.equals("GreatSerpent")){
//                            System.out.println("Is active: " + skillTag.getBoolean("isActive"));
//                            System.out.println("Is tamed: " + skillTag.getBoolean("isTamed"));
//                            System.out.println("Is dead: " + skillTag.getBoolean("isDead"));
//                        }
//                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
