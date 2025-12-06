package com.aleksiyflekssiy.tutorialmod.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class RotationUtil {
    public static Vec3 getOffsetLookPosition(Entity entity, Vec3 lookPos, double rightOffset, double upOffset,
                                             double forwardOffset) {
        Vec3 lookVec = entity.getLookAngle().normalize(); // Вектор взгляда (вперёд)
        Vec3 upVec = new Vec3(0, 1, 0); // Вертикаль мира
        Vec3 rightVec = lookVec.cross(upVec).normalize(); // Вектор вправо (перпендикулярно взгляду)

        // Корректировка верхнего вектора для точности
        Vec3 adjustedUpVec = rightVec.cross(lookVec).normalize(); // Верхний вектор относительно взгляда

        // Применяем смещение
        return lookPos
                .add(rightVec.scale(rightOffset))      // Смещение вправо/влево
                .add(adjustedUpVec.scale(upOffset))    // Смещение вверх/вниз
                .add(lookVec.scale(forwardOffset));    // Смещение вперёд/назад
    }
}
