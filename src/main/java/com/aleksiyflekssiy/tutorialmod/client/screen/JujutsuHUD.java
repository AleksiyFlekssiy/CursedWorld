package com.aleksiyflekssiy.tutorialmod.client.screen;

import com.aleksiyflekssiy.tutorialmod.capability.CursedEnergyCapability;
import com.aleksiyflekssiy.tutorialmod.capability.CursedTechniqueCapability;
import com.aleksiyflekssiy.tutorialmod.capability.ICursedEnergy;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class JujutsuHUD {
    private static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/gui/skill_slot.png");

    @SubscribeEvent
    public void onRenderGui(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        if (mc.player == null || !mc.player.isAlive()) return;
        ResourceLocation ABILITY_ICON = CursedTechniqueCapability.getCurrentSkill(mc.player).getSkillIcon();
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

        // Новый слот для способности
        int xSlot = 10; // Позиция слота (можно настроить)
        int ySlot = 40; // Под полоской энергии
        int slotSize = 36;

        guiGraphics.fill(xSlot, ySlot, xSlot + slotSize + 100, ySlot + slotSize, 0xFF555555);

        // Рисуем иконку
        RenderSystem.setShaderTexture(0, ABILITY_ICON);
        guiGraphics.blit(ABILITY_ICON, xSlot + 2, ySlot + 2, 0, 0, 32, 32, 32, 32);

        // Рисуем слот поверх иконки
        RenderSystem.setShaderTexture(0, SLOT_TEXTURE);
        guiGraphics.blit(SLOT_TEXTURE, xSlot, ySlot, 0, 0, slotSize, slotSize, slotSize, slotSize);
    }
}
