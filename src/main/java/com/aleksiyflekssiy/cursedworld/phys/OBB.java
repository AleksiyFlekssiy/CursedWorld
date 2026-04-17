package com.aleksiyflekssiy.cursedworld.phys;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

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

    public Vec3[] getVertices() {
        Vec3[] vertices = new Vec3[8];

        double cos = Math.cos(Math.toRadians(yaw));
        double sin = Math.sin(Math.toRadians(yaw));

        int i = 0;

        for (int x = -1; x <= 1; x += 2) {
            for (int y = -1; y <= 1; y += 2) {
                for (int z = -1; z <= 1; z += 2) {

                    double localX = getWidth() * x / 2;
                    double localY = getHeight() * y / 2;
                    double localZ = getDepth() * z / 2;

                    // вращение по Y
                    double rotX = localX * cos - localZ * sin;
                    double rotZ = localX * sin + localZ * cos;

                    vertices[i++] = getCenter().add(rotX, localY, rotZ);
                }
            }
        }

        return vertices;
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
        // центр
        Vec3 center = getCenter();

        // переводим точку в локальные координаты бокса
        Vec3 local = point.subtract(center);

        // 🔥 вращаем точку ОБРАТНО повороту бокса
        float yawRad = (float) Math.toRadians(-yaw);

        double cos = Math.cos(yawRad);
        double sin = Math.sin(yawRad);

        double x = local.x * cos - local.z * sin;
        double z = local.x * sin + local.z * cos;

        double y = local.y;

        // halfSize
        Vec3 half = new Vec3(
                (maxX - minX) / 2,
                (maxY - minY) / 2,
                (maxZ - minZ) / 2
        );

        return Math.abs(x) <= half.x &&
                Math.abs(y) <= half.y &&
                Math.abs(z) <= half.z;
    }

    public AABB toAABB() {
        Vec3[] vertices = getVertices();

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
