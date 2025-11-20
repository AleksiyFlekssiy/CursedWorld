package com.aleksiyflekssiy.tutorialmod.entity.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BluntAirNavigation extends FlyingPathNavigation {
    public BluntAirNavigation(Mob pMob, Level pLevel) {
        super(pMob, pLevel);
    }

    @Override
    public @Nullable Path createPath(BlockPos pPos, int pAccuracy) {
        Node node = new Node(pPos.getX(), pPos.getY(), pPos.getZ());
        return new Path(List.of(node), node.asBlockPos(), false);
    }
}
