package com.aleksiyflekssiy.cursedworld.entity.behavior;

import com.aleksiyflekssiy.cursedworld.entity.Shikigami;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;

public class ShikigamiFollowOwner {

    public static OneShot<Shikigami> create(float requiredDistance) {
        return BehaviorBuilder.create((instance) -> instance
                        .group(instance.registered(MemoryModuleType.PATH), instance.registered(MemoryModuleType.WALK_TARGET), instance.present(CustomMemoryModuleTypes.OWNER.get()))
                        .apply(instance, (path, walkTarget, owner) -> (serverLevel, shikigami, longNumber) -> {
                            Player player = instance.get(owner);
                            if (player.position().distanceToSqr(shikigami.position()) >= requiredDistance) {
                                shikigami.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(player, 1, 1));
                                return true;
                            }
                            return false;
                        })
        );
    }

}
