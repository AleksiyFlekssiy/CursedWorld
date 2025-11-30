package com.aleksiyflekssiy.tutorialmod.entity.behavior.greatserpent;

import com.aleksiyflekssiy.tutorialmod.entity.GreatSerpentEntity;
import com.aleksiyflekssiy.tutorialmod.entity.GreatSerpentSegment;
import com.aleksiyflekssiy.tutorialmod.entity.Shikigami;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SegmentFollow extends Behavior<GreatSerpentSegment> {

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
                return previousSegment.getNavigation().getPath() != null;
            }
            else {
                return segment.getParent().getNavigation().getPath() != null;
            }
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, GreatSerpentSegment segment, long pGameTime) {
        if (segment.getBrain().getMemory(MemoryModuleType.WALK_TARGET).isEmpty()) return false;
        WalkTarget walkTarget = segment.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get();
        BlockPos targetPos = walkTarget.getTarget().currentBlockPosition();
//        if (segment.blockPosition().distManhattan(new Vec3i(targetPos.getX(), targetPos.getY(), targetPos.getZ())) <= 1) {
//            System.out.println("Small distance" + segment.getIndex());
//            return false;
//        }
        //Без этой хрени всё работает идеально? Сегменты в больших всё же остаются, но каруселей нет.
        //Проблема усугубляется при застревании и малых путях. Путей на всех не хватает. Сегмент получает на 2 нода меньше предыдущего.
        return true;
    }

    @Override
    protected void tick(ServerLevel level, GreatSerpentSegment segment, long pGameTime) {

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
        Path newPath = new Path(nodes,
                target,
                false);

        segment.getBrain().setMemory(MemoryModuleType.PATH, newPath);
        segment.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, 1, 2));
        segment.getNavigation().moveTo(newPath, 1);
    }

    private BlockPos setTargetAndNodes(GreatSerpentSegment segment, BlockPos target, List<Node> nodes, Path previousPath) {
        int nodeCount = calculateLastPathNode(previousPath);

        if (previousPath.getNodeCount() >= 1) {
            //Это решило как-то проблему нехватки нодов. Иногда сам родитель застревает, ломая этим всё (опять карусель).
            //Надо фиксить мувмент родителя.
            Path firstPart = segment.getNavigation().createPath(previousPath.getNodePos(0), 1);
            for (int i = 0; i < firstPart.getNodeCount(); i++) {
                nodes.add(firstPart.getNode(i));
            }
        }

            if (nodeCount > 0) {
                for (int i = 0; i < nodeCount; i++) {
                    nodes.add(previousPath.getNode(i));
                }
                Node targetNode = previousPath.getNode(nodeCount);
                target = targetNode.asBlockPos();
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
    }

    private int calculateLastPathNode(Path path) {
        if (path == null) return 0;

        int nodeCount = path.getNodeCount();
        int lastPathNodeIndex = nodeCount - 1;
        int lastPathNodeIndexForNextSegment = lastPathNodeIndex - 1;
        //Допустим от точки спавна до родительской цели 10 точек (итого 12)
        //Родитель их проходит, следующему сегменту нужно дойти до предпоследней точки родителя.
        //Каждый следующий сегмент делает своей целью предпоследнюю точку пути предыдущего сегмента
        //Последний сегмент по сути должен только заспавниться без передвижения.

        return Math.max(0, lastPathNodeIndexForNextSegment);
    }
}
