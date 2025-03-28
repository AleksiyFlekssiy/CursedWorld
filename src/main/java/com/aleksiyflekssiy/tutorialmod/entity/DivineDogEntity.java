package com.aleksiyflekssiy.tutorialmod.entity;

import com.aleksiyflekssiy.tutorialmod.entity.navigation.CustomNavigation;
import com.aleksiyflekssiy.tutorialmod.entity.navigation.SmartBodyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DivineDogEntity extends TamableAnimal implements OwnableEntity{
    public enum Color{
        WHITE,
        BLACK
    }
    public float targetYaw = 0.0F;
    private final List<BlockPos> visualizedPath = new ArrayList<>(); // Список для хранения позиций блоков пути
    private final List<BlockState> originalBlockStates = new ArrayList<>(); // Список для хранения оригинальных состояний блоков
    private boolean isVisualizingPath = true; // Флаг для включения/выключения визуализации
    private boolean isTamed;
    private static final EntityDataAccessor<Float> REAL_SPEED = SynchedEntityData.defineId(DivineDogEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(DivineDogEntity.class, EntityDataSerializers.INT);

    protected DivineDogEntity(EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        //this.moveControl = new CustomMoveControl(this, this);
        this.entityData.set(REAL_SPEED, 0.33F);
        this.setMaxUpStep(1);

    }

    public DivineDogEntity(EntityType<? extends TamableAnimal> entityType, Level level, Player owner, boolean isTamed){
        super(entityType,level);
        this.isTamed = isTamed;
        System.out.println("Is tamed: " + isTamed);
        System.out.println("UUID: " + getStringUUID());
        setTame(isTamed);
        if (isTamed) {
            tame(owner);
            this.registerGoals();
        }
        entityData.set(COLOR, Color.WHITE.ordinal());
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity target = this.getTarget();
        if (target == null) return;
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        if (this.isVisualizingPath) {
            visualizePath();
        }
        if (this.navigation.getPath() != null) entityData.set(REAL_SPEED, 0.33f * 2.5f);
        else entityData.set(REAL_SPEED, 0.33F);
    }

//    @Override
//    protected BodyRotationControl createBodyControl() {
//        return new SmartBodyHelper(this);
//    }

    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        return new CustomNavigation(this, level());
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
        return null;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(REAL_SPEED, 0.7F);
        entityData.define(COLOR, Color.WHITE.ordinal());
    }

    public float getRealSpeed() {
        return entityData.get(REAL_SPEED);
    }

    public void setColor(Color color){
        this.entityData.set(COLOR, color.ordinal());
    }

    public Color getColor(){
        return Color.values()[entityData.get(COLOR)];
    }

    // Метод для включения визуализации пути
    public void startPathVisualization() {
        this.isVisualizingPath = true;
    }

    // Метод для выключения визуализации пути
    public void stopPathVisualization() {
        this.isVisualizingPath = false;
        revertPathBlocks(); // Возвращаем блоки к исходному состоянию при выключении
    }

    // Метод для временной замены блоков пути
    private void visualizePath() {
        PathNavigation navigation = this.getNavigation();
        Path currentPath = navigation.getPath();
        BlockPos wanted = new BlockPos((int) getMoveControl().getWantedX(), (int) getMoveControl().getWantedY(), (int) getMoveControl().getWantedZ());
        if (currentPath != null) {
            // Очищаем предыдущую визуализацию
            revertPathBlocks();
            visualizedPath.clear();
            originalBlockStates.clear();

            for (int i = 0; i < currentPath.getNodeCount(); ++i) {
                Node node = currentPath.getNode(i);
                BlockPos pos = node.asBlockPos();

                // Проверяем, что позиция находится в границах мира
                if (level().isLoaded(pos)) {
                    visualizedPath.add(pos.immutable()); // Добавляем неизменяемую копию BlockPos
                    originalBlockStates.add(level().getBlockState(pos)); // Сохраняем оригинальное состояние

                    // Заменяем блок на светящийся камень (можно выбрать любой другой яркий блок)
                    level().setBlock(pos, Blocks.ACACIA_SIGN.defaultBlockState(), 3); // Флаг 3 означает обновление клиента
                }
            }
            if (!wanted.equals(BlockPos.ZERO)) {
                visualizedPath.add(wanted.immutable());
                originalBlockStates.add(level().getBlockState(wanted));
                level().setBlock(wanted, Blocks.BIRCH_SIGN.defaultBlockState(), 3);
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
    }

    // Метод для возвращения блоков пути к их исходному состоянию
    private void revertPathBlocks() {
        for (int i = 0; i < visualizedPath.size(); ++i) {
            BlockPos pos = visualizedPath.get(i);
            BlockState originalState = originalBlockStates.get(i);
            if (level().isLoaded(pos)) {
                level().setBlock(pos, originalState, 3);
            }
        }
        visualizedPath.clear();
        originalBlockStates.clear();
    }

    @Override
    protected void registerGoals() {
        if (!isTamed) {
            this.goalSelector.addGoal(1, new FloatGoal(this)); // Чтобы не тонул в воде
            this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 2D, false));
            this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
        }
        else {
            this.goalSelector.removeAllGoals(filter -> true);
            this.targetSelector.removeAllGoals(filter -> true);
            this.goalSelector.addGoal(0, new FloatGoal(this));
            this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 2f, true));
            this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1, 5, 5, false));
            this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
            this.targetSelector.addGoal(2, new OwnerHurtByTargetGoal(this));
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        if (fallDistance <= 5.0F) { // Безопасная высота 10 блоков
            this.fallDistance = 0.0F; // Сбрасываем дистанцию падения
            return false; // Отменяем урон
        }
        // Для падений больше 10 блоков считаем урон как обычно, но с вычетом 10 блоков
        float reducedDistance = fallDistance - 5.0F;
        if (reducedDistance > 0) {
            return super.causeFallDamage(reducedDistance, damageMultiplier, damageSource);
        }
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 60)
                .add(Attributes.MOVEMENT_SPEED, 0.33)
                .add(Attributes.ATTACK_DAMAGE, 5f)
                .add(Attributes.FOLLOW_RANGE, 100)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 1)
                .add(Attributes.ARMOR_TOUGHNESS, 2.5)
                .add(Attributes.JUMP_STRENGTH, 1);
    }
}