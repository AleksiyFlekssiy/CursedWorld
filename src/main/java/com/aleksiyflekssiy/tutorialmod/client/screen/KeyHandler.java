package com.aleksiyflekssiy.tutorialmod.client.screen;

import com.aleksiyflekssiy.tutorialmod.TutorialMod;
import com.aleksiyflekssiy.tutorialmod.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.tutorialmod.cursed_technique.skill.Skill;
import com.aleksiyflekssiy.tutorialmod.network.UseSkillPacket;
import com.aleksiyflekssiy.tutorialmod.network.HoldSkillPacket;
import com.aleksiyflekssiy.tutorialmod.network.ModMessages;
import com.aleksiyflekssiy.tutorialmod.network.SyncSkillPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeyHandler {

    public static final KeyMapping PRIMARY_SKILL_ACTIVATION = new KeyMapping(
            "key.tutorialmod.primary_skill_activation", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.tutorialmod");

    public static final KeyMapping SECONDARY_SKILL_ACTIVATION = new KeyMapping(
            "key.tutorialmod.secondary_skill_activation", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, "key.categories.tutorialmod");

    public static final KeyMapping PREVIOUS_SKILL = new KeyMapping(
            "key.tutorialmod.previous_skill", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, "key.categories.tutorialmod");

    public static final KeyMapping NEXT_SKILL = new KeyMapping(
            "key.tutorialmod.next_skill", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, "key.categories.tutorialmod");

    private int chargeTicks = 0;
    private boolean isCharging = false;
    private boolean wasPressedLastTick = false;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (PRIMARY_SKILL_ACTIVATION.consumeClick()){
            Skill selectedSkill = CursedTechniqueCapability.getSkill(mc.player);
            if (selectedSkill != null) {
                ModMessages.INSTANCE.sendToServer(new UseSkillPacket(selectedSkill.getName(), Skill.UseType.ACTIVATION, 0));
                mc.player.sendSystemMessage(Component.literal("Quick activation!"));
            }
        }

        boolean isPressed = SECONDARY_SKILL_ACTIVATION.isDown();

        if (isPressed && !wasPressedLastTick) {
            isCharging = true;
            chargeTicks = 0;
        }

        if (isCharging && isPressed) {
            chargeTicks++;
            Skill selectedSkill = CursedTechniqueCapability.getSkill(mc.player);
            if (selectedSkill != null) {
                ModMessages.INSTANCE.sendToServer(new UseSkillPacket(selectedSkill.getName(), Skill.UseType.CHARGING, chargeTicks));
                if (chargeTicks == 1) mc.player.sendSystemMessage(Component.literal("Charging!"));
            }
        }

        if (!isPressed && wasPressedLastTick && isCharging) {
            if (chargeTicks > 1) {
                Skill selectedSkill = CursedTechniqueCapability.getSkill(mc.player);
                ModMessages.INSTANCE.sendToServer(new UseSkillPacket(selectedSkill.getName(), Skill.UseType.RELEASING, chargeTicks));
            }
            isCharging = false;
            chargeTicks = 0;
        }

        wasPressedLastTick = isPressed;

        if (PREVIOUS_SKILL.consumeClick()) {
            CursedTechniqueCapability.previousSkill(mc.player);
            Skill selectedSkill = CursedTechniqueCapability.getSkill(mc.player);
            if (selectedSkill != null) {
                ModMessages.INSTANCE.sendToServer(new SyncSkillPacket(selectedSkill.getName()));
            }
        }

        if (NEXT_SKILL.consumeClick()) {
            CursedTechniqueCapability.nextSkill(mc.player);
            Skill selectedSkill = CursedTechniqueCapability.getSkill(mc.player);
            if (selectedSkill != null) {
                ModMessages.INSTANCE.sendToServer(new SyncSkillPacket(selectedSkill.getName()));
            }
        }
    }
}