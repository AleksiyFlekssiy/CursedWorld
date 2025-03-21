package com.aleksiyflekssiy.tutorialmod.entity;

import com.aleksiyflekssiy.tutorialmod.entity.goal.ShikigamiMeleeAttackGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class DivineDogEntity extends PathfinderMob {

    public float targetYaw = 0.0F;
    public LivingEntity lastTarget = null; // Сохраняем последнюю цель
    private final List<BlockPos> visualizedPath = new ArrayList<>(); // Список для хранения позиций блоков пути
    private final List<BlockState> originalBlockStates = new ArrayList<>(); // Список для хранения оригинальных состояний блоков
    private boolean isVisualizingPath = true; // Флаг для включения/выключения визуализации
    private static final EntityDataAccessor<Float> REAL_SPEED = SynchedEntityData.defineId(DivineDogEntity.class, EntityDataSerializers.FLOAT);
    private Node node;

    protected DivineDogEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new CustomMoveControl(this);
        this.entityData.set(REAL_SPEED, 0.7F);
        //this.setMaxUpStep(1);
        //this.navigation.stop();
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
        //customNav();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(REAL_SPEED, 0.7F);
    }

    public float getRealSpeed() {
        return entityData.get(REAL_SPEED);
    }

    private void customNav() {
        // Определяем цель
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            this.lastTarget = target;
        }

        // Если цель есть или была
        if (this.lastTarget != null && this.lastTarget.isAlive()) {
            double dx = this.lastTarget.getX() - this.getX();
            double dy = this.lastTarget.getY() - this.getY();
            double dz = this.lastTarget.getZ() - this.getZ();
            double distanceXZ = Math.sqrt(dx * dx + dz * dz);
            double distanceTotal = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distanceTotal > 2.0D) {
                // Движение по горизонтали
                double speed = this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 2.5;
                double moveX = dx / distanceXZ * speed;
                double moveZ = dz / distanceXZ * speed;

                // Вертикальное перемещение
                double moveY = this.getDeltaMovement().y;
                if (dy > 1 && this.onGround()) { // Прыжок, если цель выше
                    moveY = 0.42F; // Скорость прыжка
                } else if (!this.onGround()) { // Падение
                    moveY -= 0.08; // Гравитация
                    moveY *= 0.98; // Сопротивление
                }

                // Проверка препятствий впереди
                Vec3 moveVec = new Vec3(moveX, 0, moveZ).normalize().scale(2.0);
                if (!this.level().getBlockState(this.blockPosition().offset((int) moveVec.x, 0, (int) moveVec.z)).isAir() &&
                        this.level().getBlockState(this.blockPosition().offset((int) moveVec.x, 1, (int) moveVec.z)).isAir()) {
                    moveY = 0.42F; // Прыжок через препятствие
                }

                this.setDeltaMovement(moveX, moveY, moveZ);

                // Обновляем целевой угол
                this.targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
            } else {
                // Остановка и атака
                this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
                if (this.distanceToSqr(this.lastTarget) < 4.0D) {
                    this.doHurtTarget(this.lastTarget);
                }
            }

            // Плавное сглаживание поворота с учетом кратчайшего пути
            float currentYaw = this.getYRot();
            float deltaYaw = Mth.wrapDegrees(this.targetYaw - currentYaw);
            float smoothedYaw = currentYaw + Mth.clamp(deltaYaw, -15.0F, 15.0F); // Ограничиваем скорость поворота
            this.setYRot(smoothedYaw);
            this.yBodyRot = smoothedYaw;
            this.yHeadRot = smoothedYaw;
        } else {
            // Если цели нет, останавливаемся
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
            this.targetYaw = this.getYRot();
        }

        // Отладка
        System.out.println("Distance: " + (this.lastTarget != null ? this.distanceTo(this.lastTarget) : "No target") +
                " | Pos: " + this.getX() + ", " + this.getZ() +
                " | YRot: " + this.getYRot() + " | TargetYaw: " + this.targetYaw);
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
        this.goalSelector.addGoal(1, new FloatGoal(this)); // Чтобы не тонул в воде
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 2D, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 30)
                .add(Attributes.MOVEMENT_SPEED, 0.33)
                .add(Attributes.ATTACK_DAMAGE, 5f)
                .add(Attributes.FOLLOW_RANGE, 100)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 1)
                .add(Attributes.ARMOR_TOUGHNESS, 1)
                .add(Attributes.JUMP_STRENGTH, 1);
    }

    class CustomMoveControl extends MoveControl {
        private Vec3 currentPos = null;
        private Path path = mob.getNavigation().getPath();
        private int index = 0;

        public CustomMoveControl(Mob pMob) {
            super(pMob);
        }

        public void tick() {
            if (this.operation == Operation.STRAFE) {
                float f = (float) this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
                float f1 = (float) this.speedModifier * f;
                float f2 = this.strafeForwards;
                float f3 = this.strafeRight;
                float f4 = Mth.sqrt(f2 * f2 + f3 * f3);
                if (f4 < 1.0F) {
                    f4 = 1.0F;
                }

                f4 = f1 / f4;
                f2 *= f4;
                f3 *= f4;
                float f5 = Mth.sin(this.mob.getYRot() * ((float) Math.PI / 180F));
                float f6 = Mth.cos(this.mob.getYRot() * ((float) Math.PI / 180F));
                float f7 = f2 * f6 - f3 * f5;
                float f8 = f3 * f6 + f2 * f5;
                if (!this.isWalkable(f7, f8)) {
                    this.strafeForwards = 1.0F;
                    this.strafeRight = 0.0F;
                }

                this.mob.setSpeed(f1);
                this.mob.setZza(this.strafeForwards);
                this.mob.setXxa(this.strafeRight);
                this.operation = Operation.WAIT;
            } else if (this.operation == Operation.MOVE_TO) {
                this.operation = Operation.WAIT;
                double d0 = this.wantedX - this.mob.getX();
                double d1 = this.wantedZ - this.mob.getZ();
                double d2 = this.wantedY - this.mob.getY();
                double d3 = d0 * d0 + d2 * d2 + d1 * d1;
                if (d3 < (double) 2.5000003E-7F) {
                    this.mob.setZza(0.0F);
                    return;
                }
                if (!new Vec3(wantedX,wantedY,wantedZ).equals(currentPos)){
                    index++;
                }
                //изначальное
                float currentYaw = mob.getYRot();
                if (mob.getTarget() != null) {
                    // Плавные повороты при спуске вниз
                    if (mob.getY() > mob.getTarget().getY()) { // Условие спуска
                        float targetAngle = (float) (Mth.atan2(d1, d0) * (180.0 / Math.PI)) - 90.0F;
                        float deltaYaw = Mth.wrapDegrees(targetAngle - currentYaw);
                        float maxTurn = 15.0F; // Максимальный шаг поворота
                        float smoothedYaw = currentYaw + deltaYaw;
                        this.mob.setYRot(smoothedYaw);
                        mob.level().setBlock(new BlockPos((int) wantedX, (int) wantedY, (int) wantedZ), Blocks.BIRCH_SIGN.defaultBlockState(), 3);
                    } else if (mob.getY() != mob.getTarget().getY()) { // Подъём или другое изменение высоты
                        float targetAngle = (float) (Mth.atan2(d1, d0) * (180.0 / Math.PI)) - 90.0F;
                        float deltaYaw = Mth.wrapDegrees(targetAngle - currentYaw);
                        //if (index % 2 == 0) { // Оставляем старую логику для подъёма
                            this.mob.setYRot(currentYaw + deltaYaw);
                        //}
                        mob.level().setBlock(new BlockPos((int) wantedX, (int) wantedY, (int) wantedZ), Blocks.BIRCH_SIGN.defaultBlockState(), 3);
                    }
                } else {
                    // Логика без цели (оставляем как есть)
                    float deltaYaw = Mth.wrapDegrees(DivineDogEntity.this.targetYaw - currentYaw);
                    float smoothedYaw = currentYaw + Mth.clamp(deltaYaw, -15.0F, 15.0F);
                    mob.setYRot(smoothedYaw);
                }
                this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                BlockPos blockpos = this.mob.blockPosition();
                BlockState blockstate = this.mob.level().getBlockState(blockpos);
                VoxelShape voxelshape = blockstate.getCollisionShape(this.mob.level(), blockpos);
                if (d2 > (double) this.mob.getStepHeight() && d0 * d0 + d1 * d1 < (double) Math.max(1.0F, this.mob.getBbWidth()) || !voxelshape.isEmpty() && this.mob.getY() < voxelshape.max(Direction.Axis.Y) + (double) blockpos.getY() && !blockstate.is(BlockTags.DOORS) && !blockstate.is(BlockTags.FENCES)) {
                    this.mob.getJumpControl().jump();
                    this.operation = Operation.JUMPING;
                }
            } else if (this.operation == Operation.JUMPING) {
                this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                if (this.mob.onGround()) {
                    this.operation = Operation.WAIT;
                }
            } else {
                this.mob.setZza(0.0F);
            }
        }
        private boolean isWalkable(float pRelativeX, float pRelativeZ){
            PathNavigation pathnavigation = this.mob.getNavigation();
            if (pathnavigation != null) {
                NodeEvaluator nodeevaluator = pathnavigation.getNodeEvaluator();
                if (nodeevaluator != null && nodeevaluator.getBlockPathType(this.mob.level(), Mth.floor(this.mob.getX() + (double) pRelativeX), this.mob.getBlockY(), Mth.floor(this.mob.getZ() + (double) pRelativeZ)) != BlockPathTypes.WALKABLE) {
                    return false;
                }
            }
            return true;
        }
    }
}



