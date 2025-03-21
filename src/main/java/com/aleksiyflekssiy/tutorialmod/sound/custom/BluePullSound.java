package com.aleksiyflekssiy.tutorialmod.sound.custom;

import com.aleksiyflekssiy.tutorialmod.entity.BlueEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BluePullSound extends AbstractTickableSoundInstance {
    private final BlueEntity entity;
    private float targetVolume = 1.0F; // Максимальная громкость
    private float currentVolume = 0.0F; // Текущая громкость
    private final float fadeInDuration = 20.0F; // Длительность подъёма в тиках (1 секунда при 20 TPS)
    private final float fadeOutDuration = 20.0F; // Длительность затухания в тиках (1 секунда)
    private float fadeTimer = 0.0F; // Счётчик для fade-in
    private boolean isFadingOut = false; // Флаг для затухания

    public BluePullSound(SoundEvent soundEvent, BlueEntity entity) {
        super(soundEvent, SoundSource.NEUTRAL, entity.level().getRandom());
        this.entity = entity;
        this.looping = true; // Звук зациклен
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
        this.volume = 0.0F; // Начальная громкость 0 (для fade-in)
        this.pitch = 1.25F; // Высота звука
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        // Обновляем позицию звука, если сущность движется
        if (entity.isAlive()) {
            this.x = entity.getX();
            this.y = entity.getY();
            this.z = entity.getZ();
        }

        // Если сущность исчезла или lifetime истёк, начинаем затухание
        if (!entity.isAlive()) {
            this.isFadingOut = true;
        }

        // Управляем громкостью
        if (isFadingOut) {
            // Затухание
            fadeTimer -= 1.0F;
            if (fadeTimer < 0) {
                this.stop(); // Останавливаем звук, когда затухание завершено
            } else {
                this.currentVolume = (fadeTimer / fadeOutDuration) * targetVolume;
            }
        } else if (fadeTimer < fadeInDuration) {
            // Подъём
            fadeTimer += 1.0F;
            this.currentVolume = (fadeTimer / fadeInDuration) * targetVolume;
        } else {
            // Нормальная громкость после завершения fade-in
            this.currentVolume = targetVolume;
        }

        // Устанавливаем текущую громкость
        this.volume = this.currentVolume;
    }

    @Override
    public boolean canPlaySound() {
        return !this.isStopped() && (entity.isAlive() || fadeTimer > 0);
    }

    // Метод для начала затухания (если нужно вызвать вручную)
    public void startFadeOut() {
        if (!this.isFadingOut) {
            this.isFadingOut = true;
            this.fadeTimer = fadeOutDuration;
        }
    }
}