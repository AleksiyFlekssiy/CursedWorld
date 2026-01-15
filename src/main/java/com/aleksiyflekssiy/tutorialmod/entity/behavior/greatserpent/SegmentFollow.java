package com.aleksiyflekssiy.tutorialmod.entity.behavior.greatserpent;

import com.aleksiyflekssiy.tutorialmod.entity.GreatSerpentEntity;
import com.aleksiyflekssiy.tutorialmod.entity.GreatSerpentSegment;
import com.aleksiyflekssiy.tutorialmod.entity.ModEntities;
import com.aleksiyflekssiy.tutorialmod.entity.Shikigami;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class SegmentFollow extends Behavior<GreatSerpentSegment> {
    public static final Map<UUID, Path> PARENT_PATHS = new HashMap<>();
    BlockPos maybetarget = null;
    private static final double FIXED_DIST = 2; // Фиксированное расстояние
    private static final double LERP_SPEED = 0.15; // 0.1-0.2 для плавности
    private static final double MAX_DIST = FIXED_DIST * 2; // Критическая разрыв для телепорта

    public SegmentFollow(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition) {
        super(pEntryCondition, 0, 72000);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, GreatSerpentSegment segment) {
        if (segment.getParent() == null) {
            System.out.println("No parent");
            return false;
        }
        else {
            List<GreatSerpentSegment> segments = segment.getParent().getSegments();
            int index = segment.getIndex();
            if (index > 0) {
                if (index - 1 >= segments.size()) return false;
                GreatSerpentSegment previousSegment = segments.get(index - 1);
                return previousSegment.getNavigation().getPath() != null && isFarFromLeader(segment);
            }
            else {
                Path parentPath = segment.getParent().getNavigation().getPath();
                if (parentPath != null) {
                    PARENT_PATHS.put(segment.getParent().getUUID(), parentPath);
                    return isFarFromLeader(segment);
                }
                return false;
            }
        }
    }

    public Path getParentPath(UUID uuid) {
        if (PARENT_PATHS.containsKey(uuid)) {
            return PARENT_PATHS.get(uuid);
        }
        return null;
    }

    private boolean isParentPathRecomputed(GreatSerpentSegment segment) {
        GreatSerpentEntity parent = segment.getParent();
        if (parent.getNavigation().getPath() == null) return false;
        Path parentPath = getParentPath(parent.getUUID());
        if (parentPath == null) return false;
        return !parentPath.equals(segment.getParent().getNavigation().getPath());
    }

    @Override
    protected boolean canStillUse(ServerLevel level, GreatSerpentSegment segment, long pGameTime) {
        if (segment.getBrain().getMemory(MemoryModuleType.WALK_TARGET).isEmpty()) return false;
        if (isParentPathRecomputed(segment)) return false;

        //Надо сделать отслеживание застревания родителя. //Вообще, змея - двигается полностью, а не по частям.
        //Так что двигаются либо все, либо никто
        WalkTarget walkTarget = segment.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get();
        BlockPos targetPos = walkTarget.getTarget().currentBlockPosition();
        //Код возможно правильный, но неверная постановка таргета все руинит. Это главная проблема.
        if (segment.blockPosition().distManhattan(new Vec3i(targetPos.getX(), targetPos.getY(), targetPos.getZ())) <= 1) {
            System.out.println("Small distance" + segment.getIndex());
            return false;
        }
        //Без этой хрени всё работает идеально? Сегменты в больших всё же остаются, но каруселей нет.
        //Проблема усугубляется при застревании и малых путях. Путей на всех не хватает. Сегмент получает на 2 нода меньше предыдущего.
        return true;
    }

    @Override
    protected void tick(ServerLevel level, GreatSerpentSegment segment, long pGameTime) {
        //Происходит подрыв структуры при стопе, хз почему
        //Ничего не помогает, появляются разрывы, слишком сильное наслоение.
        LivingEntity leader = segment.getIndex() == 0 ? segment.getParent() : segment.getParent().getSegments().get(segment.getIndex() - 1);
        Vec3 leaderPos = leader.position().add(0, leader.getBbHeight() / 2, 0); // Центр лидера для точности
        Vec3 current = segment.position().add(0, segment.getBbHeight() / 2, 0);
        double distSqr = current.distanceToSqr(leaderPos);

        if (FIXED_DIST * FIXED_DIST > distSqr) { // Только если > фиксированного
            int index = segment.getNavigation().getPath().getNodeCount();
            if (segment.getNavigation().getPath().getNextNodeIndex() < index - 1 && isFarFromLeader(segment)) {
                Vec3 nextPos = segment.getNavigation().getPath().getNextEntityPos(segment);
                Vec3 dir = current.subtract(nextPos).normalize(); // Dir от лидера к сегменту (для offset назад)
                Vec3 target = nextPos.add(dir.scale(FIXED_DIST)); // Точная позиция на FIXED_DIST позади

                Vec3 delta = target.subtract(current).scale(LERP_SPEED);

                // Collision check
                AABB newBB = segment.getBoundingBox().move(delta);
                if (level.noCollision(segment, newBB)) {
                    segment.move(MoverType.SELF, delta); // Move вместо setPos — учитывает физику
                } else if (distSqr > MAX_DIST * MAX_DIST) { // Разрыв критический: Телепорт без check
                    segment.setPos(target);
                }

                // Поворот к лидеру
                float targetYaw = (float) Mth.atan2(dir.z, dir.x) * Mth.RAD_TO_DEG - 90F;
                segment.setYRot(Mth.rotLerp(0.3F, segment.getYRot(), targetYaw));
                float targetPitch = (float) (Mth.atan2(dir.y, dir.horizontalDistance()) * Mth.RAD_TO_DEG);
                segment.setXRot(Mth.rotLerp(0.3F, segment.getXRot(), targetPitch));
            }
        }
//        }
    }

    @Override
    protected void start(ServerLevel pLevel, GreatSerpentSegment segment, long pGameTime) {
        GreatSerpentEntity parent = segment.getParent();

        int index = segment.getIndex();

        BlockPos target = null;

        List<Node> nodes = new ArrayList<>();
        //Можно попробовать получать пути сегмента и к первой точке создавать еще один путь.
        if (index == 0){
            Path parentPath = parent.getNavigation().getPath();
            target = setTargetAndNodes(segment, target, nodes, parentPath);
        }
        else {
            Path previousPath = parent.getSegments().get(index - 1).getNavigation().getPath();
            target = setTargetAndNodes(segment, target, nodes, previousPath);
        }

        if (target == null) {
            this.stop(pLevel, segment, pGameTime);
            return;
        }
        System.out.println(target);

        Path newPath = new Path(nodes,
                target,
                false);

        segment.getBrain().setMemory(MemoryModuleType.PATH, newPath);
        segment.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, 1, 2));
        segment.getNavigation().moveTo(newPath, 1);
        if (maybetarget.equals(segment.getNavigation().getTargetPos())) System.out.println("Target the same");
        else System.out.println("I dunno " + segment.getNavigation().getTargetPos());
        System.out.println("Point: " + segment.getNavigation().getPath().getEndNode().asBlockPos());
    }

    private BlockPos setTargetAndNodes(GreatSerpentSegment segment, BlockPos target, List<Node> nodes, Path previousPath) {
        //Нодкаунт равен индексу последнего нода для этого сегмента
        int nodeCount = calculateLastPathNode(previousPath);

        //Если есть путь у предыдущего, то мы создаем путь к начальной точке для следующего
        if (previousPath.getNodeCount() >= 1) {
            //Это решило как-то проблему нехватки нодов. Иногда сам родитель застревает, ломая этим всё (опять карусель).
            //Надо фиксить мувмент родителя.
            GreatSerpentSegment copy = new GreatSerpentSegment(ModEntities.GREAT_SERPENT_SEGMENT.get(), segment.level());
            copy.setPos(segment.blockPosition().getCenter());
            Path firstPart = copy.getNavigation().createPath(previousPath.getNodePos(0), 1);
            maybetarget = firstPart.getTarget();
            for (int i = 0; i < firstPart.getNodeCount(); i++) {
                nodes.add(firstPart.getNode(i));
            }
        }

            if (nodeCount > 0) {
                for (int i = 0; i < nodeCount; i++) {
                    nodes.add(previousPath.getNode(i));
                }
                Node targetNode = previousPath.getEndNode(); // -2, чтобы не догонять
                return new BlockPos(0, (int) segment.getY(), 0);
            }
        //}
        return target;
    }

    @Override
    protected void stop(ServerLevel pLevel, GreatSerpentSegment segment, long pGameTime) {
        System.out.println("STOP");
        segment.getNavigation().stop();
        segment.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        segment.getBrain().eraseMemory(MemoryModuleType.PATH);
        maybetarget = null;
    }

    private int calculateLastPathNode(Path path) {
        //Берем путь
        if (path == null) return 0;

        //Берем кол-во нодов, находим индекс последнего, уменьшаем на один = индекс финальной ноды для следующего.
        int nodeCount = path.getNodeCount();
        int lastPathNodeIndex = nodeCount - 1;
        int lastPathNodeIndexForNextSegment = lastPathNodeIndex - 1;
        //Допустим от точки спавна до родительской цели 10 точек (итого 12)
        //Родитель их проходит, следующему сегменту нужно дойти до предпоследней точки родителя.
        //Каждый следующий сегмент делает своей целью предпоследнюю точку пути предыдущего сегмента
        //Последний сегмент по сути должен только заспавниться без передвижения.

        return Math.max(0, lastPathNodeIndexForNextSegment);
    }

    private boolean isFarFromLeader(GreatSerpentSegment segment) {
        LivingEntity leader = segment.getIndex() == 0 ? segment.getParent() : segment.getParent().getSegments().get(segment.getIndex() - 1);
        Vec3 leaderPos = leader.position().add(0, leader.getBbHeight() / 2, 0); // Центр лидера для точности
        Vec3 current = segment.position().add(0, segment.getBbHeight() / 2, 0);
        double distSqr = current.distanceToSqr(leaderPos);

        return distSqr >= FIXED_DIST * FIXED_DIST;
    }
}
