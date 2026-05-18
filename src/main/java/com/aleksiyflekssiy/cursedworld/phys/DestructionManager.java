package com.aleksiyflekssiy.cursedworld.phys;

import com.aleksiyflekssiy.cursedworld.CursedWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = CursedWorld.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DestructionManager {
    private static final Map<Entity, Vec3> trackedEntities = new HashMap<>();
    private static final Random RANDOM = new Random();
    private static final int DIFF_ANGLE = 90;
    public static final double DESTRUCTION_MULTIPLIER = 1.35;

    public static void trackEntity(Entity entity){
        trackedEntities.put(entity, entity.getDeltaMovement());
    }

    private static void makeImpact(Entity entity, Vec3 position, Vec3 impulse, CollisionType collisionType){
        int power = (int) Math.round(impulse.lengthSqr() * (entity.getBbHeight() + entity.getBbWidth()));
        System.out.println("Power: " + power);
        BlockPos center = BlockPos.containing(position);
        int centerX = center.getX();
        int centerY = center.getY();
        int centerZ = center.getZ();
        int diff;
        int halfWidth = (int) Math.ceil(entity.getBbWidth());
        int halfHeight = (int) Math.ceil(entity.getBbHeight());
        Iterable<BlockPos> blockPoses;
        if (collisionType == CollisionType.HORIZONTAL) {
            diff = (int) Math.floor(entity.getBbHeight() * 0.75);
            blockPoses = BlockPos.betweenClosed(
                    new BlockPos(centerX - halfWidth, centerY - diff, centerZ - halfWidth),
                    new BlockPos(centerX + halfWidth, centerY + diff, centerZ + halfWidth)
            );
        }
        else if (collisionType == CollisionType.VERTICAL){
            diff = (int) Math.ceil(entity.getBbWidth() * 0.75);
            blockPoses = BlockPos.betweenClosed(
                    new BlockPos(centerX - diff, centerY - halfHeight, centerZ - diff),
                    new BlockPos(centerX + diff, centerY + halfHeight, centerZ + diff)
            );
        }
        else {
            diff = (int) Math.ceil(entity.getBbWidth() + entity.getBbHeight() * 0.66);
            blockPoses = BlockPos.betweenClosed(
                    new BlockPos(centerX - halfWidth, centerY - halfHeight, centerZ - halfWidth),
                    new BlockPos(centerX + halfWidth, centerY + halfHeight, centerZ + halfWidth)
            );
        }
        for (BlockPos blockPos : blockPoses){
            Vec3 vec = blockPos.getCenter();
            if (vec.distanceTo(center.getCenter()) > diff * DESTRUCTION_MULTIPLIER) continue;
            double angle = Math.acos(impulse.normalize().dot(vec.subtract(entity.getBoundingBox().getCenter()).normalize())) * Mth.RAD_TO_DEG;
            if (angle > DIFF_ANGLE) continue;
            if (calculateImpactDamage(blockPos, entity.level(),power - blockPos.getCenter().distanceTo(center.getCenter()))) {
                entity.level().removeBlock(blockPos, false);
            }
        }
        if (power > 5) reapplyImpulse(entity);
    }

    private static boolean calculateImpactDamage(BlockPos blockPos, Level level, double residualPower){
        BlockState blockState = level.getBlockState(blockPos);
        float hardness = blockState.getDestroySpeed(level, blockPos);
        if (hardness < 0) return false;
        double finalPower = residualPower - hardness;
        if (finalPower >= 0) return true;
        else return false;
    }

    private static void reapplyImpulse(Entity entity){
        Vec3 initialForce = trackedEntities.get(entity).scale(0.75);
        entity.setDeltaMovement(initialForce);
        trackedEntities.put(entity, initialForce);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event){
        if (event.phase == TickEvent.Phase.END) return;
        trackedEntities.keySet().removeIf(entity -> {
            if ((entity.verticalCollision || entity.verticalCollisionBelow) && entity.horizontalCollision){
                System.out.println(entity.getClass().getSimpleName() + " hit bidirectionally");
                makeImpact(entity, entity.getBoundingBox().getCenter(), trackedEntities.get(entity), CollisionType.BIDIRECTIONAL);
                if (trackedEntities.get(entity).lengthSqr() < 0.1) {
                    System.out.println("Deleted with length: " + trackedEntities.get(entity).lengthSqr());
                    return true;
                }
            }
            else {
                if (entity.horizontalCollision) {
                    System.out.println(entity.getClass().getSimpleName() + " hit horizontally");
                    makeImpact(entity, entity.getBoundingBox().getCenter(), trackedEntities.get(entity), CollisionType.HORIZONTAL);
                    if (trackedEntities.get(entity).lengthSqr() < 0.1) {
                        System.out.println("Deleted with length: " + trackedEntities.get(entity).lengthSqr());
                        return true;
                    }
                }
                else if (entity.verticalCollision || entity.verticalCollisionBelow) {
                    System.out.println(entity.getClass().getSimpleName() + " hit vertically");
                    makeImpact(entity, entity.getBoundingBox().getCenter(), trackedEntities.get(entity), CollisionType.VERTICAL);
                    if (trackedEntities.get(entity).lengthSqr() < 0.1) {
                        System.out.println("Deleted with length: " + trackedEntities.get(entity).lengthSqr());
                        return true;
                    }
                }
                trackedEntities.put(entity, entity.getDeltaMovement());
            }
            return false;
        });
    }

    private enum CollisionType{
        VERTICAL,
        HORIZONTAL,
        BIDIRECTIONAL
    }
}
