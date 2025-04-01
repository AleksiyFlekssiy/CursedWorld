package com.aleksiyflekssiy.tutorialmod.network;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TutorialMod.MOD_ID, "main1"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, UseSkillPacket.class,
                UseSkillPacket::encode,
                UseSkillPacket::decode,
                UseSkillPacket::handle);

        INSTANCE.registerMessage(id++, HoldSkillPacket.class,
                HoldSkillPacket::encode,
                HoldSkillPacket::decode,
                HoldSkillPacket::handle);

        INSTANCE.registerMessage(id++, SyncSkillPacket.class,
                SyncSkillPacket::encode,
                SyncSkillPacket::decode,
                SyncSkillPacket::handle);
        INSTANCE.registerMessage(id++, TechniqueSyncPacket.class,
                TechniqueSyncPacket::encode,
                TechniqueSyncPacket::decode,
                TechniqueSyncPacket::handle);
    }
}
