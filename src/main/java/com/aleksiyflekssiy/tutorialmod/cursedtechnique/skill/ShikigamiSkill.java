package com.aleksiyflekssiy.tutorialmod.cursedtechnique.skill;

import com.aleksiyflekssiy.tutorialmod.entity.GreatSerpentEntity;
import com.aleksiyflekssiy.tutorialmod.entity.Shikigami;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class ShikigamiSkill extends Skill{
    protected List<UUID> shikigamiUUIDList = new ArrayList<>();
    protected boolean isActive;
    protected boolean isTamed;
    protected boolean isDead;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isTamed() {
        return isTamed;
    }

    public void setTamed(boolean tamed) {
        isTamed = tamed;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }

    public List<UUID> getShikigamiUUID() {
        return shikigamiUUIDList;
    }

    public void setShikigamiUUID(List<UUID> shikigamiUUID) {
        this.shikigamiUUIDList = shikigamiUUID;
    }

    public abstract List<Shikigami> getShikigami();

    public abstract void setShikigami(List<Shikigami> shikigamiList);

    //-1 for the previous order, 1 for the next order
    public abstract void switchOrder(LivingEntity owner, int direction);

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.putBoolean("isActive", isActive);
        tag.putBoolean("isTamed", isTamed);
        tag.putBoolean("isDead", isDead);
        if (!shikigamiUUIDList.isEmpty()) {
            ListTag shikigamiList = new ListTag();
            for (UUID uuid : shikigamiUUIDList) {
                CompoundTag shikigamiTag = new CompoundTag();
                shikigamiTag.putUUID("uuid", uuid);
                shikigamiList.add(shikigamiTag);
            }
            tag.put("shikigamiUUID", shikigamiList);
        }
        else System.out.println("Nothing to save");
    }

    @Override
    public void loadAdditional(CompoundTag tag) {
        isActive = tag.getBoolean("isActive");
        isTamed = tag.getBoolean("isTamed");
        isDead = tag.getBoolean("isDead");
        if (tag.contains("shikigamiUUID")) {
            ListTag listTag = tag.getList("shikigamiUUID", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); ++i) {
                CompoundTag shikigamiTag = listTag.getCompound(i);
                UUID uuid = shikigamiTag.getUUID("uuid");
                this.shikigamiUUIDList.add(uuid);
            }
        }
        else System.out.println("Nothing to load");
    }

    public static Shikigami getShikigamiFromUUID(UUID uuid, ServerLevel server) {
        return ((Shikigami) server.getEntity(uuid));
    }
}
