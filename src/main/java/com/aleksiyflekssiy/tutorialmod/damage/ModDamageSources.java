package com.aleksiyflekssiy.tutorialmod.damage;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ModDamageSources {
    public static final ResourceKey<DamageType> INFINITY = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "infinity"));
    public static final ResourceKey<DamageType> BLUE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "blue"));
    public static final ResourceKey<DamageType> RED = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "red"));
    public static final ResourceKey<DamageType> HOLLOW_PURPLE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "hollow_purple"));

    public static DamageSource infinity(LivingEntity causer) {
        RegistryAccess registryAccess = causer.level().registryAccess();
        Registry<DamageType> registry = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);
        return new DamageSource(registry.getHolderOrThrow(INFINITY), causer);
    }
    public static DamageSource blue(Entity source, LivingEntity causer) {
        RegistryAccess registryAccess = source.level().registryAccess();
        Registry<DamageType> registry = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);
        return new DamageSource(registry.getHolderOrThrow(BLUE), source, causer);
    }
    public static DamageSource red(Entity source, LivingEntity causer) {
        RegistryAccess registryAccess = source.level().registryAccess();
        Registry<DamageType> registry = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);
        return new DamageSource(registry.getHolderOrThrow(RED), source, causer);
    }
    public static DamageSource hollow_purple(Entity source, LivingEntity causer) {
        RegistryAccess registryAccess = source.level().registryAccess();
        Registry<DamageType> registry = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);
        return new DamageSource(registry.getHolderOrThrow(HOLLOW_PURPLE), source, causer);
    }
}
