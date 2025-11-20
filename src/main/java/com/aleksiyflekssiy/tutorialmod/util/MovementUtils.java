package com.aleksiyflekssiy.tutorialmod.util;

import net.minecraft.client.player.Input;

public class MovementUtils {
    public static void immobilize(Input input) {
        input.down = false;
        input.up = false;
        input.left = false;
        input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;
        input.forwardImpulse = 0;
        input.leftImpulse = 0;
    }
}
