package com.aleksiyflekssiy.tutorialmod.entity;

import com.aleksiyflekssiy.tutorialmod.client.model.NueAnimations;
import com.aleksiyflekssiy.tutorialmod.entity.goal.ShikigamiOwnerHurtByTargetGoal;
import com.aleksiyflekssiy.tutorialmod.entity.goal.ShikigamiOwnerHurtTargetGoal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.definitions.CamelAnimation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.camel.CamelAi;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class NueEntity extends Shikigami{
    private LivingEntity grabbedEntity;
    Vec3 moveTargetPoint = Vec3.ZERO;
    BlockPos anchorPoint = BlockPos.ZERO;
    NueEntity.AttackPhase attackPhase = NueEntity.AttackPhase.CIRCLE;
    public AnimationState idleAnimation = new AnimationState();
    public AnimationState flyAnimation = new AnimationState();
    private int idleAnimationTimeout = 0;
    private static final EntityDataAccessor<Integer> ANIMATION = SynchedEntityData.defineId(NueEntity.class, EntityDataSerializers.INT);
    public static final int IDLE = 0;
    public static final int FLY = 1;

    public NueEntity(EntityType<? extends Shikigami> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new FlyingMoveControl(this);
        this.entityData.set(ANIMATION, 0);
    }

    enum AttackPhase {
        CIRCLE,
        SWOOP;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()){
            if (this.onGround()) setAnimation(IDLE);
            else setAnimation(FLY);
        }

        else updateAnimation();
        //this.goalSelector.getRunningGoals().forEach(goal -> System.out.println(goal.getGoal()));
    }

    private void setupAnimationStates(){
        if (this.idleAnimationTimeout <= 0){
            this.idleAnimationTimeout = random.nextInt(40) + 80;
            this.idleAnimation.start(this.tickCount);
        }
        else {
            --this.idleAnimationTimeout;
        }
    }

    private void updateAnimation(){
        int state = getAnimation();
        //stopAnimation();
        switch (state) {
            case IDLE -> idleAnimation.startIfStopped(this.tickCount);
            case FLY -> flyAnimation.startIfStopped(this.tickCount);
        }
    }

    public AnimationState getAnimationState(AnimationDefinition definition) {
        if (definition == NueAnimations.idle) return idleAnimation;
        if (definition == NueAnimations.fly) return flyAnimation;
        return idleAnimation; // По умолчанию
    }

    private void stopAnimation(){
        idleAnimation.stop();
        flyAnimation.stop();
    }

    public void setAnimation(int animation){
        this.entityData.set(ANIMATION, animation);
    }

    public int getAnimation(){
        return this.entityData.get(ANIMATION);
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FlyingAttackStrategyGoal());
        this.goalSelector.addGoal(2, new FlyingSweepAttackGoal());
        this.goalSelector.addGoal(3, new FlyingCircleAroundAnchorGoal());
        if (!isTamed) this.targetSelector.addGoal(1, new FlyingAttackPlayerTargetGoal());
        else {
            this.targetSelector.addGoal(1, new ShikigamiOwnerHurtTargetGoal(this, false));
            this.targetSelector.addGoal(2, new ShikigamiOwnerHurtByTargetGoal(this, false));
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!player.level().isClientSide() && player.equals(this.owner)){
            player.startRiding(this);
        }
        return InteractionResult.SUCCESS;
    }


    @Nullable
    public LivingEntity getControllingPassenger() {
            Entity entity = this.getFirstPassenger();
            if (entity instanceof Player player) {
                return player;
            }
        return null;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ANIMATION, 0);
    }

    public void tryGrabEntityBelow() {
        if (this.level().isClientSide()) return;
        if (grabbedEntity == null) {
            // Определяем область поиска под сущностью
            double grabRange = 2.0; // Радиус захвата по горизонтали
            double grabHeight = 5.0; // Высота поиска вниз
            AABB grabBox = new AABB(
                    this.getX() - grabRange, this.getY() - grabHeight, this.getZ() - grabRange,
                    this.getX() + grabRange, this.getY(), this.getZ() + grabRange
            );

            // Ищем живые сущности в области
            List<LivingEntity> entitiesBelow = this.level().getEntitiesOfClass(
                    LivingEntity.class,
                    grabBox,
                    entity -> entity != this && entity != this.getControllingPassenger() && !entity.isPassenger()
            );

            // Если есть сущности, пытаемся схватить ближайшую
            if (!entitiesBelow.isEmpty()) {
                LivingEntity target = entitiesBelow.get(0); // Берём первую подходящую сущность
                if (this.getPassengers().isEmpty() || this.getPassengers().size() < 2) {
                    // Прикрепляем сущность как пассажира
                    boolean isWork = target.startRiding(this, true);
                    if (isWork) grabbedEntity = target;
                    this.playSound(SoundEvents.PHANTOM_BITE, 1.0F, 1.0F); // Звук захвата
                }
            }
        }
        else {
            grabbedEntity.stopRiding();
            grabbedEntity = null;
        }
    }

    protected void tickRidden(Player pPlayer, Vec3 pTravelVector) {
        super.tickRidden(pPlayer, pTravelVector);
        this.setRot(pPlayer.getYRot(), pPlayer.getXRot() * 0.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
        if (!onGround()) {
            List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.5));
            entities.forEach(entity -> {
                if (!entity.equals(this) && !entity.equals(grabbedEntity) && !entity.equals(getControllingPassenger()))
                    this.doHurtTarget(entity);
            });
        }
    }

    protected Vec3 getRiddenInput(Player player, Vec3 travelVector) {
        byte x = 0;
        byte y = 0;
        byte z = 0;
        Minecraft client = Minecraft.getInstance();
        if (client.options.keyUp.isDown()) z += 1;
        if (client.options.keyDown.isDown()) z -= 1;
        if (client.options.keyLeft.isDown()) x += 1;
        if (client.options.keyRight.isDown()) x -= 1;
        if (client.options.keyJump.isDown()) y += 1;
        if (client.options.keySprint.isDown()) y -= 1;
        return new Vec3(x, y, z);
    }

    protected float getRiddenSpeed(Player pPlayer) {
        return (float)(this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.5);
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {
        int passengerIndex = this.getPassengers().indexOf(passenger);
        if (passengerIndex == 0) { // Первый пассажир (игрок)
            super.positionRider(passenger, callback);
        } else { // Схваченная сущность
            double nueHeight = this.getBbHeight(); // Высота NueEntity
            double offsetY = -nueHeight / 2; // Смещение до нижней границы NueEntity
            callback.accept(passenger, this.getX(), this.getY() + offsetY, this.getZ());
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 100)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.ATTACK_DAMAGE, 10f)
                .add(Attributes.FOLLOW_RANGE, 100)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 2.5)
                .add(Attributes.ARMOR_TOUGHNESS, 2.5)
                .add(Attributes.JUMP_STRENGTH, 1);
    }

    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
        //Nue is unable to be damaged by falling
    }

    public void travel(Vec3 pTravelVector) {
            if (this.isControlledByLocalInstance()) {
                if (this.isInWater()) {
                    this.moveRelative(0.02F, pTravelVector);
                    this.move(MoverType.SELF, this.getDeltaMovement());
                    this.setDeltaMovement(this.getDeltaMovement().scale((double) 0.8F));
                } else if (this.isInLava()) {
                    this.moveRelative(0.02F, pTravelVector);
                    this.move(MoverType.SELF, this.getDeltaMovement());
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
                } else {
                    BlockPos ground = getBlockPosBelowThatAffectsMyMovement();
                    float f = 0.91F;
                    if (this.onGround()) {
                        f = this.level().getBlockState(ground).getFriction(this.level(), ground, this) * 0.91F;
                    }

                    float f1 = 0.16277137F / (f * f * f);
                    f = 0.91F;
                    if (this.onGround()) {
                        f = this.level().getBlockState(ground).getFriction(this.level(), ground, this) * 0.91F;
                    }

                    this.moveRelative(this.onGround() ? 0.1F * f1 : 0.02F, pTravelVector);
                    this.move(MoverType.SELF, this.getDeltaMovement());

                    if (this.onGround()) {
                        this.setDeltaMovement(this.getDeltaMovement().scale((double) f));
                    }
                }
            }

        this.calculateEntityAnimation(false);
    }

    public boolean onClimbable() {
        return false;
    }

    abstract class FlyingMoveTargetGoal extends Goal {
        public FlyingMoveTargetGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        protected boolean touchingTarget() {
            return NueEntity.this.moveTargetPoint.distanceToSqr(NueEntity.this.getX(), NueEntity.this.getY(), NueEntity.this.getZ()) < 4.0D;
        }
    }

    class FlyingSweepAttackGoal extends FlyingMoveTargetGoal {

        public boolean canUse() {
            return NueEntity.this.getTarget() != null && NueEntity.this.attackPhase == NueEntity.AttackPhase.SWOOP;
        }

        public boolean canContinueToUse() {
            LivingEntity livingentity = NueEntity.this.getTarget();
            if (livingentity == null) {
                return false;
            } else if (!livingentity.isAlive()) {
                return false;
            } else {
                if (livingentity instanceof Player) {
                    Player player = (Player)livingentity;
                    if (livingentity.isSpectator() || player.isCreative()) {
                        return false;
                    }
                }

                if (!this.canUse()) {
                    return false;
                }
                return true;
            }
        }

        public void start() {
        }

        public void stop() {
            NueEntity.this.setTarget((LivingEntity)null);
            NueEntity.this.attackPhase = NueEntity.AttackPhase.CIRCLE;
        }

        public void tick() {
            LivingEntity livingentity = NueEntity.this.getTarget();
            if (livingentity != null) {
                NueEntity.this.moveTargetPoint = new Vec3(livingentity.getX(), livingentity.getY(0.5D), livingentity.getZ());
                if (NueEntity.this.getBoundingBox().inflate((double)0.5F).intersects(livingentity.getBoundingBox())) {
                    NueEntity.this.doHurtTarget(livingentity);
                    NueEntity.this.attackPhase = NueEntity.AttackPhase.CIRCLE;
                    if (!NueEntity.this.isSilent()) {
                        NueEntity.this.level().levelEvent(1039, NueEntity.this.blockPosition(), 0);
                    }
                } else if (NueEntity.this.horizontalCollision) {
                    NueEntity.this.attackPhase = NueEntity.AttackPhase.CIRCLE;
                }
            }
        }
    }

    class FlyingAttackPlayerTargetGoal extends Goal {
        private final TargetingConditions attackTargeting = TargetingConditions.forCombat().range(64.0D);
        private int nextScanTick = reducedTickDelay(20);

        public boolean canUse() {
            if (this.nextScanTick > 0) {
                --this.nextScanTick;
                return false;
            } else {
                this.nextScanTick = reducedTickDelay(60);
                List<Player> list = NueEntity.this.level().getNearbyPlayers(this.attackTargeting, NueEntity.this, NueEntity.this.getBoundingBox().inflate(64.0D, 64.0D, 64.0D));
                if (!list.isEmpty()) {
                    list.sort(Comparator.<Entity, Double>comparing(Entity::getY).reversed());

                    for(Player player : list) {
                        if (NueEntity.this.canAttack(player, TargetingConditions.DEFAULT)) {
                            NueEntity.this.setTarget(player);
                            return true;
                        }
                    }
                }

                return false;
            }
        }

        public boolean canContinueToUse() {
            LivingEntity livingentity = NueEntity.this.getTarget();
            return livingentity != null ? NueEntity.this.canAttack(livingentity, TargetingConditions.DEFAULT) : false;
        }
    }

    class FlyingAttackStrategyGoal extends Goal {
        private int nextSweepTick;

        public boolean canUse() {
            LivingEntity livingentity = NueEntity.this.getTarget();
            return livingentity != null ? NueEntity.this.canAttack(livingentity, TargetingConditions.DEFAULT) : false;
        }

        public void start() {
            this.nextSweepTick = this.adjustedTickDelay(10);
            NueEntity.this.attackPhase = NueEntity.AttackPhase.CIRCLE;
            this.setAnchorAboveTarget();
        }

        public void stop() {
            NueEntity.this.anchorPoint = NueEntity.this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, NueEntity.this.anchorPoint).above(10 + NueEntity.this.random.nextInt(20));
        }

        public void tick() {
            if (NueEntity.this.attackPhase == NueEntity.AttackPhase.CIRCLE) {
                --this.nextSweepTick;
                if (this.nextSweepTick <= 0) {
                    NueEntity.this.attackPhase = NueEntity.AttackPhase.SWOOP;
                    this.setAnchorAboveTarget();
                    this.nextSweepTick = this.adjustedTickDelay((10 + NueEntity.this.random.nextInt(10)) * 2);
                    NueEntity.this.playSound(SoundEvents.PHANTOM_SWOOP, 10.0F, 0.95F + NueEntity.this.random.nextFloat() * 0.1F);
                }
            }
        }

        private void setAnchorAboveTarget() {
            NueEntity.this.anchorPoint = NueEntity.this.getTarget().blockPosition().above(10); //+ NueEntity.this.random.nextInt(5));
            if (NueEntity.this.anchorPoint.getY() < NueEntity.this.level().getSeaLevel()) {
                NueEntity.this.anchorPoint = new BlockPos(NueEntity.this.anchorPoint.getX(), NueEntity.this.level().getSeaLevel() + 1, NueEntity.this.anchorPoint.getZ());
            }

        }
    }

    class FlyingCircleAroundAnchorGoal extends FlyingMoveTargetGoal {
        private float angle;
        private float distance;
        private float height;
        private float clockwise;

        public boolean canUse() {
            return NueEntity.this.getTarget() == null || NueEntity.this.attackPhase == NueEntity.AttackPhase.CIRCLE;
        }

        public void start() {
            this.distance = 5.0F + NueEntity.this.random.nextFloat() * 10.0F;
            this.height = 0;//1.0F + NueEntity.this.random.nextFloat() * 4F;
            this.clockwise = NueEntity.this.random.nextBoolean() ? 1.0F : -1.0F;
            this.selectNext();
        }

        public void tick() {
            if (NueEntity.this.random.nextInt(this.adjustedTickDelay(350)) == 0) {
                this.height = -4.0F + NueEntity.this.random.nextFloat() * 9.0F;
            }

            if (NueEntity.this.random.nextInt(this.adjustedTickDelay(250)) == 0) {
                ++this.distance;
                if (this.distance > 15.0F) {
                    this.distance = 5.0F;
                    this.clockwise = -this.clockwise;
                }
            }

            if (NueEntity.this.random.nextInt(this.adjustedTickDelay(450)) == 0) {
                this.angle = NueEntity.this.random.nextFloat() * 2.0F * (float)Math.PI;
                this.selectNext();
            }

            if (this.touchingTarget()) {
                this.selectNext();
            }

            if (NueEntity.this.moveTargetPoint.y < NueEntity.this.getY() && !NueEntity.this.level().isEmptyBlock(NueEntity.this.blockPosition().below(1))) {
                this.height = Math.max(1.0F, this.height);
                this.selectNext();
            }

            if (NueEntity.this.moveTargetPoint.y > NueEntity.this.getY() && !NueEntity.this.level().isEmptyBlock(NueEntity.this.blockPosition().above(1))) {
                this.height = Math.min(-1.0F, this.height);
                this.selectNext();
            }

        }

        private void selectNext() {
            if (BlockPos.ZERO.equals(NueEntity.this.anchorPoint)) {
                NueEntity.this.anchorPoint = NueEntity.this.blockPosition();
            }

            this.angle += this.clockwise * 15.0F * ((float)Math.PI / 180F);
            NueEntity.this.moveTargetPoint = Vec3.atLowerCornerOf(NueEntity.this.anchorPoint).add((double)(this.distance * Mth.cos(this.angle)), (double)(-4.0F + this.height), (double)(this.distance * Mth.sin(this.angle)));
        }
    }

    class FlyingMoveControl extends MoveControl {
        private float speed = 0.1F;

        public FlyingMoveControl(Mob pMob) {
            super(pMob);
        }

        public void tick() {
            if (NueEntity.this.horizontalCollision) {
                NueEntity.this.setYRot(NueEntity.this.getYRot() + 180.0F);
                this.speed = 0.1F;
            }

            double d0 = NueEntity.this.moveTargetPoint.x - NueEntity.this.getX();
            double d1 = NueEntity.this.moveTargetPoint.y - NueEntity.this.getY();
            double d2 = NueEntity.this.moveTargetPoint.z - NueEntity.this.getZ();
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            if (Math.abs(d3) > (double)1.0E-5F) {
                double d4 = 1.0D - Math.abs(d1 * (double)0.7F) / d3;
                d0 *= d4;
                d2 *= d4;
                d3 = Math.sqrt(d0 * d0 + d2 * d2);
                double d5 = Math.sqrt(d0 * d0 + d2 * d2 + d1 * d1);
                float f = NueEntity.this.getYRot();
                float f1 = (float)Mth.atan2(d2, d0);
                float f2 = Mth.wrapDegrees(NueEntity.this.getYRot() + 90.0F);
                float f3 = Mth.wrapDegrees(f1 * (180F / (float)Math.PI));
                NueEntity.this.setYRot(Mth.approachDegrees(f2, f3, 4.0F) - 90.0F);
                NueEntity.this.yBodyRot = NueEntity.this.getYRot();
                if (Mth.degreesDifferenceAbs(f, NueEntity.this.getYRot()) < 3.0F) {
                    this.speed = Mth.approach(this.speed, 5F, 0.005F * (5F / this.speed));
                } else {
                    this.speed = Mth.approach(this.speed, 0.2F, 0.025F);
                }
                float f4 = (float)(-(Mth.atan2(-d1, d3) * (double)(180F / (float)Math.PI)));
                NueEntity.this.setXRot(f4);
                float f5 = NueEntity.this.getYRot() + 90.0F;
                double d6 = (double)(this.speed * Mth.cos(f5 * ((float)Math.PI / 180F))) * Math.abs(d0 / d5);
                double d7 = (double)(this.speed * Mth.sin(f5 * ((float)Math.PI / 180F))) * Math.abs(d2 / d5);
                double d8 = (double)(this.speed * Mth.sin(f4 * ((float)Math.PI / 180F))) * Math.abs(d1 / d5);
                Vec3 vec3 = NueEntity.this.getDeltaMovement();
                NueEntity.this.setDeltaMovement(vec3.add((new Vec3(d6, d8, d7)).subtract(vec3).scale(0.2D)));
            }
        }
    }
}
