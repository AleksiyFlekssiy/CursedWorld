package com.aleksiyflekssiy.tutorialmod.item.custom;

import com.aleksiyflekssiy.tutorialmod.client.renderer.WheelOfHarmonyArmorRenderer;
import com.aleksiyflekssiy.tutorialmod.sound.ModSoundEvents;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class WheelOfHarmonyItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final int TICK_TO_SPIN = 600;

    public HashMap<DamageType, Adaptation> ADAPTATIONS = new HashMap<DamageType, Adaptation>();
    public static RawAnimation WHEEL_IDLE = RawAnimation.begin().thenLoop("wheel_idle");
    public static RawAnimation WHEEL_ROTATION = RawAnimation.begin().thenPlay("wheel_rotation");

    public WheelOfHarmonyItem(ArmorMaterial pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);

        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        if (!level.isClientSide && player.getInventory().getArmor(3).getItem() instanceof WheelOfHarmonyItem) {
            for (Map.Entry<DamageType, Adaptation> entry : ADAPTATIONS.entrySet()) {
                DamageType source = entry.getKey();
                Adaptation adaptation = entry.getValue();
                if (!adaptation.isComplete()) {
                    float speed = adaptation.getSpeed();
                    adaptation.decreaseTicks(speed);

                    if (adaptation.getTicksLeft() <= 0) {
                        adaptation.makeCycle();
                        this.triggerArmorAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) level), "rotation_controller", "wheel_rotation");
                        player.setHealth(player.getMaxHealth());
                        if (adaptation.isComplete()) player.sendSystemMessage(Component.literal("You have adapted to " + source.toString()));
                    }
                }
            }
        }
    }

    public boolean checkAdaptation(DamageType source, LivingEntity entity){
        if (ADAPTATIONS.containsKey(source)) {
            if (ADAPTATIONS.get(source).isComplete()) return true;
            return false;
        }
        else {
            return false;
        }
    }


    public void addOrSpeedUpAdaptationToDamage(DamageType damageSource, float amount, LivingEntity entity) {
        if (!ADAPTATIONS.containsKey(damageSource)) {
            int cycles = Math.max(1, Adaptation.calculateCyclesToAdapt(entity.getMaxHealth(), amount));
            entity.sendSystemMessage(Component.literal("Started adaptation to " + damageSource.toString()));
            entity.sendSystemMessage(Component.literal("You would need a " + cycles + " cycles to adapt"));
            ADAPTATIONS.put(damageSource, new Adaptation(cycles));
            System.out.println(ADAPTATIONS.size());
        }
        else {
            Adaptation adaptation = ADAPTATIONS.get(damageSource);
            adaptation.increaseSpeed();
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

    private class Adaptation {
        private final int cyclesToAdapt;
        private int cyclesWent;
        private float speed = 1;
        private float ticksLeft = TICK_TO_SPIN;

        public Adaptation(int cyclesToAdapt){
            this.cyclesToAdapt = cyclesToAdapt;
        }

        public void makeCycle(){
            if (cyclesToAdapt - cyclesWent > 0) {
                cyclesWent++;
                ticksLeft = TICK_TO_SPIN;
            }
        }

        public int getCyclesToAdapt(){
            return cyclesToAdapt;
        }

        public int getCyclesWent(){
            return cyclesWent;
        }

        public float getSpeed(){
            return speed;
        }

        public void increaseSpeed(){
            speed *= 1.1;
        }

        public void decreaseTicks(float ticks){
            ticksLeft -= ticks;
        }

        public float getTicksLeft(){
            return ticksLeft;
        }

        public boolean isComplete(){
            return cyclesToAdapt - cyclesWent == 0;
        }

        public static int calculateCyclesToAdapt(float health, float damage){
            return Math.round(health / (health / damage));
        }
    }
}
