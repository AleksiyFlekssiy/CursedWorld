package com.aleksiyflekssiy.tutorialmod.network;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, CursedEnergySyncPacket.class,
                CursedEnergySyncPacket::encode,
                CursedEnergySyncPacket::decode,
                CursedEnergySyncPacket::handle);

        INSTANCE.registerMessage(id++, UseSkillPacket.class,
                UseSkillPacket::encode,
                UseSkillPacket::decode,
                UseSkillPacket::handle);

        INSTANCE.registerMessage(id++, SyncSkillPacket.class,
                SyncSkillPacket::encode,
                SyncSkillPacket::decode,
                SyncSkillPacket::handle);

        INSTANCE.registerMessage(id++, TechniqueSyncPacket.class,
                TechniqueSyncPacket::encode,
                TechniqueSyncPacket::decode,
                TechniqueSyncPacket::handle);

        INSTANCE.registerMessage(id++, SwitchOrderPacket.class,
                SwitchOrderPacket::encode,
                SwitchOrderPacket::decode,
                SwitchOrderPacket::handle);

        INSTANCE.registerMessage(id++, InputLockPacket.class,
                InputLockPacket::encode,
                InputLockPacket::decode,
                InputLockPacket::handle);
    }
}
