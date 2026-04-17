package com.aleksiyflekssiy.cursedworld.client.screen;

import com.aleksiyflekssiy.cursedworld.CursedWorld;
import com.aleksiyflekssiy.cursedworld.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.cursedworld.cursed_technique.skill.ShikigamiSkill;
import com.aleksiyflekssiy.cursedworld.cursed_technique.skill.Skill;
import com.aleksiyflekssiy.cursedworld.network.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@Mod.EventBusSubscriber(modid = CursedWorld.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeyHandler {

    public static final KeyMapping PRIMARY_SKILL_ACTIVATION = new KeyMapping(
            "key.cursedworld.primary_skill_activation", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.cursedworld");

    public static final KeyMapping SECONDARY_SKILL_ACTIVATION = new KeyMapping(
            "key.cursedworld.secondary_skill_activation", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, "key.categories.cursedworld");

    public static final KeyMapping PREVIOUS_SKILL = new KeyMapping(
            "key.cursedworld.previous_skill", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, "key.categories.cursedworld");

    public static final KeyMapping NEXT_SKILL = new KeyMapping(
            "key.cursedworld.next_skill", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, "key.categories.cursedworld");

    public static final KeyMapping SWITCH_JUJUTSU_HUD = new KeyMapping(
            "key.cursedworld.switch_jujutsu_hud", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.categories.cursedworld"
    );

    public static final KeyMapping SKILL_1 = new KeyMapping(
            "key.cursedworld.skill_1", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_1, "key.categories.cursedworld"
    );
    public static final KeyMapping SKILL_2 = new KeyMapping(
            "key.cursedworld.skill_2", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_2, "key.categories.cursedworld"
    );
    public static final KeyMapping SKILL_3 = new KeyMapping(
            "key.cursedworld.skill_3", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_3, "key.categories.cursedworld"
    );
    public static final KeyMapping SKILL_4 = new KeyMapping(
            "key.cursedworld.skill_4", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_4, "key.categories.cursedworld"
    );
    public static final KeyMapping SKILL_5 = new KeyMapping(
            "key.cursedworld.skill_5", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_5, "key.categories.cursedworld"
    );
    public static final KeyMapping SKILL_6 = new KeyMapping(
            "key.cursedworld.skill_6", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_6, "key.categories.cursedworld"
    );
    public static final KeyMapping SKILL_7 = new KeyMapping(
            "key.cursedworld.skill_7", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_7, "key.categories.cursedworld"
    );
    public static final KeyMapping SKILL_8 = new KeyMapping(
            "key.cursedworld.skill_8", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_8, "key.categories.cursedworld"
    );
    public static final KeyMapping SKILL_9 = new KeyMapping(
            "key.cursedworld.skill_9", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_9, "key.categories.cursedworld"
    );


    private int chargeTicks = 0;
    private boolean isCharging = false;
    private boolean wasPressedLastTick = false;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (SWITCH_JUJUTSU_HUD.consumeClick()){
            JujutsuHUD.shouldRender = !JujutsuHUD.shouldRender;
        }

        if (!JujutsuHUD.shouldRender) return;

        if (PRIMARY_SKILL_ACTIVATION.consumeClick()){
            Skill selectedSkill = CursedTechniqueCapability.getCurrentSkill(mc.player);
            if (selectedSkill != null) {
                ModMessages.INSTANCE.sendToServer(new UseSkillPacket(selectedSkill.getName(), Skill.UseType.ACTIVATION, 0));
                //mc.player.sendSystemMessage(Component.literal("Quick activation!"));
            }
        }

        List<Skill> skills = CursedTechniqueCapability.getCursedTechnique(mc.player).getSkillSet();
        if (SKILL_1.consumeClick()){
            if (!skills.isEmpty()) {
                CursedTechniqueCapability.setCurrentSkill(mc.player, skills.get(0));
                ModMessages.INSTANCE.sendToServer(new SyncSkillPacket(skills.get(0).getName()));
            }
        }if (SKILL_2.consumeClick()){
            if (skills.size() >= 2) {
                CursedTechniqueCapability.setCurrentSkill(mc.player, skills.get(1));
                ModMessages.INSTANCE.sendToServer(new SyncSkillPacket(skills.get(1).getName()));
            }
        }if (SKILL_3.consumeClick()){
            if (skills.size() >= 3) {
                CursedTechniqueCapability.setCurrentSkill(mc.player, skills.get(2));
                ModMessages.INSTANCE.sendToServer(new SyncSkillPacket(skills.get(2).getName()));
            }
        }if (SKILL_4.consumeClick()){
            if (skills.size() >= 4) {
                CursedTechniqueCapability.setCurrentSkill(mc.player, skills.get(3));
                ModMessages.INSTANCE.sendToServer(new SyncSkillPacket(skills.get(3).getName()));
            }
        }if (SKILL_5.consumeClick()){
            if (skills.size() >= 5) {
                CursedTechniqueCapability.setCurrentSkill(mc.player, skills.get(4));
                ModMessages.INSTANCE.sendToServer(new SyncSkillPacket(skills.get(4).getName()));
            }
        }if (SKILL_6.consumeClick()){
            if (skills.size() >= 6) {
                CursedTechniqueCapability.setCurrentSkill(mc.player, skills.get(5));
                ModMessages.INSTANCE.sendToServer(new SyncSkillPacket(skills.get(5).getName()));
            }
        }if (SKILL_7.consumeClick()){
            if (skills.size() >= 7) {
                CursedTechniqueCapability.setCurrentSkill(mc.player, skills.get(6));
                ModMessages.INSTANCE.sendToServer(new SyncSkillPacket(skills.get(6).getName()));
            }
        }if (SKILL_8.consumeClick()){
            if (skills.size() >= 8) {
                CursedTechniqueCapability.setCurrentSkill(mc.player, skills.get(7));
                ModMessages.INSTANCE.sendToServer(new SyncSkillPacket(skills.get(7).getName()));
            }
        }if (SKILL_9.consumeClick()){
            if (skills.size() == 9) {
                CursedTechniqueCapability.setCurrentSkill(mc.player, skills.get(8));
                ModMessages.INSTANCE.sendToServer(new SyncSkillPacket(skills.get(8).getName()));
            }
        }

        boolean isPressed = SECONDARY_SKILL_ACTIVATION.isDown();

        if (isPressed && !wasPressedLastTick) {
            isCharging = true;
            chargeTicks = 0;
        }

        if (isCharging && isPressed) {
            chargeTicks++;
            Skill selectedSkill = CursedTechniqueCapability.getCurrentSkill(mc.player);
            if (selectedSkill != null) {
                ModMessages.INSTANCE.sendToServer(new UseSkillPacket(selectedSkill.getName(), Skill.UseType.CHARGING, chargeTicks));
                //if (chargeTicks == 1) mc.player.sendSystemMessage(Component.literal("Charging!"));
            }
        }

        if (!isPressed && wasPressedLastTick && isCharging) {
            if (chargeTicks > 1) {
                Skill selectedSkill = CursedTechniqueCapability.getCurrentSkill(mc.player);
                ModMessages.INSTANCE.sendToServer(new UseSkillPacket(selectedSkill.getName(), Skill.UseType.RELEASING, chargeTicks));
            }
            isCharging = false;
            chargeTicks = 0;
        }

        wasPressedLastTick = isPressed;

        if (PREVIOUS_SKILL.consumeClick()) {
            if (CursedTechniqueCapability.getCurrentSkill(mc.player) instanceof ShikigamiSkill && mc.player.isCrouching()){
                ModMessages.INSTANCE.sendToServer(new SwitchOrderPacket(-1));
            }
            else {
                CursedTechniqueCapability.previousSkill(mc.player);
                Skill selectedSkill = CursedTechniqueCapability.getCurrentSkill(mc.player);
                if (selectedSkill != null) {
                    ModMessages.INSTANCE.sendToServer(new SyncSkillPacket(selectedSkill.getName()));
                }
            }
        }

        if (NEXT_SKILL.consumeClick()) {
            if (CursedTechniqueCapability.getCurrentSkill(mc.player) instanceof ShikigamiSkill && mc.player.isCrouching()){
                ModMessages.INSTANCE.sendToServer(new SwitchOrderPacket(1));
            }
            else {
                CursedTechniqueCapability.nextSkill(mc.player);
                Skill selectedSkill = CursedTechniqueCapability.getCurrentSkill(mc.player);
                if (selectedSkill != null) {
                    ModMessages.INSTANCE.sendToServer(new SyncSkillPacket(selectedSkill.getName()));
                }
            }
        }
    }
}