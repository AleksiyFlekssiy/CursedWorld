package com.aleksiyflekssiy.tutorialmod.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AbilityScreen extends Screen {
    private static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/gui/skill_slot.png");
    private static final ResourceLocation ABILITY_ICON = ResourceLocation.fromNamespaceAndPath("tutorialmod", "textures/gui/blue.png");

    public AbilityScreen() {
        super(Component.literal("Способности"));
    }

    @Override
    protected void init() {
        super.init();
        this.width = 100; // Ширина окна
        this.height = 100; // Высота окна
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics); // Затемняем фон
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        // Центрируем слот 34x34
        int x = (this.width - 36) / 2;
        int y = (this.height - 36) / 2;

        // Рисуем фон слота
        RenderSystem.setShaderTexture(0, SLOT_TEXTURE);
        guiGraphics.blit(SLOT_TEXTURE, x, y, 0, 0, 36, 36, 36, 36); // Указываем размер текстуры 34x34

        // Рисуем иконку способности (центрируем внутри слота)
        RenderSystem.setShaderTexture(0, ABILITY_ICON);
        int iconX = x + 2; // Смещение на 2 пикселя для рамки
        int iconY = y + 2;
        guiGraphics.blit(ABILITY_ICON, iconX, iconY, 0, 0, 32, 32, 32, 32); // Иконка 32x32

        // Рисуем заголовок
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}