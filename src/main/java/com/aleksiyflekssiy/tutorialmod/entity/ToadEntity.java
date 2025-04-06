package com.aleksiyflekssiy.tutorialmod.entity;

import com.aleksiyflekssiy.tutorialmod.entity.goal.ShikigamiFollowOwnerGoal;
import com.aleksiyflekssiy.tutorialmod.entity.goal.ShikigamiOwnerHurtByTargetGoal;
import com.aleksiyflekssiy.tutorialmod.entity.goal.ShikigamiOwnerHurtTargetGoal;
import com.aleksiyflekssiy.tutorialmod.entity.goal.toad.ShikigamiTargetSummonerGoal;
import com.aleksiyflekssiy.tutorialmod.entity.goal.toad.TongueCatchGoal;
import com.aleksiyflekssiy.tutorialmod.entity.goal.toad.TonguePullGoal;
import com.aleksiyflekssiy.tutorialmod.entity.goal.toad.TongueSwingGoal;
import com.aleksiyflekssiy.tutorialmod.entity.navigation.JumpingMoveControl;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class ToadEntity extends Shikigami {
    private static final EntityDataAccessor<Float> DISTANCE = SynchedEntityData.defineId(ToadEntity.class, EntityDataSerializers.FLOAT);
    public AnimationState mouthOpen = new AnimationState();
    public float targetYaw;
    private long lastTickUse;
    private Order order = Order.NONE;

    public ToadEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new JumpingMoveControl(this);
        setDistance(0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 100)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.ATTACK_DAMAGE, 10f)
                .add(Attributes.FOLLOW_RANGE, 50)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 2.5)
                .add(Attributes.ARMOR_TOUGHNESS, 2.5)
                .add(Attributes.JUMP_STRENGTH, 1);
    }

    public Order getOrder(){
        return this.order;
    }

    public void setOrder(Order order){
        this.order = order;
    }

    public void followOrder(LivingEntity target, Order order) {
        if (this.isTamed() && this.owner != null) {
            this.goalSelector.getRunningGoals().forEach(Goal::stop);
            this.setTarget(target);
            this.setOrder(order);
        }
    }

    public void clearOrder(){
        this.setTarget(null);
        this.setOrder(Order.NONE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DISTANCE, 0F);
    }

    public float getDistance() {
        return this.entityData.get(DISTANCE);
    }

    public void setDistance(float distance) {
        this.entityData.set(DISTANCE, distance);
    }

    public void setLastTickUse() {
        this.lastTickUse = level().getGameTime();
    }

    public boolean isCooldownOff(){
        return this.level().getGameTime() - this.lastTickUse > 100;
    }

    @Override
    protected void registerGoals() {
        if (!isTamed) {
            this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, true));
        }
        else {
            this.goalSelector.addGoal(3, new ShikigamiFollowOwnerGoal(this, 5, 5, 1));
            this.targetSelector.addGoal(0, new ShikigamiOwnerHurtTargetGoal(this, true));
            this.targetSelector.addGoal(1, new ShikigamiOwnerHurtByTargetGoal(this, true));
        }
        this.goalSelector.addGoal(0, new TonguePullGoal(this));
        this.goalSelector.addGoal(1, new TongueSwingGoal(this));
        this.goalSelector.addGoal(2, new TongueCatchGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity target = this.level().getNearestEntity(LivingEntity.class, TargetingConditions.DEFAULT, this, this.getX(), this.getY(), this.getZ(), this.getBoundingBox().inflate(30));
        if (target == null) return;
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!player.level().isClientSide()) {
            player.startRiding(this);
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (entity instanceof Player player) {
            mouthOpen.startIfStopped(tickCount);
            return player;
        }
        mouthOpen.stop();
        return null;
    }

    public enum Order {
        NONE,
        PULL,
        SWING,
        CATCH
    }
}
