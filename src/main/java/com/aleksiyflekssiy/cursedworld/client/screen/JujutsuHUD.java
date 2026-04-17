package com.aleksiyflekssiy.cursedworld.client.screen;

import com.aleksiyflekssiy.cursedworld.CursedWorld;
import com.aleksiyflekssiy.cursedworld.capability.CursedEnergyCapability;
import com.aleksiyflekssiy.cursedworld.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.cursedworld.capability.ICursedEnergy;
import com.aleksiyflekssiy.cursedworld.cursed_technique.skill.Skill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class JujutsuHUD {
    private static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(CursedWorld.MOD_ID, "textures/gui/skill_slot.png");
    public static boolean shouldRender = false;

    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        // Отключаем только хотбар
        if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type() && shouldRender) event.getGuiGraphics().flush();
    }



    @SubscribeEvent
    public void onRenderGui(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        if (mc.player == null || !mc.player.isAlive() || shouldRender == false) return;
        List<Skill> skills = CursedTechniqueCapability.getCursedTechnique(mc.player).getSkillSet();
        ResourceLocation ABILITY_ICON;
        // Рисуем текст
        //guiGraphics.drawString(mc.font, "Привет, это мой HUD!", 10, 10, 0xFFFFFF, true);

        // Рисуем полоску
        int x = 10;
        int y = 20;
        int width = 100;
        int height = 10;
        int cursedEnergy = mc.player.getCapability(CursedEnergyCapability.CURSED_ENERGY).map(ICursedEnergy::getCursedEnergy).orElse(0);
        int maxCursedEnergy = mc.player.getCapability(CursedEnergyCapability.CURSED_ENERGY).map(ICursedEnergy::getMaxCursedEnergy).orElse(100);


        String energyText = "Cursed Energy: " + cursedEnergy + "/" + maxCursedEnergy;
        guiGraphics.drawString(mc.font, energyText, 10, 10, 0xFFFFFF, true);
        int filledWidth = (int) ((float) cursedEnergy / maxCursedEnergy * width);
        guiGraphics.fill(x, y, x + width, y + height, 0xFF555555); // Фон (серый)
        guiGraphics.fill(x, y, x + filledWidth, y + height, 0xFFFF0000); // Заполненная часть (красный)

        int slotSize = 28;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0,0,200);

        // Рисуем иконку
        //RenderSystem.setShaderTexture(0, ABILITY_ICON);

        int f = (int) (guiGraphics.guiWidth() / 2 - (9 * 28 * 0.5));
        guiGraphics.fill(f, guiGraphics.guiHeight() - 28, f + 28 * 9, guiGraphics.guiHeight(), 0xFF555555);
        for (int i = 0; i < 9; i++){
            if (i < skills.size()) {
                ABILITY_ICON = skills.get(i).getSkillIcon();
                if (skills.get(i).equals(CursedTechniqueCapability.getCurrentSkill(mc.player))) {
                    guiGraphics.blit(ABILITY_ICON, f + 26 * i + 2 * (1 + i), guiGraphics.guiHeight() - 30, 0, 0, 24, 24, 24, 24);
                }
                else guiGraphics.blit(ABILITY_ICON, f + 26 * i + 2 * (1 + i), guiGraphics.guiHeight() - 26, 0, 0, 24, 24, 24, 24);
            }
            guiGraphics.blit(SLOT_TEXTURE, f + 28 * i, guiGraphics.guiHeight() - 28, 0, 0, slotSize, slotSize, slotSize, slotSize);
        }
        guiGraphics.pose().popPose();
        // Рисуем слот поверх иконки
    }
}
