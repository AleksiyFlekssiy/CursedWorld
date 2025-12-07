package com.aleksiyflekssiy.tutorialmod.util;

import com.aleksiyflekssiy.tutorialmod.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.Skill;
import com.aleksiyflekssiy.tutorialmod.item.custom.WheelOfHarmonyItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AdaptationUtil {
    public static boolean checkAdaptation(DamageType damage, LivingEntity entity){
        if (!entity.level().isClientSide && entity instanceof Player player){
            if (checkForWheel(entity).isPresent()) {
                Map<Phenomenon, Adaptation> adaptationMap = loadAdaptations(checkForWheel(entity).get(), player);
                if (!adaptationMap.isEmpty()) {
                    for (Map.Entry<Phenomenon, Adaptation> entry : adaptationMap.entrySet()) {
                        Phenomenon phenomenon = entry.getKey();
                        if (phenomenon.equals(new Phenomenon(damage))) {
                            return entry.getValue().isComplete();
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean checkAdaptation(MobEffect effect, LivingEntity entity){
        if (!entity.level().isClientSide && entity instanceof Player player){
            if (checkForWheel(entity).isPresent()) {
                Map<Phenomenon, Adaptation> adaptationMap = loadAdaptations(checkForWheel(entity).get(), player);
                if (!adaptationMap.isEmpty()) {
                    for (Map.Entry<Phenomenon, Adaptation> entry : adaptationMap.entrySet()) {
                        Phenomenon phenomenon = entry.getKey();
                        if (phenomenon.equals(new Phenomenon(effect))) return entry.getValue().isComplete();
                    }
                }
            }
        }
        return false;
    }

    public static boolean checkAdaptation(Skill skill, LivingEntity entity){
        if (!entity.level().isClientSide && entity instanceof Player player){
            if (checkForWheel(entity).isPresent()) {
                Map<Phenomenon, Adaptation> adaptationMap = loadAdaptations(checkForWheel(entity).get(), player);
                if (!adaptationMap.isEmpty()) {
                    for (Map.Entry<Phenomenon, Adaptation> entry : adaptationMap.entrySet()) {
                        Phenomenon phenomenon = entry.getKey();
                        if (phenomenon.equals(new Phenomenon(skill))) return entry.getValue().isComplete();
                    }
                }
            }
        }
        return false;
    }

    public static Optional<ItemStack> checkForWheel(LivingEntity entity){
        Iterable<ItemStack> armor = entity.getArmorSlots();
        for (ItemStack itemStack : armor) {
            if (itemStack.getItem() instanceof WheelOfHarmonyItem) {
                return Optional.of(itemStack);
            }
        }
        return Optional.empty();
    }


    public static void addOrSpeedUpAdaptationToDamage(DamageType damageSource, float amount, LivingEntity entity) {
        if (checkForWheel(entity).isPresent()) {
            Map<Phenomenon, Adaptation> adaptationMap = loadAdaptations(checkForWheel(entity).get(), (Player) entity);
            if (!adaptationMap.containsKey(new Phenomenon(damageSource))) {
                int cycles = Math.max(1, Adaptation.calculateCyclesFromDamage(entity.getMaxHealth(), amount));
                entity.sendSystemMessage(Component.literal("Started adaptation to " + damageSource.msgId()));
                entity.sendSystemMessage(Component.literal("You would need a " + cycles + " cycles to adapt"));
                adaptationMap.put(new Phenomenon(damageSource), new Adaptation(cycles));
                saveAdaptations(checkForWheel(entity).get(), adaptationMap, entity.level());
                System.out.println(adaptationMap.size());
            } else {
                Adaptation adaptation = adaptationMap.get(new Phenomenon(damageSource));
                adaptation.increaseSpeed();
                saveAdaptations(checkForWheel(entity).get(), adaptationMap, entity.level());
                System.out.println(adaptation.getSpeed());
            }
            System.out.println(adaptationMap);
        }
    }

    public static void addOrSpeedUpAdaptationToEffect(MobEffectInstance effectInstance, LivingEntity entity){
        if (checkForWheel(entity).isPresent()){
            Map<Phenomenon, Adaptation> adaptationMap = loadAdaptations(checkForWheel(entity).get(), (Player) entity);
            if (!adaptationMap.containsKey(new Phenomenon(effectInstance.getEffect()))) {
                int cycles = Math.max(1, Adaptation.calculateCyclesFromEffect(effectInstance));
                entity.sendSystemMessage(Component.literal("Started adaptation to " + effectInstance.getEffect().getDescriptionId()));
                entity.sendSystemMessage(Component.literal("You would need a " + cycles + " cycles to adapt"));
                adaptationMap.put(new Phenomenon(effectInstance.getEffect()), new Adaptation(cycles));
                saveAdaptations(checkForWheel(entity).get(), adaptationMap, entity.level());
                System.out.println(adaptationMap.size());
            }
            else {
                Adaptation adaptation = adaptationMap.get(new Phenomenon(effectInstance.getEffect()));
                adaptation.increaseSpeed();
                saveAdaptations(checkForWheel(entity).get(), adaptationMap, entity.level());
                System.out.println(adaptation.getSpeed());
            }
            System.out.println(adaptationMap);
        }
    }

    public static void addOrSpeedUpAdaptationToSkill(Skill skill, LivingEntity entity){
        if (checkForWheel(entity).isPresent()){
            Map<Phenomenon, Adaptation> adaptationMap = loadAdaptations(checkForWheel(entity).get(), (Player) entity);
            if (!adaptationMap.containsKey(new Phenomenon(skill))) {
                int cycles = Math.max(1, Adaptation.calculateCyclesFromSkill(skill));
                entity.sendSystemMessage(Component.literal("Started adaptation to " + skill.getName()));
                entity.sendSystemMessage(Component.literal("You would need a " + cycles + " cycles to adapt"));
                adaptationMap.put(new Phenomenon(skill), new Adaptation(cycles));
                saveAdaptations(checkForWheel(entity).get(), adaptationMap, entity.level());
                System.out.println(adaptationMap.size());
            }
            else {
                Adaptation adaptation = adaptationMap.get(new Phenomenon(skill));
                adaptation.increaseSpeed();
                saveAdaptations(checkForWheel(entity).get(), adaptationMap, entity.level());
                System.out.println(adaptation.getSpeed());
            }
            System.out.println(adaptationMap);
        }
    }

    public static void saveAdaptations(ItemStack stack, Map<Phenomenon, Adaptation> adaptationMap, Level level) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag listTag = new ListTag();

        for (Map.Entry<Phenomenon, Adaptation> entry : adaptationMap.entrySet()) {
            Phenomenon phenomenon = entry.getKey();
            Adaptation adaptation = entry.getValue();

            CompoundTag entryTag = new CompoundTag();

            entryTag.putString("type", phenomenon.getPhenomenonType().getSerializedName());

            if (phenomenon.getPhenomenonType() == Phenomenon.PhenomenonType.DAMAGE) {
                entryTag.putString("damage_type", level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getKey(phenomenon.getDamageType()).toString());
            } else if (phenomenon.getPhenomenonType() == Phenomenon.PhenomenonType.EFFECT) {
                entryTag.putString("effect", level.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getKey(phenomenon.getEffect()).toString());
            }
            else entryTag.putString("skill", phenomenon.getSkill().getName());

            entryTag.putInt("cycles_to_adapt", adaptation.getCyclesToAdapt());
            entryTag.putInt("cycles_went", adaptation.getCyclesWent());
            entryTag.putFloat("speed", adaptation.getSpeed());
            entryTag.putFloat("ticks_left", adaptation.getTicksLeft());

            listTag.add(entryTag);
        }

        tag.put("WheelAdaptations", listTag);
    }

    public static Map<Phenomenon, Adaptation> loadAdaptations(ItemStack stack, Player player){
        Map<Phenomenon, Adaptation> map = new HashMap<>();

        if (!stack.hasTag()) return map;
        CompoundTag tag = stack.getTag();
        if (!tag.contains("WheelAdaptations")) return map;

        Level level = player.level();

        ListTag listTag = tag.getList("WheelAdaptations", 10);
        var damageTypeRegistry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        var effectRegistry = level.registryAccess().registryOrThrow(Registries.MOB_EFFECT);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag entryTag = listTag.getCompound(i);
            Phenomenon.PhenomenonType type = Phenomenon.PhenomenonType.fromName(entryTag.getString("type"));
            if (type == Phenomenon.PhenomenonType.DAMAGE){
                String damageType = entryTag.getString("damage_type");
                DamageType damage = damageTypeRegistry.get(ResourceLocation.tryParse(damageType));
                if (damage != null) {
                    Phenomenon phenomenon = new Phenomenon(damage);
                    Adaptation adaptation = new Adaptation(entryTag.getInt("cycles_to_adapt"));
                    adaptation.setCyclesWent(entryTag.getInt("cycles_went"));
                    adaptation.setSpeed(entryTag.getFloat("speed"));
                    adaptation.setTicksLeft(entryTag.getFloat("ticks_left"));
                    map.put(phenomenon, adaptation);
                }
                else System.out.println("Invalid damage_type: " + damageType);
            }
            else if (type == Phenomenon.PhenomenonType.EFFECT){
                String effect = entryTag.getString("effect");
                MobEffect mobEffect = effectRegistry.get(ResourceLocation.tryParse(effect));
                if (mobEffect != null) {
                    Phenomenon phenomenon = new Phenomenon(effectRegistry.get(ResourceLocation.tryParse(effect)));
                    Adaptation adaptation = new Adaptation(entryTag.getInt("cycles_to_adapt"));
                    adaptation.setCyclesWent(entryTag.getInt("cycles_went"));
                    adaptation.setSpeed(entryTag.getFloat("speed"));
                    adaptation.setTicksLeft(entryTag.getFloat("ticks_left"));
                    map.put(phenomenon, adaptation);
                }
                else System.out.println("Invalid effect: " + effect);
            }
            else {
                String skill = entryTag.getString("skill");
                Skill newSkill = CursedTechniqueCapability.Provider.createSkillByName(skill);
                if (newSkill != null){
                    Phenomenon phenomenon = new Phenomenon(newSkill);
                    Adaptation adaptation = new Adaptation(entryTag.getInt("cycles_to_adapt"));
                    adaptation.setCyclesWent(entryTag.getInt("cycles_went"));
                    adaptation.setSpeed(entryTag.getFloat("speed"));
                    adaptation.setTicksLeft(entryTag.getFloat("ticks_left"));
                    map.put(phenomenon, adaptation);
                }
                else System.out.println("Invalid skill: " + skill);
            }
        }
        return map;
    }
}
