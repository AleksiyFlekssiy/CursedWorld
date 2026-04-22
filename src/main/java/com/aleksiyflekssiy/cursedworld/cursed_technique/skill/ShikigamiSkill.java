package com.aleksiyflekssiy.cursedworld.cursed_technique.skill;

import com.aleksiyflekssiy.cursedworld.entity.Shikigami;
import com.aleksiyflekssiy.cursedworld.entity.ShikigamiOrder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class ShikigamiSkill extends Skill{
    protected int orderIndex = 0;
    protected List<Shikigami> shikigamiList = new ArrayList<>();
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

    protected void setTarget(LivingEntity owner, Consumer<BlockPos> blockAction, Consumer<LivingEntity> entityAction) {
        HitResult result = ProjectileUtil.getHitResultOnViewVector(owner, target -> !target.equals(owner), 100);
        if (result.getType() == HitResult.Type.ENTITY && entityAction != null) {
            EntityHitResult hitResult = (EntityHitResult) result;
            if (hitResult.getEntity() instanceof LivingEntity target) {
                entityAction.accept(target);
            }
        } else if (result.getType() == HitResult.Type.BLOCK && blockAction != null) {
            BlockHitResult hitResult = (BlockHitResult) result;
            System.out.println(hitResult.getBlockPos());
            blockAction.accept(hitResult.getBlockPos());
        }
        else if (result.getType() == HitResult.Type.MISS && blockAction != null) {
            blockAction.accept(BlockPos.containing(result.getLocation()));
        }
    }

    public abstract void setShikigami(List<Shikigami> shikigamiList);

    //-1 for the previous order, 1 for the next order
    public void switchOrder(LivingEntity owner, int direction) {
        if (isTamed) {
            int first = 0;
            int last = this.getOrders().size() - 1;
            switch (direction){
                case -1 -> {
                    if (--this.orderIndex < first) this.orderIndex = last;
                }
                case 1 -> {
                    if (++this.orderIndex > last) this.orderIndex = first;
                }
            }
            owner.sendSystemMessage(Component.literal(this.getOrders().get(orderIndex).getOrder()));
        }
    }

    public List<ShikigamiOrder> getOrders(){
        return List.of(ShikigamiOrder.NONE);
    }

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
