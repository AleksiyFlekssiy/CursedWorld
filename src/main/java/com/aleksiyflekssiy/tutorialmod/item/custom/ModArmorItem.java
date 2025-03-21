package com.aleksiyflekssiy.tutorialmod.item.custom;


import com.aleksiyflekssiy.tutorialmod.item.ModArmorMaterials;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


import java.util.Map;

public class ModArmorItem extends ArmorItem {

    private static final Map<ArmorMaterial, MobEffectInstance> MATERIAL_TO_EFFECT_MAP =
            (new ImmutableMap.Builder<ArmorMaterial, MobEffectInstance>())
                    .put(ModArmorMaterials.COPPER,
                            new MobEffectInstance(MobEffects.HEALTH_BOOST, MobEffectInstance.INFINITE_DURATION, 1, false, false, true))
                    .build();

    public ModArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        if (!level.isClientSide() && player.getInventory().armor.get(this.getEquipmentSlot().getIndex()) == stack){
            if(fullArmorSetEquipped(player)){
                evaluateArmorEffects(player);
            }
        }
    }

    private void evaluateArmorEffects(Player player) {
        for (Map.Entry<ArmorMaterial, MobEffectInstance> entry : MATERIAL_TO_EFFECT_MAP.entrySet()) {
            ArmorMaterial material = entry.getKey();
            MobEffectInstance effect = entry.getValue();

            if(hasCorrectArmorOn(material, player)){
                addStatusEffectForMaterial(player, material, effect);
            }
        }
    }

    private void addStatusEffectForMaterial(Player player, ArmorMaterial material, MobEffectInstance effect) {
        boolean hasEffect = player.hasEffect(effect.getEffect());

        if(hasCorrectArmorOn(material, player) && !hasEffect){
            player.addEffect(new MobEffectInstance(effect));
        }
    }

    private boolean hasCorrectArmorOn(ArmorMaterial material, Player player) {
        for (ItemStack armor : player.getInventory().armor) {
            if(!(armor.getItem() instanceof ArmorItem)) {
                return false;
            }
        }

        ArmorItem boots = ((ArmorItem) player.getInventory().getArmor(0).getItem());
        ArmorItem leggings = ((ArmorItem) player.getInventory().getArmor(1).getItem());
        ArmorItem chestplate = ((ArmorItem) player.getInventory().getArmor(2).getItem());
        ArmorItem helmet = ((ArmorItem) player.getInventory().getArmor(3).getItem());

        return helmet.getMaterial() == material && chestplate.getMaterial() == material
                && leggings.getMaterial() == material && boots.getMaterial() == material;
    }

    private boolean fullArmorSetEquipped(Player player) {
        ItemStack helmet = player.getInventory().getArmor(3);
        ItemStack chestplate = player.getInventory().getArmor(2);
        ItemStack leggings = player.getInventory().getArmor(1);
        ItemStack boots = player.getInventory().getArmor(0);

        return !helmet.isEmpty() && !chestplate.isEmpty() && !leggings.isEmpty() && !boots.isEmpty();
    }
}
