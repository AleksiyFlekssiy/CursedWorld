package com.aleksiyflekssiy.tutorialmod.registry;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.Skill;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.limitless.*;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.tenshadows.DivineDogs;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.tenshadows.GreatSerpent;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.tenshadows.Nue;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.tenshadows.Toad;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

public class Skills {
    public static ResourceKey<? extends Registry<Skill>> SKILL_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "skill"));
    public static final DeferredRegister<Skill> SKILLS = DeferredRegister.create(SKILL_REGISTRY_KEY, TutorialMod.MOD_ID);
    public static final Supplier<IForgeRegistry<Skill>> REGISTRY = SKILLS.makeRegistry(RegistryBuilder::of);

    public static final RegistryObject<Skill> INFINITY = registerSkill(new Infinity());
    public static final RegistryObject<Skill> BLUE = registerSkill(new Blue());
    public static final RegistryObject<Skill> RED = registerSkill(new Red());
    public static final RegistryObject<Skill> HOLLOW_PURPLE = registerSkill(new HollowPurple());
    public static final RegistryObject<Skill> UNLIMITED_VOID = registerSkill(new UnlimitedVoid());

    public static final RegistryObject<Skill> DIVINE_DOGS = registerSkill(new DivineDogs());
    public static final RegistryObject<Skill> NUE = registerSkill(new Nue());
    public static final RegistryObject<Skill> TOAD = registerSkill(new Toad());
    public static final RegistryObject<Skill> GREAT_SERPENT = registerSkill(new GreatSerpent());

    public static RegistryObject<Skill> registerSkill(Skill skill) {
        System.out.println("Registering skill " + skill.getName());
        return SKILLS.register(skill.getName(), () -> skill);
    }

    public static Skill getSkill(String name) {
        return REGISTRY.get().getValue(ResourceLocation.parse(name));
    }

    public static void register(IEventBus eventBus) {
        System.out.println("Registering skills");
        SKILLS.register(eventBus);
    }
}
