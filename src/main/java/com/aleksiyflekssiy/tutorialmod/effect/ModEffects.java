package com.aleksiyflekssiy.tutorialmod.effect;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, TutorialMod.MOD_ID);

    public static final RegistryObject<MobEffect> INFINITY = EFFECTS.register("infinity", InfinityEffect::new);

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
