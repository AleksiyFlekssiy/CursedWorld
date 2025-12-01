package com.aleksiyflekssiy.tutorialmod.item.custom;

import com.aleksiyflekssiy.tutorialmod.client.renderer.WheelOfHarmonyArmorRenderer;
import com.aleksiyflekssiy.tutorialmod.sound.ModSoundEvents;
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

    private static final int TICK_TO_SPIN = 600;

    public WheelOfHarmonyItem(ArmorMaterial pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);

        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        if (!level.isClientSide && player.getInventory().getArmor(3).getItem() instanceof WheelOfHarmonyItem) {
            Map<Phenomenon, Adaptation> adaptationMap = loadAdaptations(stack, player);
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
                    saveAdaptations(stack, adaptationMap, level);

                    if (adaptation.getTicksLeft() <= 0) {
                        adaptation.makeCycle();
                        this.triggerArmorAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) level), "rotation_controller", "wheel_rotation");
                        player.setHealth(player.getMaxHealth());
                        if (adaptation.isComplete()) {
                            if (phenomenon.getPhenomenonType() == Phenomenon.PhenomenonType.DAMAGE) player.sendSystemMessage(Component.literal("You have adapted to " + phenomenon.getDamageType().msgId()));
                            else player.sendSystemMessage(Component.literal("You have adapted to " + phenomenon.getEffect().getDescriptionId()));
                        }
                        saveAdaptations(stack, adaptationMap, level);
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

    public boolean checkAdaptation(DamageType damage, MobEffect effect, LivingEntity entity){
        if (!entity.level().isClientSide && entity instanceof Player player){
                ItemStack itemStack = player.getInventory().getArmor(3);
                if (itemStack.getItem() instanceof WheelOfHarmonyItem wheel) {
                    Map<Phenomenon, Adaptation> adaptationMap = wheel.loadAdaptations(itemStack, player);
                    if (!adaptationMap.isEmpty()) {
                        for (Map.Entry<Phenomenon, Adaptation> entry : adaptationMap.entrySet()) {
                            Phenomenon phenomenon = entry.getKey();
                            if (damage != null){
                                if (phenomenon.equals(new Phenomenon(damage))) {
                                    return entry.getValue().isComplete();
                                }
                            }
                            else{
                                if (phenomenon.equals(new Phenomenon(effect))) return entry.getValue().isComplete();
                            }
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


    public void addOrSpeedUpAdaptationToDamage(DamageType damageSource, float amount, LivingEntity entity) {
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

    public void addOrSpeedUpAdaptationToEffect(MobEffectInstance effectInstance, LivingEntity entity){
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

    private void saveAdaptations(ItemStack stack, Map<Phenomenon, Adaptation> adaptationMap, Level level) {
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

            entryTag.putInt("cycles_to_adapt", adaptation.cyclesToAdapt);
            entryTag.putInt("cycles_went", adaptation.cyclesWent);
            entryTag.putFloat("speed", adaptation.speed);
            entryTag.putFloat("ticks_left", adaptation.ticksLeft);

            listTag.add(entryTag);
        }

        tag.put("WheelAdaptations", listTag);
    }

    private Map<Phenomenon, Adaptation> loadAdaptations(ItemStack stack, Player player){
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
                    adaptation.cyclesWent = entryTag.getInt("cycles_went");
                    adaptation.speed = entryTag.getFloat("speed");
                    adaptation.ticksLeft = entryTag.getFloat("ticks_left");
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
                    adaptation.cyclesWent = entryTag.getInt("cycles_went");
                    adaptation.speed = entryTag.getFloat("speed");
                    adaptation.ticksLeft = entryTag.getFloat("ticks_left");
                    map.put(phenomenon, adaptation);
                }
                else System.out.println("Invalid effect: " + effect);
            }
        }
        return map;
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

    private static class Adaptation {
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
            speed *= 1.1f;
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

        public static int calculateCyclesFromDamage(float health, float damage){
            return Math.round(health / (health / damage));
        }

        public static int calculateCyclesFromEffect(MobEffectInstance effectInstance){
            int total = 0;
            if (effectInstance.isInfiniteDuration()) total += 5;
            else total += Math.round(effectInstance.getDuration() / 200 / 5);
            total += effectInstance.getAmplifier();
            return total;
        }

        @Override
        public String toString() {
            return "Cycles need: " + cyclesToAdapt + "; Cycles went: " + cyclesWent + "; Speed: " + speed;
        }
    }

    private static class Phenomenon{
        private final DamageType damageType;
        private final MobEffect effect;
        private final PhenomenonType phenomenonType;

        public Phenomenon(DamageType damageType){
            this.damageType = damageType;
            this.effect = null;
            this.phenomenonType = PhenomenonType.DAMAGE;
        }

        public Phenomenon(MobEffect effect){
            this.damageType = null;
            this.effect = effect;
            this.phenomenonType = PhenomenonType.EFFECT;
        }

        public DamageType getDamageType() {
            return damageType;
        }

        public MobEffect getEffect() {
            return effect;
        }

        public PhenomenonType getPhenomenonType() {
            return phenomenonType;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj instanceof Phenomenon phenomenon){
                if (this.phenomenonType == phenomenon.phenomenonType){
                    if (phenomenonType == PhenomenonType.DAMAGE) return this.damageType.msgId().equals(phenomenon.damageType.msgId());
                    else if (phenomenonType == PhenomenonType.EFFECT) return this.effect.getDescriptionId().equals(phenomenon.effect.getDescriptionId());
                }
            }
            return false;
        }

        @Override
        public String toString() {
            if (phenomenonType == PhenomenonType.DAMAGE){
                return "Type: " + phenomenonType.getSerializedName() + "; DamageType: " + damageType.msgId();
            }
            else return "Type: " + phenomenonType.getSerializedName() + "; Effect: " + effect.getDescriptionId();
        }

        @Override
        public int hashCode() {
            return Objects.hash(damageType, effect, phenomenonType);
        }

        public enum PhenomenonType{
            DAMAGE("damage"),
            EFFECT("effect");

            private final String name;

            PhenomenonType(String name){
                this.name = name;
            }

            public String getSerializedName() {
                return name; // всегда "damage", "effect" — маленькими буквами!
            }

            public static PhenomenonType fromName(String name) {
                if (name == null) return null;
                return Arrays.stream(values())
                        .filter(t -> t.name.equals(name))
                        .findFirst()
                        .orElse(null);
            }
        }
    }
}
