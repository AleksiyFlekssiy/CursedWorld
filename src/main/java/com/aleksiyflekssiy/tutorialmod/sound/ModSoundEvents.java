package com.aleksiyflekssiy.tutorialmod.sound;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TutorialMod.MOD_ID);

    public static final RegistryObject<SoundEvent> RED_LAUNCH = SOUND_EVENTS.register("entity.red_entity.launch",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(TutorialMod.MOD_ID, "entity.red_entity.launch")));

    public static final RegistryObject<SoundEvent> BLUE_PULL = SOUND_EVENTS.register("entity.blue_entity.pull",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(TutorialMod.MOD_ID, "entity.blue_entity.pull")));

    public static final RegistryObject<SoundEvent> PURPLE_EXPLOSION = SOUND_EVENTS.register("purple_explosion",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(TutorialMod.MOD_ID, "purple_explosion")));

    public static final RegistryObject<SoundEvent> HOLLOW_PURPLE_LAUNCH = SOUND_EVENTS.register("entity.hollow_purple.launch",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(TutorialMod.MOD_ID, "entity.hollow_purple.launch")));

    public static final RegistryObject<SoundEvent> HOLLOW_PURPLE_MERGE = SOUND_EVENTS.register("entity.hollow_purple.merge",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(TutorialMod.MOD_ID, "entity.hollow_purple.merge")));

    public static final RegistryObject<SoundEvent> CHANT_1 = SOUND_EVENTS.register("chant_1",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(TutorialMod.MOD_ID, "chant_1")));

    public static final RegistryObject<SoundEvent> CHANT_2 = SOUND_EVENTS.register("chant_2",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(TutorialMod.MOD_ID, "chant_2")));

    public static final RegistryObject<SoundEvent> CHANT_3 = SOUND_EVENTS.register("chant_3",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(TutorialMod.MOD_ID, "chant_3")));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
