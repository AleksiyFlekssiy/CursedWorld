package com.aleksiyflekssiy.tutorialmod.capability;

public class CursedEnergy implements ICursedEnergy {
    private int cursedEnergy;
    private final int maxCursedEnergy;
    private boolean fastTick;
    private int tickCounter; // Счётчик тиков для каждого игрока
    private static final int TICKS_INTERVAL = 10; // Интервал в 10 тиков

    public CursedEnergy(int maxCursedEnergy) {
        this.maxCursedEnergy = maxCursedEnergy;
        this.cursedEnergy = maxCursedEnergy;
        this.tickCounter = 0;
    }

    @Override
    public int getCursedEnergy() {
        return this.cursedEnergy;
    }

    @Override
    public void setCursedEnergy(int cursedEnergy) {
        this.cursedEnergy = cursedEnergy;
    }

    @Override
    public int getMaxCursedEnergy() {
        return this.maxCursedEnergy;
    }

    public void tick() {
        if (fastTick) tickCounter += 5;
        else tickCounter++;
        if (tickCounter >= TICKS_INTERVAL) {
            if (cursedEnergy < maxCursedEnergy) {
                cursedEnergy++;
            }
            tickCounter = 0;
        }
    }

    public boolean isFastTick() {
        return fastTick;
    }

    public void setFastTick(boolean fastTick) {
        this.fastTick = fastTick;
    }

    public int getTickCounter() {
        return tickCounter;
    }

    public void setTickCounter(int tickCounter) {
        this.tickCounter = tickCounter;
    }
}
