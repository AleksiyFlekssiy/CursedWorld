package com.aleksiyflekssiy.tutorialmod.item.custom;

import com.aleksiyflekssiy.tutorialmod.client.renderer.WheelOfHarmonyArmorRenderer;
import com.aleksiyflekssiy.tutorialmod.sound.ModSoundEvents;
import com.aleksiyflekssiy.tutorialmod.util.Adaptation;
import com.aleksiyflekssiy.tutorialmod.util.AdaptationUtil;
import com.aleksiyflekssiy.tutorialmod.util.Phenomenon;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.ClientUtils;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;
import java.util.function.Consumer;

public class WheelOfHarmonyItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public static RawAnimation WHEEL_IDLE = RawAnimation.begin().thenLoop("wheel_idle");
    public static RawAnimation WHEEL_ROTATION = RawAnimation.begin().thenPlay("wheel_rotation");

    public WheelOfHarmonyItem(ArmorMaterial pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);

        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        if (!level.isClientSide && player.getInventory().getArmor(3).getItem() instanceof WheelOfHarmonyItem) {
            Map<Phenomenon, Adaptation> adaptationMap = AdaptationUtil.loadAdaptations(stack, player);
            for (Map.Entry<Phenomenon, Adaptation> entry : adaptationMap.entrySet()) {
                Phenomenon phenomenon = entry.getKey();
                Adaptation adaptation = entry.getValue();
                if (!adaptation.isComplete()) {
                    if (phenomenon.getPhenomenonType() == Phenomenon.PhenomenonType.EFFECT){
                        if (player.tickCount % 300 == 0){
                            adaptation.increaseSpeed();
                        }
                    }
                    float speed = adaptation.getSpeed();
                    adaptation.decreaseTicks(speed);
                    AdaptationUtil.saveAdaptations(stack, adaptationMap, level);

                    if (adaptation.getTicksLeft() <= 0) {
                        adaptation.makeCycle();
                        this.triggerArmorAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) level), "rotation_controller", "wheel_rotation");
                        player.setHealth(player.getMaxHealth());
                        if (adaptation.isComplete()) {
                            if (phenomenon.getPhenomenonType() == Phenomenon.PhenomenonType.DAMAGE) player.sendSystemMessage(Component.literal("You have adapted to " + phenomenon.getDamageType().msgId()));
                            else if (phenomenon.getPhenomenonType() == Phenomenon.PhenomenonType.EFFECT) player.sendSystemMessage(Component.literal("You have adapted to " + phenomenon.getEffect().getDescriptionId()));
                            else player.sendSystemMessage(Component.literal("You have adapted to " + phenomenon.getSkill().getName()));
                        }
                        AdaptationUtil.saveAdaptations(stack, adaptationMap, level);
                    }
                }
                else {
                    if (phenomenon.getPhenomenonType() == Phenomenon.PhenomenonType.EFFECT){
                        if (player.hasEffect(phenomenon.getEffect())) player.removeEffect(phenomenon.getEffect());
                    }
                }
            }
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // Create our armor model/renderer for forge and return it
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoArmorRenderer<?> armorRenderer;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.armorRenderer == null)
                    this.armorRenderer = new WheelOfHarmonyArmorRenderer();

                // This prepares our GeoArmorRenderer for the current render frame.
                // These parameters may be null however, so we don't do anything further with them
                this.armorRenderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);

                return this.armorRenderer;
            }
        });
    }
    // Let's add our animation controller
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this,"idle_controller", state -> {
            state.setAndContinue(WHEEL_IDLE);
            return PlayState.CONTINUE;
        }));
        controllers.add(new AnimationController<>(this, "rotation_controller", state -> {
            if (state.isCurrentAnimation(WHEEL_ROTATION)) return PlayState.CONTINUE;
            return PlayState.STOP;
        })
                .triggerableAnim("wheel_rotation", WHEEL_ROTATION)
                .setSoundKeyframeHandler(state -> {
                    Player player = ClientUtils.getClientPlayer();
                    if (player != null) player.playSound(ModSoundEvents.WHEEL_ROTATION.get(), 1.0F, 1.0F);
                }));
    }

}
