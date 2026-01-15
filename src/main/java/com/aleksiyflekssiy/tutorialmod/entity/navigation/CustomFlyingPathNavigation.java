package com.aleksiyflekssiy.tutorialmod.entity.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class CustomFlyingPathNavigation extends FlyingPathNavigation {
    private static final float MAX_ANGLE_DEG = 30.0F; // Макс угол между нодами
    private static final double MIN_INTERP_DIST = 0.5D; // Мин расстояние для вставки ноды

    public CustomFlyingPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    public Path createPath(BlockPos pos, int accuracy) {
        Path rawPath = super.createPath(pos, accuracy); // Vanilla путь
        if (rawPath == null || rawPath.getNodeCount() < 3) return rawPath;

        // Сглаживаем путь
        List<Node> smoothedNodes = smoothNodes(getNodes(rawPath));
        return new Path(smoothedNodes, rawPath.getTarget(), rawPath.canReach());
    }

    private List<Node> getNodes(Path path){
        List<Node> smoothedNodes = new ArrayList<>();
        for (int i = 0; i < path.getNodeCount(); i++){
            smoothedNodes.add(path.getNode(i));
        }
        return smoothedNodes;
    }

    @Override
    public Path createPath(Entity entity, int accuracy) {
        Path rawPath = super.createPath(entity, accuracy);
        if (rawPath == null || rawPath.getNodeCount() < 3) return rawPath;

        List<Node> smoothedNodes = smoothNodes(getNodes(rawPath));
        return new Path(smoothedNodes, rawPath.getTarget(), rawPath.canReach());
    }

    private List<Node> smoothNodes(List<Node> originalNodes) {
        List<Node> smoothed = new ArrayList<>();
        smoothed.add(originalNodes.get(0)); // Первая нода

        for (int i = 1; i < originalNodes.size() - 1; i++) {
            Node prev = smoothed.get(smoothed.size() - 1);
            Node curr = originalNodes.get(i);
            Node next = originalNodes.get(i + 1);

            Vec3 prevPos = Vec3.atCenterOf(prev.asBlockPos());
            Vec3 currPos = Vec3.atCenterOf(curr.asBlockPos());
            Vec3 nextPos = Vec3.atCenterOf(next.asBlockPos());

            // Векторы для угла
            Vec3 dir1 = currPos.subtract(prevPos).normalize();
            Vec3 dir2 = nextPos.subtract(currPos).normalize();

            double dot = Mth.clamp(dir1.dot(dir2), -1.0, 1.0);
            double angleDeg = Math.acos(dot) * 180.0 / Math.PI;

            smoothed.add(curr); // Добавляем текущую

            if (angleDeg > MAX_ANGLE_DEG) {
                // Разбиваем на шаги
                int steps = (int) Math.ceil(angleDeg / MAX_ANGLE_DEG);
                double stepDist = currPos.distanceTo(nextPos) / steps;

                for (int s = 1; s < steps; s++) {
                    double t = (double) s / steps;
                    Vec3 interpPos = currPos.lerp(nextPos, t);
                    if (interpPos.distanceTo(smoothed.get(smoothed.size() - 1).asVec3()) >= MIN_INTERP_DIST) {
                        BlockPos interpBlock = BlockPos.containing(interpPos);
                        Node interpNode = new Node(interpBlock.getX(), interpBlock.getY(), interpBlock.getZ());
                        smoothed.add(interpNode);
                    }
                }
            }
        }

        smoothed.add(originalNodes.get(originalNodes.size() - 1)); // Последняя
        return smoothed;
    }
}
