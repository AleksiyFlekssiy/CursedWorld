package com.aleksiyflekssiy.tutorialmod.event;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.capability.CursedEnergy;
import com.aleksiyflekssiy.tutorialmod.capability.CursedEnergyCapability;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ReplenishCursedEnergy {
    @SubscribeEvent
    public static void giveEnergy(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;
        Player player = event.player;
        player.getCapability(CursedEnergyCapability.CURSED_ENERGY).ifPresent(energy -> {
            CursedEnergy cursedEnergy = (CursedEnergy) energy;
            cursedEnergy.setFastTick(player.isCrouching());
            cursedEnergy.tick();
        });
    }
}
