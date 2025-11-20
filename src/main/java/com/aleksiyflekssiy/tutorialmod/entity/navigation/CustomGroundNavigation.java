package com.aleksiyflekssiy.tutorialmod.entity.navigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class CustomGroundNavigation extends GroundPathNavigation {
    private int lastReachedNodeIndex = -1; // Индекс последнего достигнутого узла

    public CustomGroundNavigation(@NotNull Mob entity, Level world) {
        super(entity, world);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new WalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        return new MMPathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    @Override
    protected void followThePath() {
        if (this.path == null || this.path.isDone()) {
            return;
        }

        // Текущая позиция моба
        Vec3 mobPos = this.getTempMobPos();
        double mobX = mobPos.x;
        double mobY = mobPos.y;
        double mobZ = mobPos.z;

        // Максимальное расстояние до узла
        float maxDistanceToWaypointSquared = 1.0F;

        // Текущий узел
        int currentIndex = this.path.getNextNodeIndex();
        if (currentIndex >= this.path.getNodeCount()) {
            this.path = null; // Путь завершён или индекс некорректен
            return;
        }

        Node currentNode = this.path.getNode(currentIndex); // Используем getNode вместо getNextNode
        Vec3 targetPos = new Vec3(currentNode.x + 0.5, currentNode.y, currentNode.z + 0.5);

        // Расстояние до текущего узла
        double dx = targetPos.x - mobX;
        double dy = targetPos.y - mobY;
        double dz = targetPos.z - mobZ;
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        double horizontalDistanceSquared = dx * dx + dz * dz;

        // Проверка направления
        Vec3 direction = mobPos.subtract(targetPos).normalize();
        boolean passedNode = this.shouldTargetNextNodeInDirection(direction);

        // Если узел достигнут или пройден
        if (distanceSquared < maxDistanceToWaypointSquared || (passedNode && horizontalDistanceSquared < 1.0)) {
            if (currentIndex > lastReachedNodeIndex) {
                lastReachedNodeIndex = currentIndex;
            }
            if (currentIndex < this.path.getNodeCount() - 1) {
                this.path.advance();
                currentIndex = this.path.getNextNodeIndex();
                if (currentIndex >= this.path.getNodeCount()) {
                    this.path = null; // Путь завершён
                    return;
                }
                currentNode = this.path.getNode(currentIndex);
                targetPos = new Vec3(currentNode.x + 0.5, currentNode.y, currentNode.z + 0.5);
                dx = targetPos.x - mobX;
                dy = targetPos.y - mobY;
                dz = targetPos.z - mobZ;
                distanceSquared = dx * dx + dy * dy + dz * dz;
                horizontalDistanceSquared = dx * dx + dz * dz;
            } else {
                this.path = null; // Путь завершён
                return;
            }
        }

        // Предотвращаем возврат к старым узлам V2
        if (currentIndex <= lastReachedNodeIndex && currentIndex < this.path.getNodeCount() - 1) {
            // Проверяем следующий узел перед переключением
            int nextIndex = currentIndex + 1;
            Node nextNode = this.path.getNode(nextIndex);
            Vec3 nextPos = new Vec3(nextNode.x + 0.5, nextNode.y, nextNode.z + 0.5);

            double nextDx = nextPos.x - mobX;
            double nextDy = nextPos.y - mobY;
            double nextDz = nextPos.z - mobZ;
            double nextDistanceSquared = nextDx * nextDx + nextDy * nextDy + nextDz * nextDz;
            double nextHorizontalDistanceSquared = nextDx * nextDx + nextDz * nextDz;

            float stepHeight = nextDy < 0 ? 1.0F : 1.0F; // Максимальная высота шага моба
            float maxHorizontalReach = 2.0F; // Максимальное горизонтальное расстояние до следующего узла

            // Переключаемся только если следующий узел достижим
            if (Math.abs(nextDy) <= stepHeight && nextHorizontalDistanceSquared <= maxHorizontalReach * maxHorizontalReach) {
                this.path.advance();
                currentIndex = this.path.getNextNodeIndex();
                if (currentIndex >= this.path.getNodeCount()) {
                    this.path = null;
                    return;
                }
                currentNode = this.path.getNode(currentIndex);
                targetPos = new Vec3(currentNode.x + 0.5, currentNode.y, currentNode.z + 0.5);
                dx = targetPos.x - mobX;
                dy = targetPos.y - mobY;
                dz = targetPos.z - mobZ;
                distanceSquared = dx * dx + dy * dy + dz * dz;
                horizontalDistanceSquared = dx * dx + dz * dz;
            }
        }
    }

    protected boolean shouldTargetNextNodeInDirection(Vec3 direction) {
        if (this.path == null || this.path.isDone()) {
            return false;
        }
        int currentIndex = this.path.getNextNodeIndex();
        if (currentIndex >= this.path.getNodeCount()) {
            return false;
        }
        Node currentNode = this.path.getNode(currentIndex);
        Vec3 targetPos = new Vec3(currentNode.x + 0.5, currentNode.y, currentNode.z + 0.5);
        Vec3 toTarget = targetPos.subtract(this.getTempMobPos());
        double dotProduct = direction.dot(toTarget);
        return dotProduct < 0 && toTarget.lengthSqr() < 2.0;
    }

    @Override
    protected Vec3 getTempMobPos() {
        return this.mob.position();
    }

    @Override
    protected boolean canUpdatePath() {
        return true;
    }

    @Override
    public boolean moveTo(double x, double y, double z, double speedIn) {
        boolean result = super.moveTo(x, y, z, speedIn);
        if (result) {
            lastReachedNodeIndex = -1; // Сбрасываем при новом пути
        }
        return result;
    }
}
