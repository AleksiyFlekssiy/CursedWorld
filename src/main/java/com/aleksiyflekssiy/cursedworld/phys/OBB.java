package com.aleksiyflekssiy.cursedworld.phys;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class OBB {
    private final double minX;
    private final double minY;
    private final double minZ;
    private final double maxX;
    private final double maxY;
    private final double maxZ;
    private final double pitch;
    private final double yaw;
    private final double roll;

    public OBB(double minX,
               double minY,
               double minZ,
               double maxX,
               double maxY,
               double maxZ,
               double pitch,
               double yaw,
               double roll) {

        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }

    public OBB(Vec3 first, Vec3 second, Vector3f rotations){
        this.minX = first.x;
        this.minY = first.y;
        this.minZ = first.z;
        this.maxX = second.x;
        this.maxY = second.y;
        this.maxZ = second.z;
        this.pitch = rotations.x;
        this.yaw = rotations.y;
        this.roll = rotations.z;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMinZ() {
        return minZ;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMaxZ() {
        return maxZ;
    }

    public double getPitch() {
        return pitch;
    }

    public double getYaw() {
        return yaw;
    }

    public double getRoll() {
        return roll;
    }

    public Vec3 getRotations(){
        return new Vec3(pitch, yaw, roll);
    }

    public double getWidth(){
        return maxX - minX;
    }

    public double getHeight(){
        return maxY - minY;
    }

    public double getDepth(){
        return maxZ - minZ;
    }

    public Vec3 getCenter() {
        return new Vec3(
                minX + getWidth() / 2,
                minY + getHeight() / 2,
                minZ + getDepth() / 2
        );
    }

    public Vec3 getHalfSize() {
        return new Vec3(
                (maxX - minX) / 2,
                (maxY - minY) / 2,
                (maxZ - minZ) / 2
        );
    }

    public List<Vec3> getVertices() {
        List<Vec3> result = new ArrayList<>();

        Vec3 center = getCenter();
        Vec3 half = getHalfSize();

        double pitch = Math.toRadians(this.pitch);
        double yaw   = Math.toRadians(this.yaw);
        double roll  = Math.toRadians(this.roll);

        // 8 вершин AABB (локально)
        double[] xs = {-half.x, half.x};
        double[] ys = {-half.y, half.y};
        double[] zs = {-half.z, half.z};

        for (double x : xs) {
            for (double y : ys) {
                for (double z : zs) {

                    // --- вращение Y ---
                    double cosY = Math.cos(yaw);
                    double sinY = Math.sin(yaw);
                    double x1 = x * cosY + z * sinY;
                    double y1 = y;
                    double z1 = -x * sinY + z * cosY;

                    // --- вращение X ---
                    double cosX = Math.cos(pitch);
                    double sinX = Math.sin(pitch);
                    double x2 = x1;
                    double y2 = y1 * cosX - z1 * sinX;
                    double z2 = y1 * sinX + z1 * cosX;

                    // --- вращение Z ---
                    double cosZ = Math.cos(roll);
                    double sinZ = Math.sin(roll);
                    double x3 = x2 * cosZ - y2 * sinZ;
                    double y3 = x2 * sinZ + y2 * cosZ;
                    double z3 = z2;

                    // в мир
                    result.add(new Vec3(
                            x3 + center.x,
                            y3 + center.y,
                            z3 + center.z
                    ));
                }
            }
        }

        return result;
    }

    public OBB rotateX(double pitch){
        return new OBB(
                this.minX,
                this.minY,
                this.minZ,
                this.maxX,
                this.maxY,
                this.maxZ,
                this.pitch + pitch,
                this.yaw,
                this.roll
        );
    }

    public OBB rotateY(double yaw){
        return new OBB(
                this.minX,
                this.minY,
                this.minZ,
                this.maxX,
                this.maxY,
                this.maxZ,
                this.pitch,
                this.yaw + yaw,
                this.roll
        );
    }

    public OBB rotateZ(double roll){
        return new OBB(
                this.minX,
                this.minY,
                this.minZ,
                this.maxX,
                this.maxY,
                this.maxZ,
                this.pitch,
                this.yaw,
                this.roll + roll
        );
    }

    public OBB move(Vec3 offset){
        return new OBB(
                this.minX + offset.x,
                this.minY + offset.y,
                this.minZ + offset.z,
                this.maxX + offset.x,
                this.maxY + offset.y,
                this.maxZ + offset.z,
                this.pitch,
                this.yaw,
                this.roll
        );
    }

    public boolean contains(Vec3 point) {
        Vec3 center = getCenter();
        Vec3 half = getHalfSize();

        double x = point.x - center.x;
        double y = point.y - center.y;
        double z = point.z - center.z;

        double pitch = Math.toRadians(this.pitch);
        double yaw   = Math.toRadians(this.yaw);
        double roll  = Math.toRadians(this.roll);

        pitch = -pitch;
        yaw   = -yaw;
        roll  = -roll;

        double cosZ = Math.cos(roll);
        double sinZ = Math.sin(roll);
        double x1 = x * cosZ - y * sinZ;
        double y1 = x * sinZ + y * cosZ;
        double z1 = z;

        double cosX = Math.cos(pitch);
        double sinX = Math.sin(pitch);
        double x2 = x1;
        double y2 = y1 * cosX - z1 * sinX;
        double z2 = y1 * sinX + z1 * cosX;

        double cosY = Math.cos(yaw);
        double sinY = Math.sin(yaw);
        double x3 = x2 * cosY + z2 * sinY;
        double y3 = y2;
        double z3 = -x2 * sinY + z2 * cosY;

        return Math.abs(x3) <= half.x &&
                Math.abs(y3) <= half.y &&
                Math.abs(z3) <= half.z;
    }

    public AABB toAABB() {
        List<Vec3> vertices = getVertices();

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;

        for (Vec3 v : vertices) {
            minX = Math.min(minX, v.x);
            minY = Math.min(minY, v.y);
            minZ = Math.min(minZ, v.z);

            maxX = Math.max(maxX, v.x);
            maxY = Math.max(maxY, v.y);
            maxZ = Math.max(maxZ, v.z);
        }

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public boolean intersects(Entity entity){
        float width = entity.getBbWidth();
        float height = entity.getBbHeight();
        boolean result = false;
        AABB aabb = new AABB(
                new Vec3(entity.position().x - width / 2,
                        entity.position().y,
                        entity.position().z - width / 2),

                new Vec3(entity.position().x + width / 2,
                        entity.position().y + height,
                        entity.position().z + width / 2)
        );
        List<Vec3> vertices = List.of(
                new Vec3(aabb.minX, aabb.minY, aabb.minZ),
                new Vec3(aabb.minX, aabb.maxY, aabb.minZ),
                new Vec3(aabb.maxX, aabb.maxY, aabb.minZ),
                new Vec3(aabb.maxX, aabb.minY, aabb.minZ),
                new Vec3(aabb.minX, aabb.minY, aabb.maxZ),
                new Vec3(aabb.minX, aabb.maxY, aabb.maxZ),
                new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ),
                new Vec3(aabb.maxX, aabb.minY, aabb.maxZ)
        );
        for (Vec3 vertex : vertices){
            if (this.contains(vertex)) result = true;
            if (entity.level() instanceof ServerLevel level) {
                level.sendParticles(
                        ParticleTypes.CRIT,
                        vertex.x,
                        vertex.y,
                        vertex.z,
                        1,
                        0, 0, 0, 0
                );
            }
        }
        return true;
    }

    public static AABB createAAABFrom(OBB obb){
        return new AABB(obb.minX, obb.minY, obb.minZ, obb.maxX, obb.maxY, obb.maxZ);
    }

    public static OBB createOBBFrom(AABB aabb){
        return new OBB(
                aabb.minX,
                aabb.minY,
                aabb.minZ,
                aabb.maxX,
                aabb.maxY,
                aabb.maxZ,
                0,0,0
        );
    }

    @Override
    public int hashCode() {
        int j = Double.hashCode(this.minX);
        j = 31 * j + Double.hashCode(this.minY);
        j = 31 * j + Double.hashCode(this.minZ);
        j = 31 * j + Double.hashCode(this.maxX);
        j = 31 * j + Double.hashCode(this.maxY);
        j = 31 * j + Double.hashCode(this.maxZ);
        j = 31 * j + Double.hashCode(this.pitch);
        j = 31 * j + Double.hashCode(this.yaw);
        return 31 * j + Double.hashCode(this.roll);
    }

    @Override
    public String toString() {
        return "MinX: " + minX + " MinY: " + minY + " MinZ: " + minZ + " MaxX: " + maxX + " MaxY: " + maxY + " MaxZ: " + maxZ;
    }
}
